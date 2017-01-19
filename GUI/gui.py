#!/usr/bin/env python3

import socket
import pygame
import logging
import json
import pprint
import threading
import queue
import time

ROWS = 9
COLS = 9
WIDTH = 900
HEIGHT = 900
COL_WIDTH = WIDTH // COLS
ROW_HEIGHT = HEIGHT // ROWS
BOTTOM = HEIGHT // 6
screen = pygame.display.set_mode((WIDTH, HEIGHT + BOTTOM))
pygame.init()
font = pygame.font.SysFont("Times New Roman", 15)

BLANK = " "
MARKED = "*"
MINE = "X"
MARKED_MINE = "M"
UNMARKED_MINE = "?"
BAD_MARK = "&"

images = dict()
images[BLANK] = pygame.image.load("Images/facingDown.png")
images[MARKED] = pygame.image.load("Images/flagged.png")
images[MINE] = pygame.image.load("Images/bomb.png")
images[MARKED_MINE] = pygame.image.load("Images/flagged.png")
images[UNMARKED_MINE] = pygame.image.load("Images/bomb.png")
images[BAD_MARK] = pygame.image.load("Images/flagged.png")
images["0"] = pygame.image.load("Images/0.png")
images["1"] = pygame.image.load("Images/1.png")
images["2"] = pygame.image.load("Images/2.png")
images["3"] = pygame.image.load("Images/3.png")
images["4"] = pygame.image.load("Images/4.png")
images["5"] = pygame.image.load("Images/5.png")
images["6"] = pygame.image.load("Images/6.png")
images["7"] = pygame.image.load("Images/7.png")
images["8"] = pygame.image.load("Images/8.png")

game_states = queue.Queue()
data_out = queue.Queue()

def draw_board(game_state):

    # TODO: Choose some other color for this.
    screen.fill((185, 185, 185))

    time = game_state['time']
    mineCount = game_state['mineCount']

    textFont = pygame.font.SysFont("arial", 30)
    timeText = textFont.render("Time: %d Min %d Sec" % (time // 60, time % 60),
            True, (0,0,0))
    countText = textFont.render("Mine Count: %d" % mineCount, True,
            (0,0,0))
    
    timeSize = textFont.size("Time: %d Min %d Sec" % (time // 60, time % 60))
    countSize = textFont.size("Mine Count: %d" % mineCount)

    screen.blit(timeText, ((WIDTH - timeSize[0]) // 2, 
        (BOTTOM - 1.9 * timeSize[1]) // 2))
    screen.blit(countText, ((WIDTH - countSize[0]) // 2, 
        (BOTTOM - 0.1 * countSize[1]) // 2))

    for row in range(ROWS):
        for col in range(COLS):
            # Depending on the state of the square, we display varying images.
            rect = pygame.Rect(col * COL_WIDTH, row * ROW_HEIGHT + BOTTOM,
                    COL_WIDTH, ROW_HEIGHT)
            char = game_state["board"][row * COLS + col]
            image = images[char]
            image = pygame.transform.scale(image, (COL_WIDTH, ROW_HEIGHT))
            screen.blit(image, rect)

def data_read():
    # Try to constantly read data from the server, and push to the queue.

    while True:
        # First set up the socket by sending the correct payload.
        try:
            s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            s.connect(("localhost", 8000))
            logging.info("Connected to the server!")

            out = dict()
            out['type'] = 'GAME_STATE'
            out['payload'] = ''
            payload = json.dumps(out) + '\n'
            
            s.send(payload.encode('ascii'))
            # Should start receiving game data now

            readfile = s.makefile('r')

        except ConnectionRefusedError:
            logging.error("Unable to connect to server!")
            continue

        while True:
            try:
                message = readfile.readline()
                game_state = json.loads(message)
                game_states.put(game_state)
            except:
                logging.error("Connection to server terminated, reconnecting.")
                time.sleep(5)
                break

def handle_mouse(event):
    # Convert the mouse position to a row / col number. 
    x = event.pos[0]
    y = event.pos[1]

    row = (y - BOTTOM) // ROW_HEIGHT
    col = x // COL_WIDTH
   
    out = dict()
    if row >= 0 and col >= 0:
        if event.button == 1: # Left
            out['type'] = 'KEY_PRESS'
            out['payload'] = "%d:%d" % (row, col)
        elif event.button == 3:
            out['type'] = 'MARK'
            out['payload'] = "%d:%d" % (row, col)
    else:
        out['type'] = 'RESTART'
        out['payload'] = ""

    payload = json.dumps(out) + "\n"
    data_out.put(payload)

def data_write():
    while True:
        # Connect to the socket, read data.
        payload = data_out.get()
        print(payload)
        try:
            s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            s.connect(("localhost", 8000))
            s.send(payload.encode('ascii'))
            s.close()
        except ConnectionRefusedError:
            logging.error("Unable to connect to server.")
        

def main():
    screen.fill((0, 0, 0))
    read_thread = threading.Thread(target=data_read, daemon=True).start()
    write_thread = threading.Thread(target=data_write, daemon=True).start()

    while True:
        # Constantly try to get game states and redraw. Once that's done, handle
        # the mouse events.
        try:
            game_state = game_states.get_nowait()
            draw_board(game_state)
            pygame.display.update()
        except queue.Empty:
            pass

        for event in pygame.event.get():
            if event.type == pygame.MOUSEBUTTONDOWN:
                handle_mouse(event) 


if __name__ == "__main__":
    main()
