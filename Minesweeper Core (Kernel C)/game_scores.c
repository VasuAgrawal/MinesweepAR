/** @file game_scores.c
 * @brief Manages the scores for minesweeper
 *
 * @author Reid Long (relong)
 * @bug
 */

#include <game_scores.h>
#include <fifo_internal.h>
#include <contracts.h>
#include <stdlib.h>
#include <simics.h>

/** @brief Stores the last n scores */
static unsigned int scores[SCORE_LIMIT];

/** @brief points to the last score earned */
static int last;

/** @brief Reports the last n scores
 *
 * The first element int the array is the oldest score, the last element is the 
 *  most recent score
 * @param scoreArray the destination where the scores should be written
 * @return true if scoreArray has the new scores, false otherwise
 */
bool getScores(unsigned int scoreArray[SCORE_LIMIT]) {

    REQUIRES(scoreArray != NULL);

    int index = last;
    for(int i = 0; i < SCORE_LIMIT; i++) {
        scoreArray[i] = scores[index];
        index = safeIncrement(index, SCORE_LIMIT);
    }

    return true;
}

/** @brief Adds a score to the history list
 *
 * @param scoreTime the score to be reported
 * @return true if successful, false otherwise
 */
bool submitScore(unsigned int scoreTime) {
    scores[last] = scoreTime;
    last = safeIncrement(last, SCORE_LIMIT);

    return true;
}

