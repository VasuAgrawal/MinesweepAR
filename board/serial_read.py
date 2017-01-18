#!/usr/bin/env python3

# Converts the serial bytes into a character mapping

import serial
import json
import socket

def main():
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.connect(("localhost", 8000))

    out = dict()
    out['type'] = 'KEY_PRESS'

    with serial.Serial('/dev/ttyACM0', 115200) as ser:
        while True:
            # We should read a row, then a column.
            row = ser.read() 
            col = ser.read()

            out['payload'] = "%d:%d" % (int(row), int(col))
            payload = json.dumps(out)
           
            s.send(payload.encode('ascii'))
            print(payload)


if __name__ == "__main__":
    main()
