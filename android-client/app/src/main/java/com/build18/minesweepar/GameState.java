package com.build18.minesweepar;


import android.util.Log;

/**
 * Created by Reid on 1/18/17.
 */

public class GameState {

    public enum GameStatus { IN_GAME, WIN, LOSS};

    private static final String TAG = "GameState";


    private final String cause;
    private final int seconds;
    private final char[] board;
    private final int mineCount;
    private final GameStatus status;
    private final int size;

    public GameState(String cause, int seconds, char[] board, int mineCount, String status) {
        this.cause = cause;
        this.seconds = seconds;
        this.board = board;
        this.mineCount = mineCount;
        this.status = GameStatus.valueOf(status);
        this.size = (int)Math.sqrt(this.board.length);

        assert(this.size * this.size == this.board.length);

        if(this.status == null) {
            Log.e(TAG, "Invalid status: " + status);
        }
    }

    public String getCause() {
        return cause;
    }

    public int getSecondsEllapsed() {
        return seconds;
    }

    public char getSymbolAtLocation(int row, int column) {
        int index = row * size + column;
        return this.board[index];
    }

    public int getMineCount() {
        return mineCount;
    }

    public GameStatus getStatus() {
        return status;
    }

    public int boardSize() {
        return size;
    }

}
