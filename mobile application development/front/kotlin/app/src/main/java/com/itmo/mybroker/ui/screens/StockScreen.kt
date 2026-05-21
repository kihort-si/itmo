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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.itmo.mybroker.data.Ccy
import com.itmo.mybroker.core.QuotesHub
import com.itmo.mybroker.data.BrokerData
import com.itmo.mybroker.data.TradeSide
import com.itmo.mybroker.format.Fmt
import com.itmo.mybroker.i18n.strings
import com.itmo.mybroker.ui.components.MbIconButton
import com.itmo.mybroker.ui.components.ProChart
import com.itmo.mybroker.ui.components.TopBar
import com.itmo.mybroker.ui.icons.Ic
import com.itmo.mybroker.ui.theme.ChartKind
import com.itmo.mybroker.ui.theme.Lang
import com.itmo.mybroker.ui.theme.LocalPalette
import com.itmo.mybroker.ui.theme.LocalTweaks

@Composable
fun StockScreen(
    sym: String,
    onBack: () -> Unit,
    onTrade: (String, TradeSide) -> Unit,
    favorites: Set<String>,
    onToggleFavorite: (String) -> Unit,
) {
    val palette = LocalPalette.current
    val t = strings()
    val tweaks = LocalTweaks.current.value
    val lang = tweaks.lang

    val ticker = remember(sym) { BrokerData.repository.bySym(sym) }
    var period by remember { mutableStateOf("1m") }
    var kind by remember { mutableStateOf(tweaks.defaultChartKind) }
    var showVol by remember { mutableStateOf(true) }
    var showMA by remember { mutableStateOf(true) }
    var showRSI by remember { mutableStateOf(false) }

    val candles = remember(sym, period) {
        val tfMap = mapOf("1d" to "5m", "1w" to "30m", "1m" to "1d", "6m" to "1d", "1y" to "1w", "all" to "1w")
        val realTf = tfMap[period] ?: "1d"
        val n = mapOf("1d" to 78, "1w" to 80, "1m" to 90, "6m" to 130, "1y" to 60, "all" to 240)[period] ?: 120
        BrokerData.repository.genCandles(ticker, realTf, n)
    }
    val last = candles.last()
    val first = candles.first()
    val change = last.c - first.c
    val changePct = change / first.c * 100
    val day = remember(sym) { BrokerData.repository.dayStats(ticker) }
    val quotes by QuotesHub.quotes.collectAsState(initial = emptyMap())
    val live = quotes[sym]
    val heroPrice = live?.price ?: last.c
    val heroChange = live?.change ?: change
    val heroChangePct = live?.changePct ?: changePct
    val up = heroChangePct >= 0
    val dpHero = if (heroPrice > 1000) 1 else 2

    DisposableEffect(sym) {
        QuotesHub.subscribe(sym)
        onDispose { QuotesHub.unsubscribe(sym) }
    }

    Box(modifier = Modifier.fillMaxSize().background(palette.bg)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            TopBar(
                title = ticker.sym,
                subtitle = if (lang == Lang.Ru) ticker.nameRu else ticker.nameEn,
                left = { MbIconButton(onClick = onBack) { Icon(Ic.Back, contentDescription = "back", tint = palette.text) } },
                right = {
                    MbIconButton(onClick = { onToggleFavorite(sym) }) {
                        Icon(
                            imageVector = if (favorites.contains(sym)) Ic.StarSolid else Ic.Star,
                            contentDescription = "favorite",
                            tint = if (favorites.contains(sym)) palette.accent else palette.text,
                        )
                    }
                },
            )

            Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 12.dp)) {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        Fmt.num(heroPrice, dpHero),
                        color = palette.text,
                        style = MaterialTheme.typography.displayLarge,
                    )
                    Text(
                        if (ticker.ccy == Ccy.RUB) "₽" else "$",
                        color = palette.textMute,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val color = if (up) palette.up else palette.down
                    Text(if (up) "▲" else "▼", color = color, style = MaterialTheme.typography.titleSmall)
                    Text(Fmt.signed(heroChange, 2), color = color, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.titleSmall)
                    Text(Fmt.pct(heroChangePct), color = color, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = mapOf(
                            "1d" to t.period1d, "1w" to t.period1w, "1m" to t.period1m,
                            "6m" to t.period6m, "1y" to t.period1y, "all" to t.periodAll
                        )[period] ?: "",
                        color = palette.textDim,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(palette.surface2)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }

            Box(modifier = Modifier.padding(horizontal = 4.dp)) {
                ProChart(
                    candles = candles,
                    ticker = ticker,
                    kind = kind,
                    showMA = showMA,
                    showVol = showVol,
                    showRSI = showRSI,
                    height = (if (showRSI) 296 else 240).dp,
                )
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                val periods = listOf("1d" to t.period1d, "1w" to t.period1w, "1m" to t.period1m, "6m" to t.period6m, "1y" to t.period1y, "all" to t.periodAll)
                items(periods) { (v, label) ->
                    PeriodButton(label, period == v) { period = v }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(palette.surface)
                        .border(1.dp, palette.hairline, RoundedCornerShape(10.dp))
                        .padding(2.dp),
                ) {
                    KindOpt(label = t.chartLine, icon = Ic.ChartLine, on = kind == ChartKind.Line) { kind = ChartKind.Line }
                    KindOpt(label = t.chartCandles, icon = Ic.ChartCandles, on = kind == ChartKind.Candles) { kind = ChartKind.Candles }
                }
                Spacer(Modifier.weight(1f))
                StudyChip("${t.ma}20", showMA) { showMA = !showMA }
                StudyChip(t.vol, showVol) { showVol = !showVol }
                StudyChip(t.rsi, showRSI) { showRSI = !showRSI }
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StatsRow(
                    Stat(t.open, Fmt.num(day.candles.last().o, if (ticker.base > 1000) 1 else 2)),
                    Stat(t.high, Fmt.num(day.candles.last().h, if (ticker.base > 1000) 1 else 2)),
                )
                StatsRow(
                    Stat(t.low, Fmt.num(day.candles.last().l, if (ticker.base > 1000) 1 else 2)),
                    Stat(t.prevClose, Fmt.num(day.candles[day.candles.size - 2].c, if (ticker.base > 1000) 1 else 2)),
                )
                StatsRow(
                    Stat(t.volume, Fmt.vol(day.candles.last().v)),
                    Stat(t.bid, Fmt.num(last.c - (if (last.c > 100) 0.05 else 0.01), 2), valueColor = palette.down),
                )
                StatsRow(
                    Stat(t.ask, Fmt.num(last.c + (if (last.c > 100) 0.05 else 0.01), 2), valueColor = palette.up),
                    Stat(t.spread, Fmt.num(if (last.c > 100) 0.10 else 0.02, 2)),
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
                    .padding(horizontal = 16.dp, vertical = 4.dp),
            ) {
                AboutRow(t.sector, if (lang == Lang.Ru) ticker.sectorRu else ticker.sectorEn)
                AboutRow(t.exchangeLabel, ticker.market.name)
                AboutRow(t.currency, ticker.ccy.name)
                AboutRow(t.lot, "1")
            }

            Text(
                t.riskWarning,
                color = palette.textDim,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            )

            Spacer(Modifier.height(96.dp))
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(palette.bg)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CtaButton(
                label = t.sell,
                price = Fmt.num(last.c - (if (last.c > 100) 0.05 else 0.01), 2),
                bg = palette.downSoft,
                fg = palette.down,
                modifier = Modifier.weight(1f),
                onClick = { onTrade(sym, TradeSide.Sell) },
            )
            CtaButton(
                label = t.buy,
                price = Fmt.num(last.c + (if (last.c > 100) 0.05 else 0.01), 2),
                bg = palette.accent,
                fg = palette.accentFg,
                modifier = Modifier.weight(1f),
                onClick = { onTrade(sym, TradeSide.Buy) },
            )
        }
    }
}

@Composable
private fun PeriodButton(label: String, on: Boolean, onClick: () -> Unit) {
    val palette = LocalPalette.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (on) palette.surface3 else androidx.compose.ui.graphics.Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = if (on) palette.text else palette.textMute, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun KindOpt(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, on: Boolean, onClick: () -> Unit) {
    val palette = LocalPalette.current
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (on) palette.surface3 else androidx.compose.ui.graphics.Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = if (on) palette.text else palette.textMute, modifier = Modifier.size(14.dp))
        Text(label, color = if (on) palette.text else palette.textMute, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun StudyChip(label: String, on: Boolean, onClick: () -> Unit) {
    val palette = LocalPalette.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (on) palette.accentSoft else palette.surface)
            .border(1.dp, if (on) palette.accent.copy(alpha = 0.4f) else palette.hairline, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = if (on) palette.accent else palette.textMute, style = MaterialTheme.typography.labelLarge)
    }
}

private data class Stat(val label: String, val value: String, val valueColor: Color? = null)

@Composable
private fun StatsRow(left: Stat, right: Stat) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatCell(left, Modifier.weight(1f))
        StatCell(right, Modifier.weight(1f))
    }
}

@Composable
private fun StatCell(s: Stat, modifier: Modifier = Modifier) {
    val palette = LocalPalette.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(palette.surface)
            .border(1.dp, palette.hairline, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(s.label.uppercase(), color = palette.textMute, style = MaterialTheme.typography.labelSmall)
        Text(
            s.value,
            color = s.valueColor ?: palette.text,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun AboutRow(label: String, value: String) {
    val palette = LocalPalette.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = palette.textMute, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.weight(1f))
        Text(value, color = palette.text, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun CtaButton(label: String, price: String, bg: Color, fg: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = fg, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(price, color = fg, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.titleMedium)
    }
}
