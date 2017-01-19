#!/usr/bin/env python3

# Enables a self test to figure out which buttons have been pressed so far.

import numpy as np
import pygame

ROWS = 9
COLS = 9
WIDTH = 900
HEIGHT = 900
COL_WIDTH = WIDTH // COLS
ROW_HEIGHT = HEIGHT // ROWS

board = np.zeros((ROWS, COLS), dtype=np.int)
screen = pygame.display.set_mode((WIDTH, HEIGHT))

BLANK = pygame.image.load("../Images/facingDown.png")
OPEN = pygame.image.load("../Images/flagged.png");

pygame.init()

def update_board():
    for row in range(ROWS):
        for col in range(COLS):
            # Depending on the state of the square, we display varying images.
            rect = pygame.Rect(col * COL_WIDTH, row * ROW_HEIGHT,
                    COL_WIDTH, ROW_HEIGHT)
            image = OPEN if board[row][col] else BLANK
            image = pygame.transform.scale(image, (COL_WIDTH, ROW_HEIGHT))
            screen.blit(image, rect)

def main():
    with serial.Serial('dev/ttyACM0', 115200) as ser:
        update_board()
        while True:
            row = int(ser.read())
            col = int(ser.read())
            board[row][col] = 1
            update_board()
