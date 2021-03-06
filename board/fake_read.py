#!/usr/bin/env python3

# Converts the serial bytes into a character mapping

import sys
import json
import socket
import logging

# Since the inputs should be coming in fairly slowly, a new connection is going
# to be opened for every single keypress.
def main():
        # We should read a row, then a column.
        row = sys.argv[1]
        col = sys.argv[2]

        try:
            s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            s.connect(("localhost", 8000))

            out = dict()
            out['type'] = 'KEY_PRESS'
            out['payload'] = "%d:%d" % (int(row), int(col))
            payload = json.dumps(out) + "\n"
        
            s.send(payload.encode('ascii'))
            print(payload)

            s.close()
        except ConnectionRefusedError:
            logging.error("Unable to connect to server!")


if __name__ == "__main__":
    main()
