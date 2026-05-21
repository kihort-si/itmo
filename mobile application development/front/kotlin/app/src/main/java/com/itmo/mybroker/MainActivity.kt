package com.itmo.mybroker

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.itmo.mybroker.core.SessionStore
import com.itmo.mybroker.i18n.LocalI18n
import com.itmo.mybroker.i18n.StringsEn
import com.itmo.mybroker.i18n.StringsRu
import com.itmo.mybroker.ui.nav.MyBrokerNavHost
import com.itmo.mybroker.ui.theme.Lang
import com.itmo.mybroker.ui.theme.LocalPalette
import com.itmo.mybroker.ui.theme.LocalTweaks
import com.itmo.mybroker.ui.theme.MyBrokerTheme
import com.itmo.mybroker.ui.theme.Tweaks

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MyBrokerApp", "MainActivity.onCreate marker=${BuildConfig.AUTH_FORM_MARKER} version=${BuildConfig.VERSION_NAME} base=${BuildConfig.API_BASE_URL} mock=${BuildConfig.USE_MOCK_API}")
        enableEdgeToEdge()
        SessionStore.init(applicationContext)
        setContent {
            MyBrokerTheme(initial = Tweaks()) {
                val tweaks = LocalTweaks.current.value
                val strings = if (tweaks.lang == Lang.En) StringsEn else StringsRu
                val palette = LocalPalette.current
                CompositionLocalProvider(LocalI18n provides strings) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(palette.bg)
                            .windowInsetsPadding(WindowInsets.systemBars),
                    ) {
                        MyBrokerNavHost()
                    }
                }
            }
        }
    }
}
