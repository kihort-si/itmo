package com.itmo.mybroker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itmo.mybroker.data.Ccy
import com.itmo.mybroker.data.MarketId
import com.itmo.mybroker.core.QuotesHub
import com.itmo.mybroker.core.SessionStore
import com.itmo.mybroker.data.BrokerData
import com.itmo.mybroker.service.TradingService
import kotlin.math.roundToInt
import com.itmo.mybroker.data.TradeSide
import com.itmo.mybroker.format.Fmt
import com.itmo.mybroker.i18n.strings
import com.itmo.mybroker.ui.components.MbIconButton
import com.itmo.mybroker.ui.components.Sparkline
import com.itmo.mybroker.ui.components.TopBar
import com.itmo.mybroker.ui.icons.Ic
import com.itmo.mybroker.ui.theme.Lang
import com.itmo.mybroker.ui.theme.LocalPalette
import com.itmo.mybroker.ui.theme.LocalTweaks

@Composable
fun PortfolioScreen(
    isAuthed: Boolean,
    onOpenStock: (String) -> Unit,
    onOpenHistory: () -> Unit,
    onSignIn: () -> Unit,
    onOpenTweaks: () -> Unit,
) {
    val palette = LocalPalette.current
    val t = strings()
    val tweaks = LocalTweaks.current.value
    val lang = tweaks.lang
    val cashRub by SessionStore.balance.collectAsState(initial = BrokerData.repository.cashRub)
    val quotes by QuotesHub.quotes.collectAsState(initial = emptyMap())

    if (!isAuthed) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(palette.bg),
        ) {
            TopBar(title = t.portfolio, large = true)
            AuthPrompt(
                icon = { Icon(Ic.Briefcase, contentDescription = null, tint = palette.accent, modifier = Modifier.size(48.dp)) },
                title = t.authPromptTitle,
                sub = t.authPromptSub,
                ctaLabel = t.signIn,
                onCta = onSignIn,
            )
        }
        return
    }

    var currency by remember { mutableStateOf("RUB") }

    var baseRows by remember { mutableStateOf<List<com.itmo.mybroker.service.PortfolioUiRow>>(emptyList()) }
    LaunchedEffect(isAuthed) {
        if (!isAuthed) {
            baseRows = emptyList()
            return@LaunchedEffect
        }
        baseRows = TradingService.loadPortfolioRows()
    }

    val enriched = remember(baseRows, quotes) {
        val merged = TradingService.mergeWithLive(baseRows, quotes)
        merged.map { r ->
            HoldingRow(
                sym = r.sym,
                qty = r.qty.roundToInt().coerceAtLeast(1),
                market = r.market,
                nameRu = r.nameRu,
                nameEn = r.nameEn,
                valueRub = r.valueRub,
                pnlRub = r.pnlRub,
                pnlPct = r.pnlPct,
            )
        }
    }

    val totalRub = enriched.sumOf { it.valueRub } + cashRub
    val totalPnlRub = enriched.sumOf { it.pnlRub }
    val totalInvestedRub = enriched.sumOf { it.valueRub - it.pnlRub } + cashRub
    val totalPnlPct = totalPnlRub / totalInvestedRub * 100

    val ccyRate = when (currency) {
        "RUB" -> 1.0
        "USD" -> BrokerData.repository.fx.RUB_USD
        "EUR" -> BrokerData.repository.fx.RUB_EUR
        else -> 1.0
    }
    val displayCcy = when (currency) { "USD" -> Ccy.USD; "EUR" -> Ccy.EUR; else -> Ccy.RUB }
    val totalDisp = totalRub * ccyRate
    val cashDisp = cashRub * ccyRate
    val totalPnlDisp = totalPnlRub * ccyRate

    val portSpark = remember(enriched, cashRub) {
        val n = 60
        val arr = DoubleArray(n)
        for (h in enriched) {
            val tk = BrokerData.repository.bySym(h.sym)
            val c = BrokerData.repository.genCandles(tk, "1d", n)
            for (i in 0 until n) {
                arr[i] += c[i].c * h.qty * (if (tk.ccy == Ccy.USD) BrokerData.repository.fx.USD_RUB else 1.0)
            }
        }
        arr.map { it + cashRub }
    }

    DisposableEffect(enriched.map { it.sym }.joinToString()) {
        val syms = enriched.map { it.sym }
        syms.forEach { QuotesHub.subscribe(it) }
        onDispose {
            syms.forEach { QuotesHub.unsubscribe(it) }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(palette.bg),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            TopBar(
                title = t.portfolio,
                large = true,
                right = {
                    MbIconButton(onClick = onOpenTweaks) {
                        Icon(Ic.Bell, contentDescription = "tweaks", tint = palette.text)
                    }
                },
            )
        }

        item {

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(palette.surface)
                    .border(1.dp, palette.hairline, RoundedCornerShape(18.dp))
                    .padding(20.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(t.totalValue.uppercase(), color = palette.textMute, style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.weight(1f))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(palette.surface2)
                            .border(1.dp, palette.hairline, RoundedCornerShape(100.dp))
                            .clickable {
                                currency = when (currency) { "RUB" -> "USD"; "USD" -> "EUR"; else -> "RUB" }
                            }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(Ic.Swap, contentDescription = null, tint = palette.textMute, modifier = Modifier.size(12.dp))
                        Text(currency, color = palette.text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    Fmt.money(totalDisp, displayCcy, dp = if (totalDisp > 100000) 0 else 2),
                    color = palette.text,
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp),
                    fontFamily = FontFamily.Monospace,
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val color = if (totalPnlRub >= 0) palette.up else palette.down
                    Text(Fmt.signed(totalPnlDisp, if (totalPnlDisp > 1000) 0 else 2), color = color, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.titleSmall)
                    Text(Fmt.pct(totalPnlPct), color = color, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.titleSmall)
                    Text(t.allTime, color = palette.textDim, style = MaterialTheme.typography.labelSmall)
                }
                Spacer(Modifier.height(12.dp))
                Sparkline(
                    data = portSpark,
                    color = if (totalPnlRub >= 0) palette.up else palette.down,
                    fill = true,
                    width = 320.dp,
                    height = 56.dp,
                )
            }
        }

        item {
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(palette.surface)
                    .border(1.dp, palette.hairline, RoundedCornerShape(14.dp))
                    .clickable(onClick = onOpenHistory)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(palette.surface2),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Ic.Briefcase, contentDescription = null, tint = palette.textMute, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(t.cashWallet, color = palette.textMute, style = MaterialTheme.typography.labelSmall)
                    Text(Fmt.money(cashDisp, displayCcy, dp = 0), color = palette.text, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.weight(1f))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(palette.accentSoft)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Ic.Add, contentDescription = null, tint = palette.accent, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(t.add, color = palette.accent, style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        item { SectionHead(t.holdings, enriched.size.toString()) }

        items(enriched, key = { it.sym }) { h ->
            HoldingItem(
                row = h,
                lang = lang,
                ccyRate = ccyRate,
                displayCcy = displayCcy,
                onClick = { onOpenStock(h.sym) },
            )
        }

        if (BrokerData.repository.activeOrders.isNotEmpty()) {
            item { SectionHead(t.activeOrders, BrokerData.repository.activeOrders.size.toString()) }
            items(BrokerData.repository.activeOrders, key = { it.id }) { o ->
                ActiveOrderRow(sym = o.sym, side = o.side, qty = o.qty, limit = o.limit, ccy = BrokerData.repository.bySym(o.sym).ccy)
            }
        }

        item {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(palette.surface)
                    .border(1.dp, palette.hairline, RoundedCornerShape(14.dp))
                    .clickable(onClick = onOpenHistory)
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(Ic.History, contentDescription = null, tint = palette.textMute, modifier = Modifier.size(20.dp))
                Text(t.historyTitle, color = palette.text, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.weight(1f))
                Icon(Ic.Right, contentDescription = null, tint = palette.textDim, modifier = Modifier.size(18.dp))
            }

            Text(
                t.riskWarning,
                color = palette.textDim,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            )
        }
    }
}

private data class HoldingRow(
    val sym: String,
    val qty: Int,
    val market: MarketId,
    val nameRu: String,
    val nameEn: String,
    val valueRub: Double,
    val pnlRub: Double,
    val pnlPct: Double,
)

@Composable
private fun HoldingItem(row: HoldingRow, lang: Lang, ccyRate: Double, displayCcy: Ccy, onClick: () -> Unit) {
    val palette = LocalPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        val isMoex = row.market == MarketId.MOEX
        val tint = if (isMoex) palette.moex else palette.nasdaq
        val bg = if (isMoex) palette.moexSoft else palette.nasdaqSoft
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(bg)
                .border(1.dp, tint.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(row.sym.take(2), color = tint, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(row.sym, color = palette.text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("×${row.qty}", color = palette.textMute, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall)
            }
            Text(if (lang == Lang.Ru) row.nameRu else row.nameEn, color = palette.textMute, style = MaterialTheme.typography.bodySmall, maxLines = 1)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(Fmt.money(row.valueRub * ccyRate, displayCcy, dp = 0), color = palette.text, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.titleSmall)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                val c = if (row.pnlRub >= 0) palette.up else palette.down
                Text(Fmt.signed(row.pnlRub * ccyRate, 0), color = c, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelLarge)
                Text("(${Fmt.pct(row.pnlPct, 1)})", color = palette.textMute, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun ActiveOrderRow(sym: String, side: TradeSide, qty: Int, limit: Double, ccy: Ccy) {
    val palette = LocalPalette.current
    val t = strings()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        val c = if (side == TradeSide.Buy) palette.up else palette.down
        val bg = if (side == TradeSide.Buy) palette.upSoft else palette.downSoft
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(bg)
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Text(if (side == TradeSide.Buy) t.typeBuy else t.typeSell, color = c, style = MaterialTheme.typography.labelSmall)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(sym, color = palette.text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text("$qty × ${Fmt.money(limit, ccy)}", color = palette.textMute, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
        }
        Icon(Ic.Close, contentDescription = "cancel", tint = palette.textMute, modifier = Modifier.size(14.dp))
    }
}

@Composable
private fun SectionHead(label: String, count: String? = null) {
    val palette = LocalPalette.current
    Row(
        modifier = Modifier
            .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 6.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label.uppercase(), color = palette.textMute, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.weight(1f))
        if (count != null) Text(count, color = palette.textDim, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun AuthPrompt(
    icon: @Composable () -> Unit,
    title: String,
    sub: String,
    ctaLabel: String,
    onCta: () -> Unit,
) {
    val palette = LocalPalette.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(palette.accentSoft),
            contentAlignment = Alignment.Center,
        ) { icon() }
        Text(title, color = palette.text, style = MaterialTheme.typography.titleLarge)
        Text(sub, color = palette.textMute, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(palette.accent)
                .clickable(onClick = onCta)
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(ctaLabel, color = palette.accentFg, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}
