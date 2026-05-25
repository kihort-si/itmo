# Разработка мобильных приложений

## **О предмете**

Цель курса - сформировать у обучающихся представление об утвержденных правилах построения мобильных приложений. После прохождения курса слушатели смогут создавать работающие приложения, которые охватывают все темы в Android разработке. Существенным преимуществом для слушателей будут знания языка программирования Java и Kotlin.

Продолжительность: 1 семестр.

## [**Групповое задание**](https://github.com/kihort-si/itmo/tree/main/mobile%20application%20development)

![Kotlin](https://github.com/kihort-si/itmo/blob/main/common/kotlin.svg)
![React Native](https://github.com/kihort-si/itmo/blob/main/common/react.svg)
![Go](https://github.com/kihort-si/itmo/blob/main/common/go.svg)
![TypeScript](https://github.com/kihort-si/itmo/blob/main/common/ts.svg)
![Postgre SQL](https://github.com/kihort-si/itmo/blob/main/common/postgres.svg)
![Redis](https://github.com/kihort-si/itmo/blob/main/common/redis.svg)
![RabbitMQ](https://github.com/kihort-si/itmo/blob/main/common/rabbitmq.svg)
![Docker](https://github.com/kihort-si/itmo/blob/main/common/docker.svg)

Разработать экосистему для поддержания трейдинга и инвестиций с имитацией бэкенда брокера, который получает информацию о котировках акций с «биржи». Система должна поддерживать 10 тысяч клиентов.

Стек технологий:

* Нативное мобильное приложение:
  * Android (реальное устройство или симулятор)
  * Kotlin
  * Jetpack compose
* Мобильное приложение на базе Web фреймворка (React native и т.п.)
* Бэкенд:
  * *nux: Linux (Windows/WSL), MacOS
  * Kotlin, Ktor, корутины, библиотека для сериализации JSON и т.п.
  * Go для сбора данных о котировках
  * C для драйвера Linux
  * Redis Server/KeyDB– кэш БД, брокер сообщений
  * PostgreSQL и аналоги
  * ClickHouse
  * Open Telemetry – для сбора телеметрии

Функции системы:

* Получение данных от биржи
* Выдача данных клиентам
* Организация покупки/продажи акций в терминале
