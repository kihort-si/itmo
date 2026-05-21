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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itmo.mybroker.data.Ccy
import com.itmo.mybroker.data.MarketId
import com.itmo.mybroker.core.QuotesHub
import com.itmo.mybroker.data.BrokerData
import com.itmo.mybroker.data.Ticker
import com.itmo.mybroker.format.Fmt
import com.itmo.mybroker.i18n.strings
import com.itmo.mybroker.ui.components.MbIconButton
import com.itmo.mybroker.ui.components.Sparkline
import com.itmo.mybroker.ui.components.TopBar
import com.itmo.mybroker.ui.icons.Ic
import com.itmo.mybroker.ui.theme.Lang
import com.itmo.mybroker.ui.theme.LocalDensityTokens
import com.itmo.mybroker.ui.theme.LocalPalette
import com.itmo.mybroker.ui.theme.LocalTweaks
import kotlin.math.abs

private enum class Sort { Name, Price, Change }

@Composable
fun ExchangeScreen(
    favorites: Set<String>,
    onToggleFavorite: (String) -> Unit,
    onOpenStock: (String) -> Unit,
    onOpenTweaks: () -> Unit,
) {
    val palette = LocalPalette.current
    val t = strings()
    val lang = LocalTweaks.current.value.lang
    val density = LocalDensityTokens.current

    var search by remember { mutableStateOf("") }
    var sort by remember { mutableStateOf(Sort.Change) }
    var sortAsc by remember { mutableStateOf(false) }
    var filter by remember { mutableStateOf("all") }

    val quotes by QuotesHub.quotes.collectAsState(initial = emptyMap())

    val enriched = remember(quotes) {
        BrokerData.repository.tickers.map { tk ->
            val st = BrokerData.repository.dayStats(tk)
            val q = quotes[tk.sym]
            tk to if (q != null) {
                st.copy(price = q.price, change = q.change, changePct = q.changePct, spark = q.spark)
            } else st
        }
    }

    DisposableEffect(Unit) {
        BrokerData.repository.tickers.forEach { QuotesHub.subscribe(it.sym) }
        onDispose {
            BrokerData.repository.tickers.forEach { QuotesHub.unsubscribe(it.sym) }
        }
    }

    val filtered = remember(quotes, search, sort, sortAsc, filter, favorites) {
            var r = enriched
            val q = search.trim().lowercase()
            if (q.isNotEmpty()) {
                r = r.filter { (tk, _) ->
                    tk.sym.lowercase().contains(q) ||
                        tk.nameRu.lowercase().contains(q) ||
                        tk.nameEn.lowercase().contains(q)
                }
            }
            r = when (filter) {
                "moex" -> r.filter { it.first.market == MarketId.MOEX }
                "nasdaq" -> r.filter { it.first.market == MarketId.NASDAQ }
                "favs" -> r.filter { favorites.contains(it.first.sym) }
                else -> r
            }
            val dir = if (sortAsc) 1 else -1
            r.sortedWith(Comparator { a, b ->
                val cmp = when (sort) {
                    Sort.Price -> {
                        val pa = if (a.first.ccy == Ccy.USD) a.second.price * BrokerData.repository.fx.USD_RUB else a.second.price
                        val pb = if (b.first.ccy == Ccy.USD) b.second.price * BrokerData.repository.fx.USD_RUB else b.second.price
                        pa.compareTo(pb)
                    }
                    Sort.Name -> a.first.sym.compareTo(b.first.sym)
                    Sort.Change -> a.second.changePct.compareTo(b.second.changePct)
                }
                cmp * dir
            })
    }

    val gainers = remember(quotes) { enriched.count { it.second.changePct > 0 } }
    val losers = enriched.size - gainers

    LazyColumn(
        modifier = Modifier.fillMaxWidth().background(palette.bg),
        contentPadding = PaddingValues(bottom = 100.dp),
    ) {
        item {
            TopBar(
                title = "MyBroker",
                subtitle = "${BrokerData.repository.tickers.size} ${t.instruments}",
                large = true,
                right = {
                    MbIconButton(onClick = onOpenTweaks) {
                        Icon(Ic.Filter, contentDescription = "tweaks", tint = palette.text)
                    }
                },
            )
        }
        item {

            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(palette.surface)
                    .border(1.dp, palette.hairline, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Ic.Search, contentDescription = null, tint = palette.textMute, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Box(modifier = Modifier.weight(1f)) {
                    BasicTextField(
                        value = search,
                        onValueChange = { search = it },
                        textStyle = TextStyle(color = palette.text, fontSize = 15.sp),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(palette.accent),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (search.isEmpty()) {
                        Text(t.searchPlaceholder, color = palette.textDim, style = MaterialTheme.typography.bodyLarge)
                    }
                }
                if (search.isNotEmpty()) {
                    MbIconButton(onClick = { search = "" }) {
                        Icon(Ic.Close, contentDescription = "clear", tint = palette.textMute, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
        item { Spacer(Modifier.height(14.dp)) }
        item { PulseStrip(gainers, losers) }
        item { Spacer(Modifier.height(12.dp)) }
        item {

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item { Chip(t.filterAll, filter == "all") { filter = "all" } }
                item { Chip("MOEX", filter == "moex") { filter = "moex" } }
                item { Chip("NASDAQ", filter == "nasdaq") { filter = "nasdaq" } }
                item { Chip(t.filterFavs, filter == "favs", leading = { Icon(Ic.Star, contentDescription = null, tint = if (filter == "favs") palette.textInv else palette.textMute, modifier = Modifier.size(13.dp)) }) { filter = "favs" } }
            }
        }
        item { Spacer(Modifier.height(14.dp)) }
        item { ListHeader(sort, sortAsc, onSortChange = { ns ->
            if (ns == sort) sortAsc = !sortAsc
            else { sort = ns; sortAsc = (ns == Sort.Name) }
        }) }

        items(filtered, key = { it.first.sym }) { (tk, st) ->
            StockRow(
                ticker = tk,
                price = st.price,
                changePct = st.changePct,
                change = st.change,
                spark = st.spark,
                lang = lang,
                fav = favorites.contains(tk.sym),
                rowPadX = density.rowPadX,
                rowPadY = density.rowPadY,
                minHeight = density.rowMinHeight,
                onClick = { onOpenStock(tk.sym) },
            )
        }
        if (filtered.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(60.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("∅", color = palette.textDim, style = MaterialTheme.typography.headlineLarge)
                        Spacer(Modifier.height(8.dp))
                        Text(t.nothingFound, color = palette.textMute, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun PulseStrip(gainers: Int, losers: Int) {
    val palette = LocalPalette.current
    val t = strings()
    val indices = BrokerData.repository.indices
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(palette.surface)
            .border(1.dp, palette.hairline, RoundedCornerShape(14.dp))
            .padding(vertical = 12.dp),
    ) {
        PulseCell(t.gainers, gainers.toString(), palette.up, Modifier.weight(1f))
        PulseDivider()
        PulseCell(t.losers, losers.toString(), palette.down, Modifier.weight(1f))
        PulseDivider()
        PulseCell(indices[0].label, indices[0].value, palette.text, Modifier.weight(1f), monoValue = true)
        PulseDivider()
        PulseCell(indices[1].label, indices[1].value, palette.text, Modifier.weight(1f), monoValue = true)
    }
}

@Composable
private fun PulseCell(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color, modifier: Modifier, monoValue: Boolean = false) {
    val palette = LocalPalette.current
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label.uppercase(), color = palette.textMute, style = MaterialTheme.typography.labelSmall)
        Text(
            value,
            color = valueColor,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            fontFamily = if (monoValue) FontFamily.Monospace else FontFamily.Default,
        )
    }
}

@Composable
private fun PulseDivider() {
    val palette = LocalPalette.current
    Box(modifier = Modifier.width(1.dp).height(28.dp).background(palette.hairline))
}

@Composable
private fun Chip(label: String, on: Boolean, leading: (@Composable () -> Unit)? = null, onClick: () -> Unit) {
    val palette = LocalPalette.current
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(if (on) palette.text else palette.surface)
            .border(1.dp, if (on) palette.text else palette.hairline, RoundedCornerShape(100.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        leading?.invoke()
        Text(label, color = if (on) palette.textInv else palette.textMute, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ListHeader(sort: Sort, asc: Boolean, onSortChange: (Sort) -> Unit) {
    val palette = LocalPalette.current
    val t = strings()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(width = 0.dp, color = androidx.compose.ui.graphics.Color.Transparent),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SortLabel(t.sortName, sort == Sort.Name, asc, Modifier.weight(1f)) { onSortChange(Sort.Name) }
        SortLabel(t.sortPrice, sort == Sort.Price, asc, Modifier.width(96.dp), trailingAlign = true) { onSortChange(Sort.Price) }
        SortLabel(t.sortChange, sort == Sort.Change, asc, Modifier.width(78.dp), trailingAlign = true) { onSortChange(Sort.Change) }
    }
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(palette.hairline))
}

@Composable
private fun SortLabel(label: String, active: Boolean, asc: Boolean, modifier: Modifier = Modifier, trailingAlign: Boolean = false, onClick: () -> Unit) {
    val palette = LocalPalette.current
    Row(
        modifier = modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (trailingAlign) Arrangement.End else Arrangement.Start,
    ) {
        Text(label.uppercase(), color = if (active) palette.text else palette.textDim, style = MaterialTheme.typography.labelSmall)
        if (active) {
            Icon(
                imageVector = if (asc) Ic.Up else Ic.Down,
                contentDescription = null,
                tint = palette.text,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Composable
private fun StockRow(
    ticker: Ticker,
    price: Double,
    changePct: Double,
    change: Double,
    spark: List<Double>,
    lang: Lang,
    fav: Boolean,
    rowPadX: androidx.compose.ui.unit.Dp,
    rowPadY: androidx.compose.ui.unit.Dp,
    minHeight: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
) {
    val palette = LocalPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .clickable(onClick = onClick)
            .padding(horizontal = rowPadX, vertical = rowPadY),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {

        val isMoex = ticker.market == MarketId.MOEX
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
            Text(ticker.sym.take(2), color = tint, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(ticker.sym, color = palette.text, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(palette.surface2)
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(ticker.market.name, color = palette.textDim, style = MaterialTheme.typography.labelSmall)
                }
                if (fav) Icon(Ic.StarSolid, contentDescription = null, tint = palette.textDim, modifier = Modifier.size(11.dp))
            }
            Text(
                if (lang == Lang.Ru) ticker.nameRu else ticker.nameEn,
                color = palette.textMute,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
            )
        }

        Sparkline(
            data = spark,
            color = if (changePct >= 0) palette.up else palette.down,
            width = 56.dp,
            height = 28.dp,
            fill = true,
        )

        Column(horizontalAlignment = Alignment.End, modifier = Modifier.width(64.dp)) {
            Text(Fmt.num(price, if (price > 1000) 1 else 2), color = palette.text, style = MaterialTheme.typography.titleSmall, fontFamily = FontFamily.Monospace)
            Text(if (ticker.ccy == Ccy.RUB) "₽" else "$", color = palette.textDim, style = MaterialTheme.typography.labelSmall)
        }

        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (changePct >= 0) palette.upSoft else palette.downSoft)
                .padding(horizontal = 6.dp, vertical = 4.dp)
                .width(74.dp),
            horizontalAlignment = Alignment.End,
        ) {
            val color = if (changePct >= 0) palette.up else palette.down
            Text(Fmt.pct(changePct), color = color, style = MaterialTheme.typography.titleSmall, fontFamily = FontFamily.Monospace)
            Text(Fmt.signed(change, if (abs(change) > 100) 1 else 2), color = color.copy(alpha = 0.75f), fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall)
        }
    }
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(palette.hairline))
}
