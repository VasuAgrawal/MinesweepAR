/** @file game_help.c
 * @brief Implementation of the help state for minesweeper
 *
 * @author Reid Long (relong)
 * @bug
 */

#include <game_core.h>

#include <p1kern.h>
#include <stdio.h>
#include <video_defines.h>
#include <game_play.h>

#define HELP_COLOR (FGND_BLACK | BGND_RED)

/** @brief Determines the next state to transition to
 *
 * After viewing the help screen, the player will always be sent to the 
 * play screen
 *
 * @param key the key that was most recently pressed
 * @return the next gameState to go to
 */
gameState_t help_nextState(int key) {
    return PLAY;
}

/** @brief Display the help screen */
void help_output(void) {

    set_term_color(HELP_COLOR);
    clear_console();
    hide_cursor();

    printf("                Welcome to the Help Screen!\n");
    printf("'%c':        Displays this message\n", HELP_KEY);
    printf("'%c':        Pauses the game\n", PAUSE_KEY);
    printf("'%c':        Resumes the game\n", RESUME_KEY);
    printf("'%c':        Exit the game (go back to title screen)\n", EXIT_KEY);
    printf("'%c':        Restart the level\n", RESTART_KEY);
    printf("'%c':        Skip to the next level\n", SKIP_KEY);
    printf("\n");
    printf("                Game Play Keys!\n");
    printf("'%c':        Move cursor up\n", UP_KEY);
    printf("'%c':        Move cursor left\n", LEFT_KEY);
    printf("'%c':        Move cursor down\n", DOWN_KEY);
    printf("'%c':        Move cursor right\n",RIGHT_KEY);
    printf("'%c':        Open Cell (Click/Select/Activate)\n", OPEN_KEY);
    printf("'%c':        Mark/Unmark cell\n", MARK_KEY);
    printf("\n");
    printf("Press any key to start playing!\n");

}