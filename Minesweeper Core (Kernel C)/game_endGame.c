/** @file game_endGame.c
 * @brief Implementation of the End Game state for minesweeper
 *
 * @author Reid Long (relong)
 * @bug
 */

#include <game_core.h>
#include <video_defines.h>
#include <stdio.h>
#include <p1kern.h>

 #define END_GAME_COLOR (FGND_BLUE | BGND_BLACK)

/** @brief Impements the next state logic of the End Game state
 *
 * @param key the key that was most recently pressed
 * @return the next gameState to go to
 */
gameState_t endGame_nextState(int key) {

    // Always go back to the beginning! 
    return TITLE;
}

/** @brief Displays the end game screen */
void endGame_output(void) {
    set_term_color(END_GAME_COLOR);
    clear_console();
    hide_cursor();
    printf("\n\n");
    printf("                CONGRATULATIONS! YOU BEAT EVERY LEVEL!!!!!!\n");
    printf("\n\n");
    printf("                Now..... Can you do it faster?\n");
    printf("\n\n");
    printf("                Press any key to play again!\n");

}