package com.itmo.mybroker.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.itmo.mybroker.data.BrokerData
import com.itmo.mybroker.data.OpStatus
import com.itmo.mybroker.data.OpType
import com.itmo.mybroker.data.Operation
import com.itmo.mybroker.format.Fmt
import com.itmo.mybroker.i18n.strings
import com.itmo.mybroker.ui.components.MbIconButton
import com.itmo.mybroker.ui.components.TopBar
import com.itmo.mybroker.ui.icons.Ic
import com.itmo.mybroker.ui.theme.Lang
import com.itmo.mybroker.ui.theme.LocalPalette
import com.itmo.mybroker.ui.theme.LocalTweaks
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun HistoryScreen(onBack: () -> Unit) {
    val palette = LocalPalette.current
    val t = strings()
    val lang = LocalTweaks.current.value.lang

    val groups = remember {
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
        BrokerData.repository.history.groupBy { df.format(Date(it.t)) }
            .toList()
            .sortedByDescending { it.first }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(palette.bg),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            TopBar(
                title = t.historyTitle,
                left = { MbIconButton(onClick = onBack) { Icon(Ic.Back, contentDescription = "back", tint = palette.text) } },
            )
        }
        for ((key, items) in groups) {
            item {
                Text(
                    dayLabel(key, lang),
                    color = palette.textMute,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 6.dp),
                )
            }
            items.forEach { op ->
                item(key = op.id) { HistoryRow(op = op, lang = lang) }
            }
        }
    }
}

@Composable
private fun HistoryRow(op: Operation, lang: Lang) {
    val palette = LocalPalette.current
    val t = strings()
    val isBuy = op.type == OpType.Buy
    val isSell = op.type == OpType.Sell
    val fail = op.status == OpStatus.Cancelled || op.status == OpStatus.Rejected
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {

        val (bg, fg, iconVec) = when {
            isBuy -> Triple(palette.upSoft, palette.up, Ic.Up)
            isSell -> Triple(palette.downSoft, palette.down, Ic.Down)
            else -> Triple(palette.surface2, palette.textMute, Ic.Close)
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(bg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(iconVec, contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = when (op.type) {
                        OpType.Buy -> t.typeBuy
                        OpType.Sell -> t.typeSell
                        OpType.Cancel -> t.typeCancel
                        OpType.Reject -> t.typeReject
                    },
                    color = palette.text,
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(op.sym, color = palette.textMute, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelMedium)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(op.qty.toString(), color = palette.text, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
                Text("×", color = palette.textMute, style = MaterialTheme.typography.bodySmall)
                Text(Fmt.money(op.price, op.currency), color = palette.text, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.width(4.dp))
                Text(timeLabel(op.t), color = palette.textDim, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall)
            }
            if (op.reason != null) {
                Text(op.reason, color = palette.down, style = MaterialTheme.typography.bodySmall)
            }
            if (op.status == OpStatus.Partial && op.filledQty != null) {
                Text(
                    "${t.statusPartial}: ${op.filledQty}/${op.qty}",
                    color = palette.textMute,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }

        val (statusBg, statusFg) = when (op.status) {
            OpStatus.Filled -> palette.upSoft to palette.up
            OpStatus.Partial -> palette.accentSoft to palette.accent
            OpStatus.Cancelled -> palette.surface2 to palette.textMute
            OpStatus.Rejected -> palette.downSoft to palette.down
            OpStatus.Pending -> palette.surface2 to palette.textMute
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(statusBg)
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Text(
                text = when (op.status) {
                    OpStatus.Filled -> t.statusFilled
                    OpStatus.Partial -> t.statusPartial
                    OpStatus.Cancelled -> t.statusCancelled
                    OpStatus.Rejected -> t.statusRejected
                    OpStatus.Pending -> t.statusPending
                },
                color = statusFg,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

private fun dayLabel(key: String, lang: Lang): String {
    val today = "2026-05-08"
    val yest = "2026-05-07"
    if (key == today) return if (lang == Lang.Ru) "Сегодня" else "Today"
    if (key == yest) return if (lang == Lang.Ru) "Вчера" else "Yesterday"
    val df = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
    val d = df.parse(key) ?: return key
    val out = SimpleDateFormat(if (lang == Lang.Ru) "d MMMM" else "MMMM d", if (lang == Lang.Ru) Locale("ru") else Locale.US)
    return out.format(d)
}

private fun timeLabel(t: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
    return sdf.format(Date(t))
}
