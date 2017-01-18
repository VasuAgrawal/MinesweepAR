import java.util.Arrays;
import java.util.Random;

public class Minesweeper {
    public enum State {
        IN_GAME, WIN, LOSS
    };
    
    public static final int BOARD_SIZE = 9;
    public static final int MAX_INDEX = BOARD_SIZE * BOARD_SIZE;
    private static final int MINE_COUNT = 10;
    private static final String TAG = "Minesweeper";
    
    enum SpaceSymbol {
    	BLANK(' '), MARKED('*'), MINE('X'), MARKED_MINE('M'), UNMARKED_MINE('?'), BAD_MARK('&');
    	
    	private char symbol;
    	SpaceSymbol(char symbol) {
    		this.symbol = symbol;
    	}
    	
    	public char getSymbol() {
    		return this.symbol;
    	}
    	
    }
    
    private long startTime;
    private long endTime;
    private char[] visibleBoard;
    private char[] map;
    private int mineCount;
    private int totalMines;
    private State state;
    private int hiddenSpaces;

    public int getTime() {
    	
    	long currentTime = this.endTime;
    	
    	if(currentTime == -1) {
    		currentTime = System.currentTimeMillis();
    	}
    	
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
    	if(state != State.IN_GAME) {
    		Log.i(TAG, "Game already over, not handling mark request");
    		return;
    	}
    	
    	int index = getIndex(row, column);
    	
    	char userValue = this.visibleBoard[index];
    	
    	if(userValue == SpaceSymbol.MARKED.getSymbol()) {
    		Log.i(TAG, "Unmarking board: " + prettyLocation(index));
    		this.mineCount++;
    		this.visibleBoard[index] = SpaceSymbol.BLANK.getSymbol();
    	} else if(userValue == SpaceSymbol.BLANK.getSymbol()) {
    		Log.i(TAG, "Marking board: " + prettyLocation(index));
    		this.mineCount--;
    		this.visibleBoard[index] = SpaceSymbol.MARKED.getSymbol();
    	} else {
    		Log.i(TAG, 
    				String.format("User not allowed to mark on top of symbol %c @ %s", 
    						userValue, prettyLocation(index)));
    	}
    }
    
    
    private void handleEndGame(State endState) {
    	this.state = endState;
    	
    	for(int index = 0; index < MAX_INDEX; index++) {
    		if(this.visibleBoard[index] == SpaceSymbol.MARKED.getSymbol()) {
    			if(this.map[index] == SpaceSymbol.MINE.getSymbol()) {
    				this.visibleBoard[index] = SpaceSymbol.MARKED_MINE.getSymbol();
    			} else {
    				this.visibleBoard[index] = SpaceSymbol.BAD_MARK.getSymbol();
    			}
    		} else if(this.visibleBoard[index] == SpaceSymbol.BLANK.getSymbol()) {
    			if(this.map[index] == SpaceSymbol.MINE.getSymbol()) {
    				this.visibleBoard[index] = SpaceSymbol.UNMARKED_MINE.getSymbol();
    			} else {
    				this.visibleBoard[index] = this.map[index];
    			}
    		}
    	}
    	
    	this.endTime = System.currentTimeMillis();
    	
    	Log.i(TAG, "Game over @ " + this.endTime);
    	Minesweeper.logBoard(this.visibleBoard);
    	
    }
    
    
    
    private void recursiveOpen(int row, int column) {
    	if(0 <= row && row < BOARD_SIZE && 0 <= column && column < BOARD_SIZE 
    			&& this.state == State.IN_GAME) {
    		int index = getIndex(row, column);
    		char mapValue = this.map[index];
    		
    		this.visibleBoard[index] = mapValue;
    		this.hiddenSpaces--;
    	
    		if(mapValue == SpaceSymbol.MINE.getSymbol()) {
    			
    			handleEndGame(State.LOSS);
    			return;
    		} else if (mapValue == '0') {
    			for(int deltaRow = -1; deltaRow <= 1; deltaRow++) {
    				for(int deltaCol = -1; deltaCol <= 1; deltaCol++) {
    					if(deltaRow == 0 && deltaCol == 0) {
    						continue;
    					}
    					int neighborRow = row + deltaRow;
    					int neighborCol = column + deltaCol;
    					recursiveOpen(neighborRow, neighborCol);
    				}
    			}
    		}
    		
    		if(this.hiddenSpaces == this.totalMines) {
    			handleEndGame(State.WIN);
    		}
    		
    	}
    }

    public void pressMine(int row, int column) {
    	if(state != State.IN_GAME) {
    		Log.i(TAG, "Game already over, not handling mine press request");
    		return;
    	}
    	
    	int index = getIndex(row, column);
    	char userValue = this.visibleBoard[index];
    	
    	if(userValue == SpaceSymbol.MARKED.getSymbol()) {
    		Log.i(TAG, "User not allowed to step on a marked mine @ " + prettyLocation(index));
    	} else if(userValue == SpaceSymbol.BLANK.getSymbol()) {
    		Log.i(TAG, "User opening cell @ " + prettyLocation(index));
    		recursiveOpen(row, column);
    	} else {
    		Log.i(TAG, "Skipping open, space already opened");
    	}
    	
    }
    
   
    
    private static int getIndex(int row, int column) {
    	return row * BOARD_SIZE + column;
    }
    
    private static int getRow(int index) {
    	return index / BOARD_SIZE;
    }
    
    private static int getColumn(int index) {
    	return index % BOARD_SIZE;
    }
    
    private static String prettyLocation(int index) {
    	return String.format("%d (%d, %d)", index, getRow(index), getColumn(index));
    }
    
    private static void logBoard(char[] board) {
    	
    	Log.i(TAG, "Printing Board");
    	for(int index = 0; index < MAX_INDEX; index += BOARD_SIZE) {
    		for(int subIndex = 0; subIndex < BOARD_SIZE; subIndex++) {
    			System.out.print(board[index + subIndex]);
    		}
    		System.out.println();
    	}
    }
    
    private void generateMap() {
    	
    	Arrays.fill(this.map, '0');
    	
    	
    	Random r = new Random();
    	for(int i = 0; i < MINE_COUNT; i++) {
    		int mineLocation = r.nextInt(MAX_INDEX);
    		
    		if(this.map[mineLocation] == SpaceSymbol.MINE.getSymbol()) {
    			Log.i(TAG, "Mine already exists at index: " + mineLocation);
    			this.mineCount--;
    		} else {
    			Log.i(TAG, String.format("Mine placed at index %s", prettyLocation(mineLocation)));
    			int row =  getRow(mineLocation);
    			int column = getColumn(mineLocation);
    			
    			this.map[mineLocation] = SpaceSymbol.MINE.getSymbol();
    					
    			for(int deltaRow = -1; deltaRow <= 1; deltaRow++) {
    				for(int deltaCol = -1; deltaCol <= 1; deltaCol++) {
    					int neighborRow = row + deltaRow;
    					int neighborCol = column + deltaCol;
    					
    					int neighborIndex = getIndex(neighborRow, neighborCol);
    						
    					if(neighborIndex < 0 || neighborIndex >= MAX_INDEX) {
    						continue;
    					}
    					
    					if(this.map[neighborIndex] == SpaceSymbol.MINE.getSymbol()) {
    						Log.i(TAG, "Adjacent Mine Detected");
    					} else {
    						this.map[neighborIndex]++;
    					}
    				}
    			}
    		}
    	}
    	
    	logBoard(this.map);
    	
    }

    public Minesweeper() {
    	this.startTime = System.currentTimeMillis();
    	this.visibleBoard = new char[MAX_INDEX];
    	this.map = new char[MAX_INDEX];
    	this.mineCount = MINE_COUNT;
    	this.state = State.IN_GAME;
    	this.hiddenSpaces = MAX_INDEX;
    	this.endTime = -1;
    	
    	Arrays.fill(this.visibleBoard, SpaceSymbol.BLANK.getSymbol());
    	
    	generateMap(); 
    	
    	// Needs to be done after generating the map in case there are some duplicate mine placements
    	this.totalMines = this.mineCount;
    	
    	Log.i(TAG, "Game Started @ " + this.startTime);
    	
    	
    }
}
