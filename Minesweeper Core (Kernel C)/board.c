/** @file board.c
 * @brief Implements the game board logic
 *
 * @author Reid Long (relong)
 * @bug
 */

#include <stdlib.h>
#include <contracts.h>
#include <minesweeper_levels.h>
#include <board.h>
#include <asm.h>
#include <stdbool.h>
#include <video_defines.h>
#include <p1kern.h>
#include <game_play.h>

typedef struct {
    int width;
    int height;
    char** userBoard;
    
    int cursorRow;
    int cursorCol;
    bool isBlink;

    int runningMineCount;
    int totalMines;
    int hiddenSpaces;
    
    int level;
    
} board_t;

static board_t* currentBoard = NULL;

static drawMode_t currentMode = NORMAL;
static int lastRowOffset = 0;
static int lastColOffset = 0;

#define EMPTY_CHARACTER ' '
#define BOARD_BACKGROUND BGND_LGRAY

/** @brief Initalizes the board
 *
 * @param level the level to setup
 */
void initBoard(int level) {
    REQUIRES(0 <= level && level < numlevels);

    // Release all pointers
    if(currentBoard != NULL) {
        for(int row = 0; row < currentBoard->height; row++) {
            free(currentBoard->userBoard[row]);
        }
        free(currentBoard->userBoard);
        free(currentBoard); 
    }

    board_t* board = malloc(sizeof(board_t));
    board->width = levels[level]->width;
    board->height = levels[level]->height;
    board->userBoard = calloc(board->height, sizeof(char*));
    for(int row = 0; row < board->height; row++) {
        board->userBoard[row] = calloc(board->width, sizeof(char));
        for(int col = 0; col < board->width; col++) {
            board->userBoard[row][col] = EMPTY_CHARACTER;
        }
    }

    board->cursorRow = 0;
    board->cursorCol = 0;
    board->isBlink = false;

    board->runningMineCount = levels[level]->num_mines;
    board->totalMines = levels[level]->num_mines;
    board->hiddenSpaces = board->width * board->height;

    board->level = level;

    currentBoard = board;
}


static void getContents_blur(int, int, char*, int*);
static void getContents_reveal(int, int, char*, int*);
static void getContents_normal(int,int, char*, int*);

static void (*getContents[DRAW_MODE_COUNT])(int, int, char*, int*) = {
    [BLUR] = getContents_blur,
    [REVEAL] = getContents_reveal,
    [NORMAL] = getContents_normal,
};

/** @brief determines the contents for the blur mode
 * @param row the row to print
 * @param col the col to print
 * @param characterOut the destination to put the character
 * @param colorOut the destination to put the color
 */
static void getContents_blur(int row, int col, char* characterOut, 
    int* colorOut) {
    *characterOut = EMPTY_CHARACTER;
    *colorOut = FGND_LGRAY | BOARD_BACKGROUND;
}

/** @brief computes the index into the map
 * @param row the row
 * @param col the column
 * @return the index coresponding to row, col
 */
static int calculateIndex(int row, int col) {
    return row * currentBoard->width + col;
}

/** @brief determines the contents for the reveal mode
 * @param row the row to print
 * @param col the col to print
 * @param characterOut the destination to put the character
 * @param colorOut the destination to put the color
 */
static void getContents_reveal(int row, int col, char* characterOut, 
    int* colorOut) {

    int index = calculateIndex(row, col);
    char mapValue = levels[currentBoard->level]->map[index];
    char localValue = currentBoard->userBoard[row][col];

    int foreground;
    if(mapValue == MINE_CHARACTER || localValue == MARK_CHARACTER) {
        if(localValue == MARK_CHARACTER) {
            *characterOut = MARK_CHARACTER;
        } else {
            *characterOut = MINE_CHARACTER;
        }

        if(localValue == MARK_CHARACTER && mapValue == MINE_CHARACTER) {
            // Mine that was marked
            foreground = FGND_BGRN;
        } else if(localValue == MARK_CHARACTER) {
            // Non-Mine that was marked
            foreground = FGND_RED;
        } else if(currentBoard->cursorRow == row && 
            currentBoard->cursorCol == col) {
            // Mine that was pressed
            foreground = FGND_BMAG;
        } else {
            // Is a mine that wasn't marked
            foreground = FGND_MAG;
        }

        *colorOut = foreground | BOARD_BACKGROUND;
    } else {
        getContents_normal(row, col, characterOut, colorOut);
    }
}

#define ADJACENT_COUNT 9
static int colorList[ADJACENT_COUNT] = {
    FGND_WHITE, FGND_BLACK, FGND_BLUE, FGND_GREEN, FGND_CYAN,
    FGND_RED, FGND_BRWN, FGND_BBLUE, FGND_PINK
};

/** @brief determines the contents for the normal mode
 * @param row the row to print
 * @param col the col to print
 * @param characterOut the destination to put the character
 * @param colorOut the destination to put the color
 */
static void getContents_normal(int row, int col, char* characterOut, 
    int* colorOut) {
    char value = currentBoard->userBoard[row][col];

    int foreground;
    int background = BOARD_BACKGROUND;
    switch(value) {
        case EMPTY_CHARACTER:
            foreground = FGND_LGRAY;
            break;
        case MINE_CHARACTER:
            ASSERT(false);
            // Why is a mine shown during the normal mode?
            break;
        case MARK_CHARACTER:
            foreground = FGND_DGRAY;
            break;
        default:
            ; // Make the compiler happy
            int offset = value - '0';
            ASSERT(0 <= offset && offset < ADJACENT_COUNT);
            foreground = colorList[offset];
            break;
    }

    *characterOut = value;
    *colorOut = foreground | background;
}


void drawBoard(drawMode_t mode) {
    REQUIRES(currentBoard != NULL);

    currentMode = mode;

    int rowOffset, colOffset;
    get_cursor(&rowOffset, &colOffset);
    lastRowOffset = rowOffset;
    lastColOffset = colOffset;

    int width = currentBoard->width;
    int height = currentBoard->height;
    for(int row = 0; row < height; row++) {
        for(int col = 0; col < width; col++) {
            char value;
            int color;
            (*getContents[mode])(row, col, &value, &color);
            draw_char(row + rowOffset, col+colOffset, value, color);
        }
    }

    currentBoard->isBlink = false;

}

// This will toggle the background color between LGRAY and MAG
#define BLINK_MASK 0xDF;

/** @brief Blinks the cursor
 *
 * This should not be called while the board is in drawing mode
 */
void blinkCursor(void) {
    REQUIRES(currentBoard != NULL);
    REQUIRES(currentMode == NORMAL);
    
    int row = currentBoard->cursorRow;
    int col = currentBoard->cursorCol;

    char value;
    int color;

    getContents_normal(row, col, &value, &color);

    if(currentBoard->isBlink) {
       currentBoard->isBlink = false; 
    } else {
        color &= BLINK_MASK;
        currentBoard->isBlink = true;
    }
    draw_char(row + lastRowOffset, col + lastColOffset, value, color);
    
}

/* REQUIRES: key == {UP, LEFT, RIGHT, DOWN} */
void moveCursor(int key) {
    REQUIRES(
        key == UP_KEY ||
        key == DOWN_KEY ||
        key == LEFT_KEY ||
        key == RIGHT_KEY);
    REQUIRES(currentBoard != NULL);

    int nextRow = currentBoard->cursorRow;
    int nextCol = currentBoard->cursorCol;
    switch(key) {
        case UP_KEY:
            --nextRow;
            break;
        case DOWN_KEY:
            ++nextRow;
            break;
        case LEFT_KEY:
            --nextCol;
            break;
        case RIGHT_KEY:
            ++nextCol;
            break;
        default:
            ASSERT(false);
            // How did you get here?
    }

    if(0 <= nextRow && nextRow < currentBoard->height &&
        0 <= nextCol && nextCol < currentBoard->width) {
        // Everything looks good

        // This is kinda funky. We need to make sure that the blink effect 
        // isn't trigged while moving
        disable_interrupts();
        currentBoard->cursorRow = nextRow;
        currentBoard->cursorCol = nextCol;
        enable_interrupts();
    }

}

static actionResult_t recursiveOpen(int row, int col);


/** @brief Helper function for the recursive open
 *
 * @param row the row to check
 * @param col the column to check
 */
static void doOpen(int row, int col) {
    // Hit all adjacent squares
    for(int deltaRow = -1; deltaRow <= 1; deltaRow += 1) {
        for(int deltaCol = -1; deltaCol <= 1; deltaCol += 1) {
            if(deltaCol != 0 || deltaRow != 0) {
                actionResult_t result = recursiveOpen(
                    row + deltaRow, col + deltaCol);

                (void)result; // Make the compiler happy
                // The recursion shouldn't ever fail
                ASSERT(result == GOOD_PLAY); 
            }
        }
    }
}

/** @brief Recursively opens all zero mine cells
 *
 * @param row the row to check
 * @param col the col to check
 * @return the result of the open
 */
static actionResult_t recursiveOpen(int row, int col) {
    if(0 <= row && row < currentBoard->height &&
        0 <= col && col < currentBoard->width &&
        currentBoard->userBoard[row][col] == EMPTY_CHARACTER) {
        // We are in bounds

        int index = calculateIndex(row, col);
        char mapValue = levels[currentBoard->level]->map[index];
        switch(mapValue) {
            case MINE_CHARACTER:
                return MINE;
            case ZERO_CHARACTER:

                // Marking the cell before recursing is extremely important!
                currentBoard->userBoard[row][col] = mapValue;
                doOpen(row, col);
                // WARNING: Fall through enabled! 
            default:
                // We found a non-zero number
                currentBoard->userBoard[row][col] = mapValue;
                --currentBoard->hiddenSpaces;

                if(currentBoard->hiddenSpaces == currentBoard->totalMines) {
                    return ALL_CLEAR;
                } else {
                    return GOOD_PLAY;
                }
        }
    } else {
        // Out of bounds, just ignore
        return GOOD_PLAY;
    }
}

/** @brief Applies a move action on the game
 * 
 * @param key the key that was pressed
 * @return the action result
 */
actionResult_t applyAction(int key) {
    REQUIRES(currentBoard != NULL);
    REQUIRES(key == OPEN_KEY || key == MARK_KEY);

    int row = currentBoard->cursorRow;
    int col = currentBoard->cursorCol;

    char userValue = currentBoard->userBoard[row][col];

    if(userValue == MARK_CHARACTER) {
        if(key == MARK_KEY) {
            currentBoard->userBoard[row][col] = EMPTY_CHARACTER;
            ++currentBoard->runningMineCount;
        } else {
            ASSERT(key == OPEN_KEY);
            // Don't let the user open a marked cell
        }
        return GOOD_PLAY;
    } else if(userValue == EMPTY_CHARACTER) {
        if(key == MARK_KEY) {
            currentBoard->userBoard[row][col] = MARK_CHARACTER;
            --currentBoard->runningMineCount;
            return GOOD_PLAY;
        } else {
            ASSERT(key == OPEN_KEY);
            return recursiveOpen(row, col);
        }
    } else {
        // This must have been an already opened space
        // Just ignore the action
        return GOOD_PLAY;
    }
}

/** @brief returns the number of mines
 *
 * @return the number of mines (user vision)
 */
int getRunningMineCount(void) {
    REQUIRES(currentBoard != NULL);
    return currentBoard->runningMineCount;
}