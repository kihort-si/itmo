package com.itmo.mybroker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.itmo.mybroker.ui.theme.LocalPalette
import com.itmo.mybroker.ui.theme.MyBrokerTypography

@Composable
fun TopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    large: Boolean = false,
    transparent: Boolean = false,
    left: (@Composable () -> Unit)? = null,
    right: (@Composable () -> Unit)? = null,
) {
    val palette = LocalPalette.current
    val bg = if (transparent) androidx.compose.ui.graphics.Color.Transparent else palette.bg
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(bg),
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.widthIn(min = 48.dp), contentAlignment = Alignment.CenterStart) {
                left?.invoke()
            }
            if (!large) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = title,
                        style = MyBrokerTypography.headlineSmall,
                        color = palette.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MyBrokerTypography.bodySmall,
                            color = palette.textMute,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.widthIn(min = 48.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                right?.invoke()
            }
        }

        if (large) {
            Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 16.dp)) {
                Text(
                    text = title,
                    style = MyBrokerTypography.displayMedium,
                    color = palette.text,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MyBrokerTypography.bodyMedium,
                        color = palette.textMute,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        }
    }
}
