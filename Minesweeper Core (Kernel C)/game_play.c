/** @file game_play.c
 * @brief Implementation of the play state for minesweeper
 *
 * @author Reid Long (relong)
 * @bug
 */

#include <game_core.h>
#include <p1kern.h>
#include <stdio.h>
#include <simics.h>
#include <game_play.h>
#include <time_tools.h>
#include <asm.h>
#include <board.h>
#include <minesweeper_levels.h>
#include <contracts.h>
#include <stdbool.h>
#include <game_scores.h>

#define CURSOR_BLINK_PERIOD 50

typedef enum {PAUSED_GAME, IN_GAME, PRE_GAME, LOSE, WIN} playState_t;

static int currentLevel = -1;

// CAUTION: DRIVEN BY INTERRUPT!
static int ticksInGame = 0;

// CAUTION: USED BY INTERRUPT!
static playState_t currentState = PRE_GAME;
static bool isDrawing = false;

static bool optimizeDraw = false;

#define PLAY_COLOR (FGND_LGRAY | BGND_BLACK)

static int secondsInGame();

/************************
 *      NEXT_STATE_LOGIC
 ************************/

static void internal_nextState(int key);

/** @brief Implements the core behaviors of the play state
 *
 * @param key the key that was most recently pressed
 * @return the next gameState to go to
 */
gameState_t play_nextState(int key) {
    REQUIRES(0 <= currentLevel);
    
    if(currentState == LOSE) {
        // If the user just lost, their next action is a reset
        key = RESTART_KEY;
    } else if(currentState == WIN) {
        // If the user just won, their next action is a skip
        key = SKIP_KEY;
        // Don't forget to report the score
        submitScore(secondsInGame());
    }

    gameState_t nextState = PLAY;
    int nextLevel; // Declaring up here to make the compiler happy
    switch(key) {
        case HELP_KEY:
            // Help the user out if they go into the help screen
            internal_nextState(PAUSE_KEY); 
            nextState = HELP;
            break;
        case EXIT_KEY:
            reset(0);   // Reset all state to the initial conditions
            nextState = TITLE;   // They gave up, go back to the lauch screen
            break;
        case RESTART_KEY:
            reset(currentLevel);  // Just reset back to the start of this level
            nextState = PLAY; // Keep on playing
            break;
        case SKIP_KEY:
            nextLevel = currentLevel + 1;
            if(nextLevel >= numlevels) {
                reset(0);
                nextState = END_GAME;
            } else {
                reset(currentLevel + 1); // Cheaters always win....    
                nextState = PLAY;    // We'll be nice and let them play
            }
            break;
        case LEFT_KEY:
        case UP_KEY:
        case DOWN_KEY:
        case RIGHT_KEY:
            // WARNING: This is pretty fragile. 
            if(currentState == IN_GAME || currentState == PRE_GAME) {
                moveCursor(key);    
            }
            // If the user tries to move the cursor while paused, ignore them
            // The currentState should never be LOSE while the key is a move
            ASSERT(currentState != LOSE);
            nextState = PLAY;
            break;
        case OPEN_KEY:
        case MARK_KEY:
        case PAUSE_KEY:
        case RESUME_KEY:
            internal_nextState(key);
            nextState = PLAY;
            break;
        default:
            // Somebody is just randomly smashing the keyboard.
            // Please remove the cat
            nextState = PLAY;
            break;
    }
    if(nextState != PLAY) {
        optimizeDraw = false;
    }

    return nextState;

}

/** @brief Determines if a key press is an action key
 *
 * @param key the key that was pressed
 * @return true if the key was an action key, false otherwise
 */
static bool isActionKey(int key) {
    switch(key) {
        case OPEN_KEY:
        case MARK_KEY:
            return true;
        default:
            return false;
    }
}

/** @brief Converts an actionResult_t into a playState_t
 *
 * @param actionResult the actionResult to be converted
 * @return the converted actionResult
 */
static playState_t processActionResult(actionResult_t actionResult) {

   switch(actionResult) {
        case MINE:
            return LOSE;
        case GOOD_PLAY:
            return IN_GAME;
        case ALL_CLEAR:
            return WIN;
        default:
            ASSERT(false);
            return IN_GAME; // How did you get here? 
   }

}

/** @brief Manage internal FSM
 *
 * @param key the key that was most recently pressed
 */
static void internal_nextState(int key) {
    REQUIRES(
        key == OPEN_KEY     ||
        key == MARK_KEY     ||
        key == PAUSE_KEY    ||
        key == RESUME_KEY);

    // We should never be able to transition out of LOSE or WIN.
    // Leaving lose or win requires a RESET
    ASSERT(currentState != LOSE);
    ASSERT(currentState != WIN);

    playState_t nextState = currentState;
    
    switch(currentState) {
        case PAUSED_GAME:
            if(key == RESUME_KEY) {
                nextState = IN_GAME;
            } 
            break;
        case PRE_GAME:
            if(key == PAUSE_KEY) {
                // Don't let the user pause in PRE_GAME. 
                // They haven't actually started playing yet
                break;
            }
            // WARNING: FALL THROUGH ENABLED!
        case IN_GAME:
            if(key == PAUSE_KEY) {
                ASSERT(currentState == IN_GAME);
                nextState = PAUSED_GAME;
            } else if(isActionKey(key)) {
                actionResult_t result = applyAction(key);
                nextState = processActionResult(result);
            }
            break;
        default:
            // This should never be reachable
            ASSERT(false);
            break;
    }

    // This is used in the interrupt, but since it is a single word store
    // we should be safe to not disable interrupts
    currentState = nextState;
}

/** @brief Reset the game to a specific level
 *
 * @param level the level to reset to
 */
void reset(int level) {

    currentLevel = level;

     // This is used in the interrupt, but since it is a single word store
    // we should be safe to not disable interrupts
    currentState = PRE_GAME;
    
    // Even though this is driven by the interrupt, since we are in the PRE_GAME
    // state, we can conclude that we don't need to disable interrupts
    // since the tick counter only increases while in game
    ticksInGame = 0; 

    initBoard(currentLevel);

    optimizeDraw = false;
    
}

/************************
 *      OUTPUT_LOGIC
 ************************/


static void updateHeader() {
    int lastRow, lastCol;
    get_cursor(&lastRow, &lastCol);
    set_cursor(0,0);
    char buffer[TIME_BUFFER_SIZE];
    convertToTime(secondsInGame(), buffer, TIME_BUFFER_SIZE);
    printf("LEVEL: %-10d TIME: %-10s MINES: %03d\n", 
        (currentLevel + 1), buffer, getRunningMineCount());

    set_cursor(lastRow, lastCol);
}


/** @brief Displays the play screen */
 void play_output(void) {

    disable_interrupts();
    isDrawing = true;
    enable_interrupts();

    if(!optimizeDraw) {
        set_term_color(PLAY_COLOR);
        clear_console();
        hide_cursor();
        optimizeDraw = true; // Next time we are optimal
    } else {
        set_cursor(0, 0);
    }
    
    updateHeader();
    printf("\n"); // Move the cursor down
    
    switch(currentState) {
        case PAUSED_GAME:
            printf("GAME PAUSED! Press '%c' to resume\n", RESUME_KEY);
            drawBoard(BLUR);
            break;
        case IN_GAME:
        case PRE_GAME:
            printf("Press '%c' for help!\n", HELP_KEY);
            drawBoard(NORMAL);
            break;
        case LOSE:
            printf("Game Over! Press any key to try again\n");
            drawBoard(REVEAL);
            break;
        case WIN:
            printf("You Win! Press any key to advance to the next level\n");
            drawBoard(NORMAL);
            break;
        default:
            ASSERT(false);
            // How did you get here?
    }

    disable_interrupts();
    isDrawing = false;
    enable_interrupts();
 }

/************************
 *      TIMER TICK
 ************************/

/** @brief Timer Tick for Play State
 *
 * @param numTicks the number of ticks since the app launched
 */
void play_tick(unsigned int numTicks) {
    
    if(currentState == IN_GAME) {
        ++ticksInGame;
        
    }

    if(!isDrawing) {
        // Toggle the cursor
        if((currentState == IN_GAME || currentState == PRE_GAME) && 
            numTicks % CURSOR_BLINK_PERIOD == 0) {
            blinkCursor();
        } 

        if(numTicks % 100 == 0) {
            updateHeader();
        }
    }
    

    
}

/** @brief Converts ticks to seconds
 *
 * @return the number of seconds since the game started
 */
static int secondsInGame() {
    // Since this is a word-size read only operation we can safely assume
    // that we do not need to disable_interrupts here
    int localTicks = ticksInGame;
    return localTicks / 100;
}