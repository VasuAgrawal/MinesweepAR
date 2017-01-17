public class Minesweeper {
    public enum State {
        IN_GAME, WIN, LOSS
    };
    
    public static final int BOARD_SIZE = 9;
    
    private long startTime;
    private char[] visibleBoard;
    private int mineCount;
    private State state;

    public int getTime() {
    	long currentTime = System.currentTimeMillis();
    	
    	long seconds = (currentTime - startTime) / 1000;
    	
    	return (int)seconds;
    }

    public char[] getBoard() {
    	return visibleBoard;
    }

    public int getMineCount() {
        return mineCount;
    }

    public State getStatus() {
        return state;

    }

    public void markMine(int row, int column) {

    }

    public void pressMine(int row, int column) {

    }

    public Minesweeper() {
    	this.startTime = System.currentTimeMillis();
    	this.visibleBoard = new char[BOARD_SIZE * BOARD_SIZE];
    	this.mineCount = 0;
    	this.state = State.IN_GAME;
    }
}
