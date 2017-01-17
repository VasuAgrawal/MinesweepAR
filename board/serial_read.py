#!/usr/bin/env python3

import serial
with serial.Serial('/dev/ttyACM0', 9600) as ser:
    while True:
        print(ser.read())
