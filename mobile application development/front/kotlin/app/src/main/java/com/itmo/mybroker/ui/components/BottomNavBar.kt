package com.itmo.mybroker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BusinessCenter
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.itmo.mybroker.i18n.strings
import com.itmo.mybroker.ui.theme.LocalPalette

enum class HomeTab(val route: String) {
    Exchange("exchange"),
    Portfolio("portfolio"),
    Profile("profile");
    companion object {
        fun fromRoute(r: String?): HomeTab? = values().firstOrNull { it.route == r }
    }
}

@Composable
fun BottomNavBar(
    current: HomeTab,
    onSelect: (HomeTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalPalette.current
    val t = strings()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(78.dp)
            .background(palette.surface)
            .padding(start = 4.dp, end = 4.dp, top = 8.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Tab(HomeTab.Exchange, current, Icons.Outlined.ShowChart, t.tabExchange, onSelect, Modifier.weight(1f))
        Tab(HomeTab.Portfolio, current, Icons.Outlined.BusinessCenter, t.tabPortfolio, onSelect, Modifier.weight(1f))
        Tab(HomeTab.Profile, current, Icons.Outlined.Person, t.tabProfile, onSelect, Modifier.weight(1f))
    }
}

@Composable
private fun Tab(
    tab: HomeTab,
    current: HomeTab,
    icon: ImageVector,
    label: String,
    onSelect: (HomeTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalPalette.current
    val on = current == tab
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable { onSelect(tab) },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(width = 56.dp, height = 32.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(if (on) palette.accentSoft else androidx.compose.ui.graphics.Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (on) palette.accent else palette.textMute,
                modifier = Modifier.size(22.dp),
            )
        }
        Text(
            text = label,
            color = if (on) palette.text else palette.textMute,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
