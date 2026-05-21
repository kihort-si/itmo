# Kotlin mobile frontend integration

Kotlin-приложение теперь умеет работать в двух режимах:

- mock-макет: `USE_MOCK_API = true`
- интеграция с backend: `USE_MOCK_API = false`

Переключатель находится в `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "API_BASE_URL", "\"http://127.0.0.1:8060\"")
buildConfigField("boolean", "USE_MOCK_API", "false")
```

## Backend mode

Все запросы идут в BFF с префиксом:

```text
/api/broker-app/v1
```

Форма регистрации отправляет:

- `name`
- `username`
- `email`
- `password`

Валюта счета с формы убрана. `base_currency_id` и другие системные параметры задаются в `core/env/gateway.env`.

Перед регистрацией приложение проверяет занятость `username` и `email` через BFF.

## Debug-маркеры и Logcat

На экране авторизации должен быть виден маркер:

```text
KOTLIN_AUTH_FORM_V2_BFF_2026_05_20
```

На вкладке регистрации должен быть виден маркер:

```text
SIGNUP_FORM_V2_VISIBLE: name + username + email + password; currency removed
```

Если этих строк нет, Android Studio запускает не свежую Kotlin-сборку или не тот модуль.

В Android Studio Logcat фильтруй по тегам:

```text
MyBrokerApp
MyBrokerAuth
MyBrokerSession
MyBrokerApi
```

`MyBrokerApi` пишет метод, полный URL, наличие JWT, request body с замаскированным password/token, status code, elapsed time и response body.

## Тестирование с Android-телефона по USB

1. Подними backend:

   ```powershell
   cd core
   docker compose up --build
   ```

2. Подключи телефон по USB и включи USB debugging.

3. Проверь, что устройство видно:

   ```powershell
   C:\Users\Artyom\AppData\Local\Android\Sdk\platform-tools\adb.exe devices
   ```

4. Пробрось порт BFF с компьютера на телефон:

   ```powershell
   C:\Users\Artyom\AppData\Local\Android\Sdk\platform-tools\adb.exe reverse tcp:8060 tcp:8060
   ```

5. Собери/запусти Kotlin-приложение. При `API_BASE_URL = "http://127.0.0.1:8060"` телефон будет попадать в BFF на компьютере, а BFF дальше пойдет через `core-nginx` к нужным backend-сервисам.

Для Android Emulator вместо `adb reverse` можно поставить:

```kotlin
buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8060\"")
```
