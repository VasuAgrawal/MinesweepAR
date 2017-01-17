public class Minesweeper {
    public enum State {
        IN_GAME, WIN, LOSS
    };

    public int getTime() {
        return 0;
    }

    public char[] getBoard() {
        return new char[0];
    }

    public int getMineCount() {
        return 1;

    }

    public State getStatus() {
        return State.IN_GAME;

    }

    public void markMine(int row, int column) {

    }

    public void pressMine(int row, int column) {

    }

    public Minesweeper() {

    }
}
