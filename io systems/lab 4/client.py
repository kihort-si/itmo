import crc8
import time
import threading


ser = serial.Serial(
    "/dev/cu.usbserial-2120",
    115200,
    parity=serial.PARITY_ODD,
    stopbits=serial.STOPBITS_TWO,
    timeout=1
)

hash = crc8.crc8()


def tocrc(data):
    # Считает CRC8 для переданных данных.

    return hash.reset().update(data).digest()


def sendPacket(data):
    ser.write(b'\x5A')
    ser.write(len(data).to_bytes(1, "little"))
    ser.write(data)
    ser.write(tocrc(data))


def sendWrongLengthPacket():
    data = b'MYAW'

    ser.write(b'\x5A')
    ser.write((len(data) + 3).to_bytes(1, "little"))
    ser.write(data)
    ser.write(tocrc(data))


def sendNoSyncPacket():

    data = b'MYAW'

    ser.write(b'\x00')
    ser.write(len(data).to_bytes(1, "little"))
    ser.write(data)
    ser.write(tocrc(data))


def sendNotEnoughDataPacket():
    data = b'MYAW'

    ser.write(b'\x5A')
    ser.write((5).to_bytes(1, "little"))
    ser.write(data)

def interval():
    while True:
        print("correct MYAW")
        sendPacket(b'MYAW')
        time.sleep(5)

        print("wrong length")
        sendWrongLengthPacket()
        time.sleep(5)

        print("no sync byte")
        sendNoSyncPacket()
        time.sleep(5)

        print("not enough data")
        sendNotEnoughDataPacket()
        time.sleep(5)


threading.Thread(target=interval, daemon=True).start()


while True:
    bs = ser.read()

    if bs != b'\x5A':
        continue

    n = ser.read()

    if len(n) == 0:
        continue

    n = int.from_bytes(n, "little")

    data = ser.read(n)
    crc = ser.read()

    if len(data) != n:
        print("Error: not enough data")
        continue

    if len(crc) == 0:
        print("Error: no crc byte")
        continue

    if tocrc(data) != crc:
        print("Error in crc8")
        continue

    if len(data) == 4:
        temp = int.from_bytes(data[0:2], "little")
        hum = int.from_bytes(data[2:4], "little")

        print(
            f'Температура: {temp}°C\nВлажность: {hum}%'
        )
    else:
        print(f'Эхо-пакет от контроллера: {data}')
