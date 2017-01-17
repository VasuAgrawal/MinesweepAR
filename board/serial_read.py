#!/usr/bin/env python3

# Converts the serial bytes into a character mapping

import serial

def main():
    with serial.Serial('/dev/ttyACM0', 115200) as ser:
        while True:
            # We should read a row, then a column.
            row = ser.read() 
            col = ser.read()
            print("Button pressed at (%d:%d)", int(row), int(col))

if __name__ == "__main__":
    main()
