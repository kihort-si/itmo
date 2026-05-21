package com.itmo.mybroker.core

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.itmo.mybroker.BuildConfig
import com.itmo.mybroker.api.ApiClient
import com.itmo.mybroker.api.AuthApi
import com.itmo.mybroker.api.AuthResponse
import com.itmo.mybroker.api.LoginRequest
import com.itmo.mybroker.api.MockApi
import com.itmo.mybroker.api.RegisterRequest
import com.itmo.mybroker.api.UserApi
import com.itmo.mybroker.data.BrokerData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val Context.sessionDataStore by preferencesDataStore(name = "session")
private const val SESSION_LOG_TAG = "MyBrokerSession"

object SessionStore {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var appContext: Context? = null

    private val _jwt = MutableStateFlow<String?>(null)
    val jwt: StateFlow<String?> = _jwt.asStateFlow()

    private val _email = MutableStateFlow<String?>(null)
    val email: StateFlow<String?> = _email.asStateFlow()

    private val _username = MutableStateFlow<String?>(null)
    val username: StateFlow<String?> = _username.asStateFlow()

    private val _name = MutableStateFlow<String?>(null)
    val name: StateFlow<String?> = _name.asStateFlow()

    private val _clntId = MutableStateFlow<Int?>(null)
    val clntId: StateFlow<Int?> = _clntId.asStateFlow()

    private val _balance = MutableStateFlow(BrokerData.repository.cashRub)
    val balance: StateFlow<Double> = _balance.asStateFlow()

    fun init(context: Context) {
        if (appContext != null) return
        Log.d(SESSION_LOG_TAG, "SessionStore.init marker=${BuildConfig.AUTH_FORM_MARKER} base=${BuildConfig.API_BASE_URL} mock=${BuildConfig.USE_MOCK_API}")
        appContext = context.applicationContext
        ApiClient.tokenProvider = { _jwt.value }
        scope.launch(Dispatchers.IO) { hydrate() }
    }

    suspend fun hydrate() {
        val ctx = appContext ?: return
        val prefs = ctx.sessionDataStore.data.first()
        val j = prefs[JWT_KEY]
        val e = prefs[EMAIL_KEY]
        val u = prefs[USERNAME_KEY]
        val n = prefs[NAME_KEY]
        val c = prefs[CLNT_ID_KEY]
        val b = prefs[BALANCE_KEY] ?: BrokerData.repository.cashRub
        withContext(Dispatchers.Main) {
            _jwt.value = j
            _email.value = e
            _username.value = u
            _name.value = n
            _clntId.value = c
            _balance.value = b
        }
        Log.d(SESSION_LOG_TAG, "Hydrated session tokenPresent=${!j.isNullOrBlank()} email=$e username=$u clntId=$c")
    }

    private suspend fun persist() {
        val ctx = appContext ?: return
        ctx.sessionDataStore.edit { p ->
            val j = _jwt.value
            if (j == null) {
                p.remove(JWT_KEY)
                p.remove(EMAIL_KEY)
                p.remove(USERNAME_KEY)
                p.remove(NAME_KEY)
                p.remove(CLNT_ID_KEY)
                p.remove(BALANCE_KEY)
            } else {
                p[JWT_KEY] = j
                _email.value?.let { p[EMAIL_KEY] = it }
                _username.value?.let { p[USERNAME_KEY] = it }
                _name.value?.let { p[NAME_KEY] = it }
                _clntId.value?.let { p[CLNT_ID_KEY] = it }
                p[BALANCE_KEY] = _balance.value
            }
        }
    }

    suspend fun login(body: LoginRequest): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(SESSION_LOG_TAG, "Login started login=${body.email} mock=${BuildConfig.USE_MOCK_API}")
            val res = if (BuildConfig.USE_MOCK_API) MockApi.login(body) else AuthApi.login(body)
            applyAuth(res)
            Log.d(SESSION_LOG_TAG, "Login succeeded email=${res.email} username=${res.username} clntId=${res.clntId}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(SESSION_LOG_TAG, "Login failed: ${e.javaClass.simpleName}: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun register(body: RegisterRequest): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(SESSION_LOG_TAG, "Register started name=${body.name} username=${body.username} email=${body.email} mock=${BuildConfig.USE_MOCK_API}")
            val res = if (BuildConfig.USE_MOCK_API) MockApi.register(body) else AuthApi.register(body)
            applyAuth(res)
            Log.d(SESSION_LOG_TAG, "Register succeeded email=${res.email} username=${res.username} clntId=${res.clntId}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(SESSION_LOG_TAG, "Register failed: ${e.javaClass.simpleName}: ${e.message}", e)
            Result.failure(e)
        }
    }

    private suspend fun applyAuth(res: AuthResponse) {
        val user = res.user
        withContext(Dispatchers.Main) {
            _jwt.value = res.accessToken ?: res.token
            _email.value = user?.email ?: res.email
            _username.value = user?.username ?: res.username
            _name.value = user?.name ?: res.name
            _clntId.value = user?.clntId ?: res.clntId
            _balance.value = user?.balance ?: res.balance
        }
        persist()
        Log.d(SESSION_LOG_TAG, "Applied auth tokenPresent=${_jwt.value != null} email=${_email.value} username=${_username.value} name=${_name.value} clntId=${_clntId.value}")
        PortfolioStore.seedIfEmpty()
        QuotesHub.onSessionChanged()
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        val j = _jwt.value
        if (j != null) {
            try {
                AuthApi.logout()
            } catch (_: Exception) {
            }
        }
        withContext(Dispatchers.Main) {
            _jwt.value = null
            _email.value = null
            _username.value = null
            _name.value = null
            _clntId.value = null
            _balance.value = BrokerData.repository.cashRub
        }
        persist()
        QuotesHub.onSessionChanged()
    }

    fun setBalance(value: Double) {
        _balance.value = value
        scope.launch(Dispatchers.IO) { persist() }
    }

    suspend fun refreshMe() = withContext(Dispatchers.IO) {
        if (_jwt.value == null) return@withContext
        try {
            val currentEmail = _email.value ?: return@withContext
            val me = if (BuildConfig.USE_MOCK_API) {
                MockApi.getMe(currentEmail, _username.value, _name.value, _balance.value)
            } else {
                UserApi.getMe()
            }
            withContext(Dispatchers.Main) {
                _email.value = me.email
                _username.value = me.username
                _name.value = me.name
                _clntId.value = me.clntId
                _balance.value = me.balance
            }
            persist()
        } catch (e: Exception) {
            throw e
        }
    }

    private val JWT_KEY = stringPreferencesKey("jwt")
    private val EMAIL_KEY = stringPreferencesKey("email")
    private val USERNAME_KEY = stringPreferencesKey("username")
    private val NAME_KEY = stringPreferencesKey("name")
    private val CLNT_ID_KEY = intPreferencesKey("clnt_id")
    private val BALANCE_KEY = doublePreferencesKey("balance")
}
