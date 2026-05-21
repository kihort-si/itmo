package com.itmo.mybroker.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itmo.mybroker.data.Ccy
import com.itmo.mybroker.core.SessionStore
import com.itmo.mybroker.data.BrokerData
import com.itmo.mybroker.service.TradingService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.itmo.mybroker.data.TradeSide
import com.itmo.mybroker.format.Fmt
import com.itmo.mybroker.i18n.strings
import com.itmo.mybroker.ui.components.MbIconButton
import com.itmo.mybroker.ui.components.Segmented
import com.itmo.mybroker.ui.components.TopBar
import com.itmo.mybroker.ui.icons.Ic
import com.itmo.mybroker.ui.theme.Lang
import com.itmo.mybroker.ui.theme.LocalPalette
import com.itmo.mybroker.ui.theme.LocalTweaks
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradeScreen(
    sym: String,
    initialSide: TradeSide,
    isAuthed: Boolean,
    onBack: () -> Unit,
    onClose: () -> Unit,
    onPlaced: () -> Unit,
    onSignIn: () -> Unit,
) {
    val palette = LocalPalette.current
    val t = strings()
    val tweaks = LocalTweaks.current.value
    val lang = tweaks.lang
    val cashRub by SessionStore.balance.collectAsState(initial = BrokerData.repository.cashRub)

    val ticker = remember(sym) { BrokerData.repository.bySym(sym) }
    var side by remember { mutableStateOf(initialSide) }
    var orderType by remember { mutableStateOf("limit") }
    val day = remember(sym) { BrokerData.repository.dayStats(ticker) }
    val ob = remember(sym) { BrokerData.repository.genOrderbook(ticker) }
    val lastPrice = day.price
    val dp = if (lastPrice > 1000) 1 else 2
    val bestAsk = ob.asks[0].price
    val bestBid = ob.bids[0].price
    var limit by remember { mutableStateOf(if (side == TradeSide.Buy) bestAsk else bestBid) }
    var qty by remember { mutableStateOf(1) }
    var confirmOpen by remember { mutableStateOf(false) }

    LaunchedEffect(side) {
        limit = if (side == TradeSide.Buy) ob.asks[0].price else ob.bids[0].price
    }

    val ccyRate = if (ticker.ccy == Ccy.USD) BrokerData.repository.fx.USD_RUB else 1.0
    val totalCcy = limit * qty
    val totalRub = limit * ccyRate * qty
    val reserveRub = if (side == TradeSide.Buy) totalRub * 1.005 else 0.0
    val insufficient = side == TradeSide.Buy && reserveRub > cashRub
    val fee = totalCcy * 0.0005

    val tickStep = when {
        lastPrice > 1000 -> 0.5
        lastPrice > 100 -> 0.05
        else -> 0.01
    }
    fun adjLimit(d: Int) {
        limit = max(0.0, ((limit + d * tickStep) * 100).roundToLong().toDouble() / 100.0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.bg)
            .verticalScroll(rememberScrollState()),
    ) {
        TopBar(
            title = "${if (side == TradeSide.Buy) t.buy else t.sell} ${ticker.sym}",
            subtitle = if (lang == Lang.Ru) ticker.nameRu else ticker.nameEn,
            left = { MbIconButton(onClick = onBack) { Icon(Ic.Back, contentDescription = "back", tint = palette.text) } },
            right = { MbIconButton(onClick = onClose) { Icon(Ic.Close, contentDescription = "close", tint = palette.text) } },
        )

        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(palette.surface)
                .border(1.dp, palette.hairline, RoundedCornerShape(12.dp))
                .padding(4.dp),
        ) {
            SideTab(t.buy, side == TradeSide.Buy, palette.up, Modifier.weight(1f)) { side = TradeSide.Buy }
            SideTab(t.sell, side == TradeSide.Sell, palette.down, Modifier.weight(1f)) { side = TradeSide.Sell }
        }

        Spacer(Modifier.height(12.dp))

        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            Segmented(
                value = orderType,
                onChange = { orderType = it },
                options = listOf("market" to t.market, "limit" to t.limit),
            )
        }
        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(palette.surface)
                .border(1.dp, palette.hairline, RoundedCornerShape(14.dp))
                .padding(horizontal = 4.dp, vertical = 8.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
                Text("${t.bookPrice}, ${if (ticker.ccy == Ccy.RUB) "₽" else "$"}", color = palette.textDim, style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.weight(1f))
                Text(t.bookSize, color = palette.textDim, style = MaterialTheme.typography.labelSmall)
            }

            val askTop = ob.asks.take(6)
            val maxAsk = askTop.maxOf { it.size }.coerceAtLeast(1)
            for (a in askTop.reversed()) {
                BookRow(a.price, a.size, maxAsk, false, palette.downSoft, palette.down, dp) {
                    side = TradeSide.Buy; limit = a.price
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(t.bookMid, color = palette.textMute, style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.weight(1f))
                Text(Fmt.num(ob.mid, dp), color = palette.text, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.width(8.dp))
                Text("${t.spread} ${Fmt.num(ob.tick, 2)}", color = palette.textDim, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall)
            }

            val bidTop = ob.bids.take(6)
            val maxBid = bidTop.maxOf { it.size }.coerceAtLeast(1)
            for (b in bidTop) {
                BookRow(b.price, b.size, maxBid, true, palette.upSoft, palette.up, dp) {
                    side = TradeSide.Sell; limit = b.price
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(t.qty, color = palette.textMute, style = MaterialTheme.typography.labelSmall)
                Stepper(
                    value = qty.toString(),
                    onChange = { v -> qty = (v.toIntOrNull() ?: 1).coerceAtLeast(1) },
                    onDec = { if (qty > 1) qty-- },
                    onInc = { qty++ },
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(listOf(1, 5, 10, 50, 100)) { q ->
                        QtyChip(q.toString(), qty == q) { qty = q }
                    }
                }
            }

            if (orderType == "limit") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (side == TradeSide.Buy) t.upperPrice else t.lowerPrice, color = palette.textMute, style = MaterialTheme.typography.labelSmall)
                    Stepper(
                        value = "%.${dp}f".format(limit),
                        onChange = { v -> v.toDoubleOrNull()?.let { limit = it } },
                        onDec = { adjLimit(-1) },
                        onInc = { adjLimit(+1) },
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${t.bestBid}: ", color = palette.textMute, style = MaterialTheme.typography.labelSmall)
                            Text(Fmt.num(bestBid, dp), color = palette.down, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${t.bestAsk}: ", color = palette.textMute, style = MaterialTheme.typography.labelSmall)
                            Text(Fmt.num(bestAsk, dp), color = palette.up, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(palette.surface)
                    .border(1.dp, palette.hairline, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                SumRow(t.total, Fmt.money(totalCcy, ticker.ccy))
                SumRow(t.fee, Fmt.money(fee, ticker.ccy), dim = true)
                if (side == TradeSide.Buy) {
                    SumRow(t.estReserve, Fmt.money(reserveRub, Ccy.RUB, dp = 0))
                }
                SumRow(t.available, Fmt.money(cashRub, Ccy.RUB, dp = 0), color = if (insufficient) palette.down else null)
            }

            if (insufficient) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(palette.downSoft)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Ic.Lock, contentDescription = null, tint = palette.down, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(t.insufficient, color = palette.down, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            CtaBig(
                label = when {
                    !isAuthed -> t.signIn
                    side == TradeSide.Buy -> t.placeBuy
                    else -> t.placeSell
                },
                value = Fmt.money(totalCcy, ticker.ccy),
                bg = if (side == TradeSide.Buy) palette.accent else palette.downSoft,
                fg = if (side == TradeSide.Buy) palette.accentFg else palette.down,
                enabled = !insufficient,
                onClick = {
                    if (!isAuthed) onSignIn()
                    else if (!insufficient) confirmOpen = true
                },
            )
        }
        Spacer(Modifier.height(24.dp))
    }

    if (confirmOpen) {
        ConfirmSheet(
            side = side,
            qty = qty,
            limit = limit,
            ticker = ticker,
            onDismiss = { confirmOpen = false },
            onConfirmed = {
                confirmOpen = false
                onPlaced()
            },
        )
    }
}

@Composable
private fun SideTab(label: String, on: Boolean, accent: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val palette = LocalPalette.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (on) accent.copy(alpha = 0.18f) else androidx.compose.ui.graphics.Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = if (on) accent else palette.textMute, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun BookRow(
    price: Double, size: Int, maxSize: Int, isBid: Boolean,
    barColor: Color, textColor: Color, dp: Int, onClick: () -> Unit,
) {
    val palette = LocalPalette.current
    val fraction = (size.toFloat() / maxSize).coerceIn(0f, 1f)
    Box(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 6.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .height(20.dp)
                .background(barColor)
                .align(if (isBid) Alignment.CenterStart else Alignment.CenterEnd),
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(Fmt.num(price, dp), color = textColor, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.weight(1f))
            Text(size.toString(), color = palette.textMute, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun Stepper(value: String, onChange: (String) -> Unit, onDec: () -> Unit, onInc: () -> Unit) {
    val palette = LocalPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(palette.surface)
            .border(1.dp, palette.hairline, RoundedCornerShape(12.dp))
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clickable(onClick = onDec),
            contentAlignment = Alignment.Center,
        ) { Icon(Ic.Minus, contentDescription = null, tint = palette.text) }
        BasicTextField(
            value = value,
            onValueChange = onChange,
            textStyle = TextStyle(color = palette.text, fontSize = 18.sp, fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center),
            cursorBrush = SolidColor(palette.accent),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier = Modifier
                .size(48.dp)
                .clickable(onClick = onInc),
            contentAlignment = Alignment.Center,
        ) { Icon(Ic.Add, contentDescription = null, tint = palette.text) }
    }
}

@Composable
private fun QtyChip(label: String, on: Boolean, onClick: () -> Unit) {
    val palette = LocalPalette.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (on) palette.accentSoft else palette.surface)
            .border(1.dp, if (on) palette.accent.copy(alpha = 0.4f) else palette.hairline, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(label, color = if (on) palette.accent else palette.textMute, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun SumRow(label: String, value: String, dim: Boolean = false, color: Color? = null) {
    val palette = LocalPalette.current
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = palette.textMute, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.weight(1f))
        Text(
            value,
            color = color ?: if (dim) palette.textMute else palette.text,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.titleSmall,
        )
    }
}

@Composable
private fun CtaBig(label: String, value: String, bg: Color, fg: Color, enabled: Boolean, onClick: () -> Unit) {
    val palette = LocalPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (enabled) bg else palette.surface3)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = if (enabled) fg else palette.textDim, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))
        Text(value, color = if (enabled) fg else palette.textDim, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.titleMedium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfirmSheet(
    side: TradeSide,
    qty: Int,
    limit: Double,
    ticker: com.itmo.mybroker.data.Ticker,
    onDismiss: () -> Unit,
    onConfirmed: () -> Unit,
) {
    val palette = LocalPalette.current
    val t = strings()
    val lang = LocalTweaks.current.value.lang
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var localSent by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = palette.surface,
        contentColor = palette.text,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (!localSent) {
                Text(t.confirmTitle, style = MaterialTheme.typography.titleLarge, color = palette.text)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(palette.surface2)
                        .border(1.dp, palette.hairline, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ConfirmRow(if (lang == Lang.Ru) "Действие" else "Action", if (side == TradeSide.Buy) t.typeBuy else t.typeSell, valueColor = if (side == TradeSide.Buy) palette.up else palette.down)
                    ConfirmRow(if (lang == Lang.Ru) "Инструмент" else "Instrument", "${ticker.sym} · ${ticker.market.name}")
                    ConfirmRow(t.qty, qty.toString())
                    ConfirmRow(if (side == TradeSide.Buy) t.upperPrice else t.lowerPrice, Fmt.money(limit, ticker.ccy))
                    ConfirmRow(t.total, Fmt.money(limit * qty, ticker.ccy), big = true)
                }
                HoldButton(
                    label = if (side == TradeSide.Buy) t.confirmBuy else t.confirmSell,
                    bg = if (side == TradeSide.Buy) palette.accent else palette.down,
                    fg = if (side == TradeSide.Buy) palette.accentFg else palette.textInv,
                    onComplete = {
                        scope.launch {
                            try {
                                withContext(Dispatchers.IO) {
                                    TradingService.executeTrade(
                                        ticker.sym,
                                        qty.toDouble(),
                                        if (side == TradeSide.Buy) "buy" else "sell",
                                    )
                                }
                                localSent = true
                            } catch (_: Exception) {
                                onDismiss()
                            }
                        }
                    },
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onDismiss)
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(t.cancel, color = palette.textMute, style = MaterialTheme.typography.titleMedium)
                }
            } else {
                LaunchedEffect(Unit) {
                    delay(1100)
                    onConfirmed()
                }
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(50))
                            .background(palette.accentSoft),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Ic.Check, contentDescription = null, tint = palette.accent, modifier = Modifier.size(36.dp))
                    }
                    Text(t.sentTitle, color = palette.text, style = MaterialTheme.typography.titleLarge)
                    Text(
                        if (side == TradeSide.Buy) t.sentSubBuy else t.sentSubSell,
                        color = palette.textMute,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(palette.surface2)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("${ticker.sym} · $qty × ${Fmt.money(limit, ticker.ccy)}", color = palette.text, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.titleSmall)
                        Text("= ${Fmt.money(limit * qty, ticker.ccy)}", color = palette.textMute, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ConfirmRow(label: String, value: String, valueColor: Color? = null, big: Boolean = false) {
    val palette = LocalPalette.current
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = palette.textMute, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.weight(1f))
        Text(
            value,
            color = valueColor ?: palette.text,
            fontFamily = FontFamily.Monospace,
            style = if (big) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun HoldButton(label: String, bg: Color, fg: Color, onComplete: () -> Unit) {
    val palette = LocalPalette.current
    val progress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(palette.surface3)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        scope.launch {
                            progress.animateTo(1f, animationSpec = tween(900))
                            if (progress.value >= 0.999f) onComplete()
                        }
                        val released = tryAwaitRelease()
                        if (!released || progress.value < 0.999f) {
                            scope.launch { progress.animateTo(0f, animationSpec = tween(180)) }
                        }
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.value)
                .height(56.dp)
                .background(bg),
        )
        Text(
            text = if (progress.value > 0 && progress.value < 1) "$label …" else label,
            color = fg,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

