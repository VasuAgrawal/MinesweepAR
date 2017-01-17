/** @file game_title.c
 * @brief Implementation of the title state for minesweeper
 *
 * @author Reid Long (relong)
 * @bug
 */

#include <video_defines.h>
#include <p1kern.h>
#include <game_core.h>
#include <stdio.h>
#include <game_scores.h>
#include <stdbool.h>
#include <contracts.h>
#include <time_tools.h>
#include <game_play.h>

// Start writing text at the top third of the screen
#define START_OF_TEXT_ROW (CONSOLE_HEIGHT / 3)

// The terminal color for the title screen
#define TITLE_COLOR (FGND_BLUE | BGND_BLACK)

#define DEVELOPER "Reid"



/** @brief Displays the title screen */
void title_output(void) {

    set_term_color(TITLE_COLOR);
    clear_console();
    hide_cursor();
    set_cursor(START_OF_TEXT_ROW, 0);

    printf("                  WELCOME TO MINESWEEPER!\n");
    printf("                  Developer: %s\n", DEVELOPER);
    printf("                  High Scores!\n");

    unsigned int scores[SCORE_LIMIT];
    bool result = getScores(scores);
    (void)result; // Make the compiler happy
    ASSERT(result);
    for(int i = 0; i < SCORE_LIMIT; i++) {
        char buffer[TIME_BUFFER_SIZE];
        convertToTime(scores[SCORE_LIMIT - i - 1], buffer, TIME_BUFFER_SIZE);
        printf("                         #%d: %s\n", (i+1), buffer);
    }
    printf("\n");
    printf("                  Press '%c' for help\n", HELP_KEY);
    printf("                  Press any key to continue\n");
}

/** @brief Determines the next state
 * 
 * @param key the key that was pressed
 * @return the next state
 */
gameState_t title_nextState(int key) {
    switch(key) {
        case HELP_KEY: 
            return HELP;
        default:
            return PLAY;
    }
}