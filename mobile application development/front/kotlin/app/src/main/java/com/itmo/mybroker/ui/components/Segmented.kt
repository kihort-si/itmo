package com.itmo.mybroker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.itmo.mybroker.ui.theme.LocalPalette

@Composable
fun Segmented(
    value: String,
    onChange: (String) -> Unit,
    options: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
) {
    val palette = LocalPalette.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(palette.surface2)
            .border(1.dp, palette.hairline, RoundedCornerShape(10.dp))
            .padding(2.dp),
    ) {
        for ((v, label) in options) {
            val on = v == value
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (on) palette.surface else androidx.compose.ui.graphics.Color.Transparent)
                    .clickable { onChange(v) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    color = if (on) palette.text else palette.textMute,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}
