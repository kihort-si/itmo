package com.itmo.mybroker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Tweaks(
    val theme: ThemeMode = ThemeMode.Dark,
    val variant: Variant = Variant.Calm,
    val density: Density = Density.Regular,
    val defaultChartKind: ChartKind = ChartKind.Candles,
    val lang: Lang = Lang.Ru,
)

enum class ThemeMode { Dark, Light }
enum class Variant { Calm, Bold }
enum class Density { Regular, Compact }
enum class ChartKind { Line, Candles }
enum class Lang { Ru, En }

val LocalTweaks = staticCompositionLocalOf<MutableState<Tweaks>> {
    error("Tweaks not provided. Wrap your tree in MyBrokerTheme().")
}

val LocalPalette = staticCompositionLocalOf<MyBrokerPalette> { DarkCalmPalette }

data class DensityTokens(
    val rowPadX: Dp,
    val rowPadY: Dp,
    val rowMinHeight: Dp,
)

val LocalDensityTokens = staticCompositionLocalOf {
    DensityTokens(rowPadX = 16.dp, rowPadY = 12.dp, rowMinHeight = 60.dp)
}

private fun densityTokensFor(d: Density): DensityTokens = when (d) {
    Density.Compact -> DensityTokens(rowPadX = 14.dp, rowPadY = 8.dp, rowMinHeight = 48.dp)
    Density.Regular -> DensityTokens(rowPadX = 16.dp, rowPadY = 12.dp, rowMinHeight = 60.dp)
}

private fun paletteFor(theme: ThemeMode, variant: Variant): MyBrokerPalette = when (theme) {
    ThemeMode.Dark -> if (variant == Variant.Bold) DarkBoldPalette else DarkCalmPalette
    ThemeMode.Light -> if (variant == Variant.Bold) LightBoldPalette else LightCalmPalette
}

@Composable
fun MyBrokerTheme(
    initial: Tweaks = Tweaks(),
    content: @Composable () -> Unit,
) {
    val state: MutableState<Tweaks> = remember { mutableStateOf(initial) }
    val tweaks = state.value
    val palette = paletteFor(tweaks.theme, tweaks.variant)
    val colorScheme = if (palette.isLight) {
        lightColorScheme(
            primary = palette.accent,
            onPrimary = palette.accentFg,
            secondary = palette.accent2,
            background = palette.bg,
            onBackground = palette.text,
            surface = palette.surface,
            onSurface = palette.text,
            surfaceVariant = palette.surface2,
            onSurfaceVariant = palette.textMute,
            outline = palette.hairline,
            error = palette.down,
        )
    } else {
        darkColorScheme(
            primary = palette.accent,
            onPrimary = palette.accentFg,
            secondary = palette.accent2,
            background = palette.bg,
            onBackground = palette.text,
            surface = palette.surface,
            onSurface = palette.text,
            surfaceVariant = palette.surface2,
            onSurfaceVariant = palette.textMute,
            outline = palette.hairline,
            error = palette.down,
        )
    }
    CompositionLocalProvider(
        LocalTweaks provides state,
        LocalPalette provides palette,
        LocalDensityTokens provides densityTokensFor(tweaks.density),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MyBrokerTypography,
            content = content,
        )
    }
}
