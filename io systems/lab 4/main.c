/**
* DHT11 Sensor Reader
 * This sketch reads temperature and humidity data from the DHT11 sensor and prints the values to the serial port.
 * It also handles potential error states that might occur during reading.
 *
 * Author: Dhruba Saha
 * Version: 2.1.0
 * License: MIT
 */

// Include the DHT11 library for interfacing with the sensor.
#include <cstdint>
#include <DHT11.h>
#include "CRC8.h"
#include "CRC.h"
#include <avr/interrupt.h>

#define DHT_PIN 2
#define LED_PIN 13

#define SYNC_BYTE 0x5A
#define MAX_PACKET_SIZE 256

#define SetBit(reg, bit) reg |= (1 << bit)

DHT11 sensor(DHT_PIN);

uint8_t rxPacket[MAX_PACKET_SIZE];

/*
  state = 0 — ждем 0x5A
  state = 1 — ждем длину пакета
  state = 2 — принимаем данные
  state = 3 — принимаем CRC и проверяем пакет
*/
volatile int rxState = 0;
volatile int payloadLength = 0;
volatile int payloadPos = 0;


// Передача одного байта через UART.
void UART_SendByte(uint8_t value) {
    while (!(UCSR0A & (1 << UDRE0)));

    UDR0 = value;
}

// Передача массива байтов.
void UART_SendBytes(uint8_t data[], int length) {
    for (int i = 0; i < length; i++) {
        UART_SendByte(data[i]);
    }
}

/*
Отправка пакета в формате:
  0x5A | LEN | DATA | CRC8
*/
void UART_SendPacket(uint8_t data[], int length) {
    uint8_t checkSum = calcCRC8(data, length);

    UART_SendByte(SYNC_BYTE);
    UART_SendByte((uint8_t) length);
    UART_SendBytes(data, length);
    UART_SendByte(checkSum);
}

void UART_Init() {
    uint32_t baudRate = 115200;

    /*
        Расчет значения регистра UBRR.
        Формула для обычного асинхронного режима:
        UBRR = F_CPU / (16 * baudRate) - 1

        Для Arduino Uno/Nano F_CPU обычно равен 16 MHz.
    */
    uint32_t ubrrValue = 16000000UL / 16UL / baudRate;

    UBRR0H = (uint8_t)(ubrrValue >> 8);
    UBRR0L = (uint8_t) ubrrValue;

    /*
        UCSR0B:
        TXEN0  — разрешить передачу
        RXEN0  — разрешить прием
        RXCIE0 — разрешить прерывание при получении байта
    */
    SetBit(UCSR0B, TXEN0);
    SetBit(UCSR0B, RXEN0);
    SetBit(UCSR0B, RXCIE0);

    /*
        UCSR0C:
        UCSZ01 + UCSZ00 — 8 бит данных
        UPM01 + UPM00   — odd parity
        USBS0           — 2 stop bits

        Вариант 4 требует:
        8 data bits, odd parity, 2 stop bits.
    */
    SetBit(UCSR0C, UCSZ00);
    SetBit(UCSR0C, UCSZ01);
    SetBit(UCSR0C, UPM00);
    SetBit(UCSR0C, UPM01);
    SetBit(UCSR0C, USBS0);
}

void setup() {
    /*
    Настраиваем задержку чтения DHT11.
    По заданию нужно отправлять данные примерно раз в секунду.
        */
    sensor.setDelay(1000);

    /*
        Настраиваем UART вручную через регистры.
        Serial.begin() не используется.
    */
    UART_Init();

    pinMode(LED_PIN, OUTPUT);

    /*
        Разрешаем глобальные прерывания.
        Без этого ISR(USART_RX_vect) не будет вызываться.
    */
    sei();
}

/*
  Обработчик прерывания UART RX.

  Он вызывается каждый раз, когда по UART пришел новый байт.
  Байт забирается из регистра UDR0.
*/
ISR(USART_RX_vect) {
    uint8_t currentByte = UDR0;

    switch (rxState) {
        case 0:
            /*
                Состояние 0:
                ищем начало пакета — синхробайт 0x5A.
            */
            if (currentByte == SYNC_BYTE) {
                rxState = 1;
            }
            break;

        case 1:
            /*
                Состояние 1:
                получили длину полезных данных.
            */
            payloadLength = currentByte;
            payloadPos = 0;

            if (payloadLength <= 0 || payloadLength > MAX_PACKET_SIZE) {
                rxState = 0;
            } else {
                rxState = 2;
            }
            break;

        case 2:
            /*
                Состояние 2:
                принимаем DATA.
            */
            rxPacket[payloadPos] = currentByte;
            payloadPos++;

            if (payloadPos >= payloadLength) {
                rxState = 3;
            }
            break;

        case 3:
            /*
                Состояние 3:
                received — это CRC, который пришел от клиента.
                Считаем CRC от полученных DATA и сравниваем.
                    Если совпало — отправляем этот же пакет обратно.
                    Если не совпало — просто отбрасываем.
                */
            uint8_t expectedCrc = calcCRC8(rxPacket, payloadLength);

            if (currentByte == expectedCrc) {
                UART_SendPacket(rxPacket, payloadLength);

                /*
                Дополнительная демонстрация:
                если клиент отправил один байт 'A', включаем LED.
                если клиент отправил один байт 'B', выключаем LED.
                */
                if (payloadLength == 1 && rxPacket[0] == 'A') {
                    digitalWrite(LED_PIN, HIGH);
                }

                if (payloadLength == 1 && rxPacket[0] == 'B') {
                    digitalWrite(LED_PIN, LOW);
                }
            }

            /*
                После проверки возвращаемся к ожиданию нового пакета.
            */
            rxState = 0;
            payloadPos = 0;
            payloadLength = 0;
            break;
    }
}

void loop() {
    int temperature = 0;
    int humidity = 0;

    // Читаем значения температуры и влажности с DHT11 сенсора.
    int readResult = sensor.readTemperatureHumidity(temperature, humidity);

    if (readResult == 0) {
        /*
        Упаковываем данные датчика в 4 байта:

        res[0] — температура
        res[1] — влажность

        Так как int занимает 2 байта на Arduino Uno,
        массив из двух int занимает 4 байта.
        */
        int sensorValues[2];

        sensorValues[0] = temperature;
        sensorValues[1] = humidity;

        /*
        Отправляем пакет:
        0x5A | 0x04 | TEMP_LOW | TEMP_HIGH | HUM_LOW | HUM_HIGH | CRC8
        */
        UART_SendPacket((uint8_t *) sensorValues, 4);
    }
    delay(1000);
}
