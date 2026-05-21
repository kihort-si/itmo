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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.itmo.mybroker.core.SessionStore
import com.itmo.mybroker.data.BrokerData
import com.itmo.mybroker.i18n.strings
import com.itmo.mybroker.ui.components.MbIconButton
import com.itmo.mybroker.ui.components.MbSwitch
import com.itmo.mybroker.ui.components.TopBar
import com.itmo.mybroker.ui.icons.Ic
import com.itmo.mybroker.ui.theme.Lang
import com.itmo.mybroker.ui.theme.LocalPalette
import com.itmo.mybroker.ui.theme.LocalTweaks

@Composable
fun ProfileScreen(
    isAuthed: Boolean,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onOpenTweaks: () -> Unit,
) {
    val palette = LocalPalette.current
    val t = strings()
    val tweaks = LocalTweaks.current.value
    val lang = tweaks.lang
    val authEmail by SessionStore.email.collectAsState(initial = null)
    val authUsername by SessionStore.username.collectAsState(initial = null)
    val authName by SessionStore.name.collectAsState(initial = null)
    val authClntId by SessionStore.clntId.collectAsState(initial = null)

    if (!isAuthed) {
        Column(modifier = Modifier.fillMaxSize().background(palette.bg)) {
            TopBar(title = t.profile, large = true)
            AuthPrompt(
                icon = { Icon(Ic.User, contentDescription = null, tint = palette.accent, modifier = Modifier.size(48.dp)) },
                title = t.signInToAccount,
                sub = t.signInToAccountSub,
                ctaLabel = t.signIn,
                onCta = onSignIn,
            )
        }
        return
    }

    var notifyOn by remember { mutableStateOf(true) }
    var biometryOn by remember { mutableStateOf(true) }
    var soundsOn by remember { mutableStateOf(false) }
    val user = BrokerData.repository.user
    val displayName = authName?.takeIf { it.isNotBlank() } ?: if (lang == Lang.Ru) user.nameRu else user.nameEn
    val displayEmail = authEmail ?: user.email
    val displayUsername = authUsername?.takeIf { it.isNotBlank() } ?: "trader"
    val displayInitials = displayName
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }
        .ifBlank { displayUsername.take(1).uppercase() }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(palette.bg),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            TopBar(
                title = t.profile,
                large = true,
                right = {
                    MbIconButton(onClick = onOpenTweaks) {
                        Icon(Ic.Bell, contentDescription = "tweaks", tint = palette.text)
                    }
                },
            )
        }
        item {

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(palette.surface)
                    .border(1.dp, palette.hairline, RoundedCornerShape(18.dp))
                    .padding(vertical = 20.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(palette.accentSoft),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(displayInitials, color = palette.accent, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                }
                Text(displayName, color = palette.text, style = MaterialTheme.typography.titleLarge)
                Text("@$displayUsername", color = palette.textMute, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodyMedium)
                Text(displayEmail, color = palette.textMute, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(palette.surface2)
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    ProfileStat(t.memberSince, if (lang == Lang.Ru) user.memberSinceRu else user.memberSinceEn)
                    StatDivider()
                    ProfileStat("ID", authClntId?.toString() ?: "-")
                    StatDivider()
                    ProfileStat(t.streak, user.streak)
                }
            }
        }
        item {
            Text(
                t.settings.uppercase(),
                color = palette.textMute,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 6.dp),
            )
        }
        item {
            SettingsList {
                SettingsRow(icon = Ic.Bell, label = t.notifications) { MbSwitch(on = notifyOn, onChange = { notifyOn = it }) }
                SettingsRow(icon = Ic.Shield, label = t.biometry) { MbSwitch(on = biometryOn, onChange = { biometryOn = it }) }
                SettingsRow(icon = Ic.Signal, label = t.tradeSounds) { MbSwitch(on = soundsOn, onChange = { soundsOn = it }) }
            }
        }
        item {
            Text(
                t.support.uppercase(),
                color = palette.textMute,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 6.dp),
            )
        }
        item {
            SettingsList {
                SettingsRow(icon = Ic.Briefcase, label = t.termsFees) {
                    Icon(Ic.Right, contentDescription = null, tint = palette.textDim, modifier = Modifier.size(18.dp))
                }
                SettingsRow(icon = Ic.Shield, label = t.privacy) {
                    Icon(Ic.Right, contentDescription = null, tint = palette.textDim, modifier = Modifier.size(18.dp))
                }
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(palette.downSoft)
                    .clickable(onClick = onSignOut)
                    .padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Ic.Logout, contentDescription = null, tint = palette.down, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(t.signOut, color = palette.down, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Text(
                "${t.riskWarning} v1.0.4 · build 2026.05",
                color = palette.textDim,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            )
        }
    }
}

@Composable
private fun ProfileStat(label: String, value: String) {
    val palette = LocalPalette.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = palette.textMute, style = MaterialTheme.typography.labelSmall)
        Text(value, color = palette.text, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun StatDivider() {
    val palette = LocalPalette.current
    Box(modifier = Modifier.width(1.dp).height(28.dp).background(palette.hairline))
}

@Composable
private fun SettingsList(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    val palette = LocalPalette.current
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(palette.surface)
            .border(1.dp, palette.hairline, RoundedCornerShape(14.dp)),
        content = content,
    )
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    trailing: @Composable () -> Unit,
) {
    val palette = LocalPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, contentDescription = null, tint = palette.textMute, modifier = Modifier.size(20.dp))
        Text(label, color = palette.text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        trailing()
    }
}
