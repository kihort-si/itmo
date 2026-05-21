package com.itmo.mybroker.core

import com.google.gson.Gson
import com.itmo.mybroker.BuildConfig
import com.itmo.mybroker.api.ApiClient
import com.itmo.mybroker.data.BrokerData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.atomic.AtomicInteger

data class LiveQuote(
    val symbol: String,
    val price: Double,
    val change: Double,
    val changePct: Double,
    val spark: List<Double>,
    val updatedAt: Long = System.currentTimeMillis(),
)

private data class QuoteMsg(
    val type: String? = null,
    val symbol: String? = null,
    val symbols: List<String>? = null,
    val price: Double? = null,
    val change: Double? = null,
    val changePct: Double? = null,
    val spark: List<Double>? = null,
)

object QuotesHub {
    private val gson = Gson()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val refCount = mutableMapOf<String, AtomicInteger>()
    private var ws: WebSocket? = null
    private var mockJob: Job? = null
    private var reconnectJob: Job? = null

    private val _quotes = MutableStateFlow<Map<String, LiveQuote>>(emptyMap())
    val quotes: StateFlow<Map<String, LiveQuote>> = _quotes.asStateFlow()

    private fun seed(sym: String): LiveQuote {
        val tk = BrokerData.repository.bySym(sym)
        val s = BrokerData.repository.dayStats(tk)
        return LiveQuote(tk.sym, s.price, s.change, s.changePct, s.spark)
    }

    fun quoteFor(sym: String): LiveQuote? = _quotes.value[sym]

    fun subscribe(sym: String) {
        val c = refCount.getOrPut(sym) { AtomicInteger(0) }
        c.incrementAndGet()
        if (_quotes.value[sym] == null) {
            _quotes.value = _quotes.value + (sym to seed(sym))
        }
        syncConnection()
    }

    fun unsubscribe(sym: String) {
        refCount[sym]?.decrementAndGet()
        if (refCount[sym]?.get() == 0) {
            refCount.remove(sym)
        }
        syncConnection()
    }

    fun onSessionChanged() {
        closeWs()
        syncConnection()
    }

    private fun subscribedSymbols(): List<String> =
        refCount.filter { it.value.get() > 0 }.keys.toList()

    private fun syncConnection() {
        val syms = subscribedSymbols()
        if (syms.isEmpty()) {
            stopMock()
            closeWs()
            return
        }
        if (BuildConfig.USE_MOCK_API) {
            closeWs()
            startMock()
            return
        }
        stopMock()
        openOrReconnectWs()
    }

    private fun setQuote(q: LiveQuote) {
        _quotes.value = _quotes.value + (q.symbol to q)
    }

    private fun handleMessage(json: String) {
        val msg = runCatching { gson.fromJson(json, QuoteMsg::class.java) }.getOrNull() ?: return
        if (msg.type != "quote") return
        val sym = msg.symbol ?: return
        val prev = _quotes.value[sym] ?: seed(sym)
        setQuote(
            LiveQuote(
                symbol = sym,
                price = msg.price ?: prev.price,
                change = msg.change ?: prev.change,
                changePct = msg.changePct ?: prev.changePct,
                spark = msg.spark ?: prev.spark,
            ),
        )
    }

    private fun sendSubscribe(symbols: List<String>) {
        if (symbols.isEmpty()) return
        val w = ws ?: return
        val payload = gson.toJson(mapOf("type" to "subscribe", "symbols" to symbols))
        w.send(payload)
    }

    private fun startMock() {
        if (mockJob?.isActive == true) return
        mockJob = scope.launch {
            while (isActive) {
                delay(1200)
                val syms = subscribedSymbols()
                for (sym in syms) {
                    val prev = _quotes.value[sym] ?: seed(sym)
                    val jitter = (Math.random() - 0.5) * prev.price * 0.002
                    val price = maxOf(0.01, prev.price + jitter)
                    val change = price - (prev.price - prev.change)
                    val base = prev.price - prev.change
                    val changePct = if (base != 0.0) change / base * 100 else 0.0
                    setQuote(prev.copy(price = price, change = change, changePct = changePct))
                }
            }
        }
    }

    private fun stopMock() {
        mockJob?.cancel()
        mockJob = null
    }

    private fun closeWs() {
        ws?.close(1000, null)
        ws = null
        reconnectJob?.cancel()
        reconnectJob = null
    }

    private fun wsUrl(): String {
        val base = BuildConfig.API_BASE_URL.trimEnd('/')
        val wsBase = if (base.startsWith("https://")) "wss://" + base.removePrefix("https://")
        else "ws://" + base.removePrefix("http://")
        val path = "$wsBase/api/broker-app/v1/quotes/stream"
        val token = SessionStore.jwt.value
        return if (token != null) "$path?token=${java.net.URLEncoder.encode(token, Charsets.UTF_8.name())}"
        else path
    }

    private fun openOrReconnectWs() {
        if (ws != null) return
        scope.launch(Dispatchers.IO) {
            try {
                val req = Request.Builder().url(wsUrl()).build()
                ws = ApiClient.client.newWebSocket(req, object : WebSocketListener() {
                    override fun onMessage(webSocket: WebSocket, text: String) {
                        handleMessage(text)
                    }

                    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                        handleMessage(bytes.utf8())
                    }

                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        stopMock()
                        sendSubscribe(subscribedSymbols())
                    }

                    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                        ws = null
                        scheduleReconnect()
                    }

                    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                        ws = null
                    }
                })
            } catch (_: Exception) {
                scheduleReconnect()
                startMock()
            }
        }
    }

    private fun scheduleReconnect() {
        if (reconnectJob?.isActive == true) return
        reconnectJob = scope.launch {
            delay(1500)
            if (subscribedSymbols().isNotEmpty() && !BuildConfig.USE_MOCK_API) {
                openOrReconnectWs()
            }
        }
    }
}
