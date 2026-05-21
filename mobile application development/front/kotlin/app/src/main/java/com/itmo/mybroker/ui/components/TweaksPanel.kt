package com.itmo.mybroker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.itmo.mybroker.api.LoginRequest
import com.itmo.mybroker.core.SessionStore
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.dp
import com.itmo.mybroker.ui.theme.ChartKind
import com.itmo.mybroker.ui.theme.Density
import com.itmo.mybroker.ui.theme.Lang
import com.itmo.mybroker.ui.theme.LocalPalette
import com.itmo.mybroker.ui.theme.LocalTweaks
import com.itmo.mybroker.ui.theme.ThemeMode
import com.itmo.mybroker.ui.theme.Variant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TweaksPanel(onDismiss: () -> Unit) {
    val palette = LocalPalette.current
    val tweaks = LocalTweaks.current
    val scope = rememberCoroutineScope()
    val jwt by SessionStore.jwt.collectAsState(initial = null)
    val authed = jwt != null
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = palette.surface,
        contentColor = palette.text,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Tweaks",
                style = MaterialTheme.typography.titleLarge,
                color = palette.text,
            )

            Section("Variant") {
                Radio(
                    label = "Aesthetic",
                    value = tweaks.value.variant.name,
                    options = listOf("Calm" to "Calm", "Bold" to "Bold"),
                ) { v ->
                    tweaks.value = tweaks.value.copy(variant = if (v == "Bold") Variant.Bold else Variant.Calm)
                }
                Radio(
                    label = "Theme",
                    value = tweaks.value.theme.name,
                    options = listOf("Dark" to "Dark", "Light" to "Light"),
                ) { v ->
                    tweaks.value = tweaks.value.copy(theme = if (v == "Light") ThemeMode.Light else ThemeMode.Dark)
                }
            }

            Section("Display") {
                Radio(
                    label = "Density",
                    value = tweaks.value.density.name,
                    options = listOf("Regular" to "Regular", "Compact" to "Compact"),
                ) { v ->
                    tweaks.value = tweaks.value.copy(density = if (v == "Compact") Density.Compact else Density.Regular)
                }
                Radio(
                    label = "Default chart",
                    value = tweaks.value.defaultChartKind.name,
                    options = listOf("Line" to "Line", "Candles" to "Candles"),
                ) { v ->
                    tweaks.value = tweaks.value.copy(defaultChartKind = if (v == "Line") ChartKind.Line else ChartKind.Candles)
                }
                Radio(
                    label = "Language",
                    value = tweaks.value.lang.name,
                    options = listOf("Ru" to "Русский", "En" to "English"),
                ) { v ->
                    tweaks.value = tweaks.value.copy(lang = if (v == "En") Lang.En else Lang.Ru)
                }
            }

            Section("Account") {
                Radio(
                    label = "Auth state",
                    value = if (authed) "in" else "out",
                    options = listOf("out" to "Signed out", "in" to "Signed in"),
                ) { v ->
                    scope.launch {
                        if (v == "in") {
                            SessionStore.login(LoginRequest(email = "dev@itmo.ru", password = "devpass"))
                        } else {
                            SessionStore.logout()
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    val palette = LocalPalette.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title,
            color = palette.textMute,
            style = MaterialTheme.typography.labelMedium,
        )
        content()
    }
}

@Composable
private fun Radio(
    label: String,
    value: String,
    options: List<Pair<String, String>>,
    onChange: (String) -> Unit,
) {
    val palette = LocalPalette.current
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = palette.text, style = MaterialTheme.typography.bodyMedium)
        Segmented(
            value = value,
            onChange = onChange,
            options = options,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
