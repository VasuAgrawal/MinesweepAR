import socket
import pygame
import logging
import json
import pprint

WIDTH = 450
HEIGHT = 450
screen = pygame.display.set_mode((WIDTH, HEIGHT))
pygame.init()
font = pygame.font.SysFont("Times New Roman", 15)

def draw_board(game_state):
    ROWS = 9
    COLS = 9
    COL_WIDTH = WIDTH / COLS
    ROW_HEIGHT = HEIGHT / ROWS

    BLANK = " "
    MARKED = "*"
    MINE = "X"
    MARKED_MINE = "M"
    UNMARKED_MINE = "?"
    BAD_MARK = "&"

    full_color = pygame.Color(255, 255, 0, 255)
    blank_color = pygame.Color(0, 0, 255, 255)

    for row in range(ROWS):
        for col in range(COLS):
            rect = pygame.Rect(col * COL_WIDTH, row * ROW_HEIGHT,
                    COL_WIDTH, ROW_HEIGHT)
            screen.fill(full_color, rect)

            # Depending on the state of the square, we display varying images.
            char = game_state["board"][row * COLS + col]
            print(char)
            if char == BLANK:
                screen.fill(blank_color)
            else:
                text = font.render(char, 1, (0, 0, 0))
                screen.blit(text, (col * COL_WIDTH, row * ROW_HEIGHT))
                # screen.blit(text, 100, 100)


def main():
    screen.fill((0, 0, 0))

    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.connect(("localhost", 8000))

        out = dict()
        out['type'] = 'GAME_STATE'
        out['payload'] = ''
        payload = json.dumps(out) + '\n'
        
        s.send(payload.encode('ascii'))
        # Should start receiving game data now

        readfile = s.makefile('r')

    except ConnectionRefusedError:
        logging.error("Unable to connect to server!")
        # return

    while True:
        message = readfile.readline()
        game_state = json.loads(message)
        draw_board(game_state)
        print(game_state)
        pygame.display.update()


if __name__ == "__main__":
    main()
