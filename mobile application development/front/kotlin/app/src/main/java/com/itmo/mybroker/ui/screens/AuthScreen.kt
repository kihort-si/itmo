package com.itmo.mybroker.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.itmo.mybroker.BuildConfig
import com.itmo.mybroker.api.AuthApi
import com.itmo.mybroker.api.LoginRequest
import com.itmo.mybroker.api.RegisterRequest
import com.itmo.mybroker.core.SessionStore
import com.itmo.mybroker.i18n.strings
import com.itmo.mybroker.ui.components.MbIconButton
import com.itmo.mybroker.ui.components.TopBar
import com.itmo.mybroker.ui.icons.Ic
import com.itmo.mybroker.ui.theme.Lang
import com.itmo.mybroker.ui.theme.LocalPalette
import com.itmo.mybroker.ui.theme.LocalTweaks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val AUTH_LOG_TAG = "MyBrokerAuth"

@Composable
fun AuthScreen(
    onClose: () -> Unit,
    onAuthed: () -> Unit,
) {
    val palette = LocalPalette.current
    val t = strings()
    val lang = LocalTweaks.current.value.lang
    val isRu = lang == Lang.Ru

    var mode by remember { mutableStateOf("signin") }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var showPwd by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var err by remember { mutableStateOf<String?>(null) }
    var nameErr by remember { mutableStateOf<String?>(null) }
    var usernameErr by remember { mutableStateOf<String?>(null) }
    var emailErr by remember { mutableStateOf<String?>(null) }
    var passwordErr by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun clearFieldErrors() {
        nameErr = null
        usernameErr = null
        emailErr = null
        passwordErr = null
    }

    fun validateLocal(): Boolean {
        clearFieldErrors()
        var ok = true
        if (mode == "signup" && name.trim().length < 3) {
            nameErr = if (isRu) "Укажите полное ФИО" else "Enter your full name"
            ok = false
        }
        if (mode == "signup" && username.trim().length < 2) {
            usernameErr = if (isRu) "Минимум 2 символа" else "At least 2 characters"
            ok = false
        }
        if (mode == "signin" && email.trim().isBlank()) {
            emailErr = if (isRu) "Введите email или username" else "Enter email or username"
            ok = false
        }
        if (mode == "signup" && (!email.contains("@") || email.substringAfter("@", "").isBlank())) {
            emailErr = if (isRu) "Введите корректный email" else "Enter a valid email"
            ok = false
        }
        if (password.length < 6) {
            passwordErr = if (isRu) "Минимум 6 символов" else "At least 6 characters"
            ok = false
        }
        return ok
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.bg)
            .verticalScroll(rememberScrollState()),
    ) {
        TopBar(
            title = "",
            transparent = true,
            left = { MbIconButton(onClick = onClose) { Icon(Ic.Close, contentDescription = "close", tint = palette.text) } },
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(palette.accent),
                contentAlignment = Alignment.Center,
            ) {
                Text("M", color = palette.accentFg, style = MaterialTheme.typography.displaySmall)
            }
            Spacer(Modifier.height(12.dp))
            Text("MyBroker", color = palette.text, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${BuildConfig.AUTH_FORM_MARKER} | v${BuildConfig.VERSION_NAME}",
                color = palette.accent,
                style = MaterialTheme.typography.labelSmall,
            )
            Text(
                text = "API_BASE_URL=${BuildConfig.API_BASE_URL} | USE_MOCK_API=${BuildConfig.USE_MOCK_API}",
                color = palette.accent,
                style = MaterialTheme.typography.labelSmall,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (mode == "signin") {
                    if (isRu) "С возвращением. Рынки уже открыты." else "Welcome back. Markets are open."
                } else {
                    if (isRu) "Создайте брокерский аккаунт через backend." else "Create a broker account through the backend."
                },
                color = palette.textMute,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(palette.surface)
                .border(1.dp, palette.hairline, RoundedCornerShape(12.dp))
                .padding(4.dp),
        ) {
            AuthTab(t.signIn, mode == "signin", Modifier.weight(1f)) {
                mode = "signin"
                clearFieldErrors()
                err = null
            }
            AuthTab(t.signUp, mode == "signup", Modifier.weight(1f)) {
                mode = "signup"
                clearFieldErrors()
                err = null
            }
        }
        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (mode == "signup") {
                Text(
                    text = "SIGNUP_FORM_V2_VISIBLE: name + username + email + password; currency removed",
                    color = palette.accent,
                    style = MaterialTheme.typography.labelSmall,
                )
                AuthField(
                    label = if (isRu) "Полное ФИО" else "Full name",
                    value = name,
                    onChange = {
                        name = it
                        nameErr = null
                    },
                    placeholder = if (isRu) "Иванов Иван Иванович" else "Ivan Ivanov",
                    errorText = nameErr,
                )
                AuthField(
                    label = t.username,
                    value = username,
                    onChange = {
                        username = it
                        usernameErr = null
                    },
                    placeholder = "trader_2026",
                    errorText = usernameErr,
                )
            }
            AuthField(
                label = if (mode == "signin") {
                    if (isRu) "Email или username" else "Email or username"
                } else {
                    t.email
                },
                value = email,
                onChange = {
                    email = it
                    emailErr = null
                },
                placeholder = "user@itmo.ru",
                keyboardType = KeyboardType.Email,
                errorText = emailErr,
            )
            AuthField(
                label = t.password,
                value = password,
                onChange = {
                    password = it
                    passwordErr = null
                },
                placeholder = "********",
                keyboardType = KeyboardType.Password,
                visual = if (showPwd) VisualTransformation.None else PasswordVisualTransformation(),
                errorText = passwordErr,
                trailing = {
                    MbIconButton(onClick = { showPwd = !showPwd }) {
                        Icon(Ic.Eye, contentDescription = "toggle", tint = palette.textMute)
                    }
                },
            )

            if (mode == "signin") {
                Text(
                    text = t.forgot,
                    color = palette.accent,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            Spacer(Modifier.height(4.dp))
            if (err != null) {
                Text(
                    err!!,
                    color = palette.down,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
            Button(
                onClick = {
                    Log.d(AUTH_LOG_TAG, "Submit clicked mode=$mode marker=${BuildConfig.AUTH_FORM_MARKER} base=${BuildConfig.API_BASE_URL} mock=${BuildConfig.USE_MOCK_API}")
                    if (loading) return@Button
                    if (!validateLocal()) {
                        Log.d(AUTH_LOG_TAG, "Local validation failed mode=$mode nameErr=$nameErr usernameErr=$usernameErr emailErr=$emailErr passwordErr=$passwordErr")
                        return@Button
                    }
                    loading = true
                    err = null
                    scope.launch {
                        if (mode == "signup" && !BuildConfig.USE_MOCK_API) {
                            try {
                                Log.d(AUTH_LOG_TAG, "Checking username availability username=${username.trim()}")
                                val usernameBusy = withContext(Dispatchers.IO) {
                                    AuthApi.checkUsernameInUse(username.trim()).inUse
                                }
                                Log.d(AUTH_LOG_TAG, "Username availability result username=${username.trim()} inUse=$usernameBusy")
                                if (usernameBusy) {
                                    usernameErr = if (isRu) "Username уже занят" else "Username is already in use"
                                    loading = false
                                    return@launch
                                }
                                Log.d(AUTH_LOG_TAG, "Checking email availability email=${email.trim()}")
                                val emailBusy = withContext(Dispatchers.IO) {
                                    AuthApi.checkEmailInUse(email.trim()).inUse
                                }
                                Log.d(AUTH_LOG_TAG, "Email availability result email=${email.trim()} inUse=$emailBusy")
                                if (emailBusy) {
                                    emailErr = if (isRu) "Email уже занят" else "Email is already in use"
                                    loading = false
                                    return@launch
                                }
                            } catch (e: Exception) {
                                Log.e(AUTH_LOG_TAG, "Availability check failed: ${e.javaClass.simpleName}: ${e.message}", e)
                                loading = false
                                err = e.message ?: if (isRu) "Не удалось проверить данные" else "Could not validate account"
                                return@launch
                            }
                        }

                        val result = if (mode == "signin") {
                            SessionStore.login(LoginRequest(email = email.trim(), password = password))
                        } else {
                            SessionStore.register(
                                RegisterRequest(
                                    name = name.trim(),
                                    email = email.trim(),
                                    password = password,
                                    username = username.trim(),
                                ),
                            )
                        }
                        loading = false
                        if (result.isSuccess) {
                            Log.d(AUTH_LOG_TAG, "Auth flow succeeded mode=$mode")
                            onAuthed()
                        } else {
                            val failure = result.exceptionOrNull()
                            Log.e(AUTH_LOG_TAG, "Auth flow failed mode=$mode: ${failure?.javaClass?.simpleName}: ${failure?.message}", failure)
                            err = failure?.message ?: "Error"
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.accent,
                    contentColor = palette.accentFg,
                    disabledContainerColor = palette.surface3,
                    disabledContentColor = palette.textDim,
                ),
                shape = RoundedCornerShape(14.dp),
                enabled = !loading,
            ) {
                Text(
                    if (loading) "..." else if (mode == "signin") t.signIn else t.signUp,
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (mode == "signin") t.noAccount else t.hasAccount,
                    color = palette.textMute,
                    style = MaterialTheme.typography.bodyMedium,
                )
                TextButton(onClick = { mode = if (mode == "signin") "signup" else "signin" }) {
                    Text(
                        if (mode == "signin") t.signUp else t.signIn,
                        color = palette.accent,
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = t.riskWarning,
            color = palette.textDim,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
        )
    }
}

@Composable
private fun AuthTab(label: String, on: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val palette = LocalPalette.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (on) palette.surface3 else androidx.compose.ui.graphics.Color.Transparent)
            .clickable(onClick = onClick)
            .height(40.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = if (on) palette.text else palette.textMute, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun AuthField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    visual: VisualTransformation = VisualTransformation.None,
    errorText: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val palette = LocalPalette.current
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = palette.textMute, style = MaterialTheme.typography.labelSmall)
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = palette.textDim) },
            singleLine = true,
            isError = errorText != null,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = visual,
            trailingIcon = trailing,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = palette.text,
                unfocusedTextColor = palette.text,
                errorTextColor = palette.text,
                focusedContainerColor = palette.surface,
                unfocusedContainerColor = palette.surface,
                errorContainerColor = palette.surface,
                focusedBorderColor = palette.accent,
                unfocusedBorderColor = palette.hairline,
                errorBorderColor = palette.down,
            ),
        )
        if (errorText != null) {
            Text(errorText, color = palette.down, style = MaterialTheme.typography.bodySmall)
        }
    }
}
