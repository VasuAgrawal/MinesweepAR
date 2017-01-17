/** @file game_core.c
 * @brief Implements the core logic of the Minesweeper game
 *
 * @author Reid Long (relong)
 * @bug
 */

#include <p1kern.h>
#include <game_title.h>
#include <game_help.h>
#include <game_play.h>
#include <game_endGame.h>
#include <simics.h>

static void (*outputLogic[STATE_HANDLER_COUNT])(void) = {
    [TITLE]     = title_output,
    [HELP]      = help_output,
    [PLAY]      = play_output,
    [END_GAME]  = endGame_output,
};

static gameState_t (*nextStateLogic[STATE_HANDLER_COUNT])(int key) = {     
    [TITLE]     = title_nextState,
    [HELP]      = help_nextState,
    [PLAY]      = play_nextState,
    [END_GAME]  = endGame_nextState, 
};

static gameState_t currentState = TITLE;

/** @brief Play Ball!... or Avoid Mines!*/
void game_run(void) {
    /* We will model the game as a state machine */

    currentState = TITLE;

    reset(0); // Setup game

    while(1) { // Have fun
        
        // do output logic
        (*outputLogic[currentState])();
        
        // Read input
        int key;
        // Spin until we find a key press
        while((key = readchar()) == -1) continue;

        // do next state logic
        gameState_t nextState = (*nextStateLogic[currentState])(key);

        // Transition
        currentState = nextState;

    }
}

/** @brief Handle the timer ticks
 *
 * 1. Blinks the cursor
 * 2. Updates the time
 */
void game_tick(unsigned int numTicks) {

    if(currentState == PLAY) {
        play_tick(numTicks);    
    }
    
}
