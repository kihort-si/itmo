package com.itmo.mybroker.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import com.itmo.mybroker.ui.theme.Lang
import com.itmo.mybroker.ui.theme.LocalTweaks

val LocalI18n = compositionLocalOf<I18n> { StringsRu }

@Composable
@ReadOnlyComposable
fun strings(): I18n {
    val lang = LocalTweaks.current.value.lang
    return if (lang == Lang.En) StringsEn else StringsRu
}
