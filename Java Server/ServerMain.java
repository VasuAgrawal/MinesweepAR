import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ServerMain {

    private static final int SERVER_PORT = 8000;
    private static final String TAG = "ServerMain";

    private static final String JSON_TYPE_FIELD = "type";
    private static final String JSON_PAYLOAD_FIELD = "payload";

    private static final char[][] KEY_MAPPING = {
      {'A', 'J', 'S', 'b', 'k', 't', '2', '!', ')'},
      {'B', 'K', 'T', 'c', 'l', 'u', '3', '@', '-'},
      {'C', 'L', 'U', 'd', 'm', 'v', '4', '#', '_'},
      {'D', 'M', 'V', 'e', 'n', 'w', '5', '$', '='},
      {'E', 'N', 'W', 'f', 'o', 'x', '6', '%', '+'},
      {'F', 'O', 'X', 'g', 'p', 'y', '7', '^', ','},
      {'G', 'P', 'Y', 'h', 'q', 'z', '8', '&', '<'},
      {'H', 'Q', 'Z', 'i', 'r', '0', '9', '*', '.'},
      {'I', 'R', 'a', 'j', 's', '1', ' ', '(', '>'}
    };


    enum ConnectionType {
        RESTART, MARK, KEY_PRESS, GAME_STATE
    };

    private final int port;
    private List<BufferedWriter> listeners = new ArrayList<BufferedWriter>();
    private Minesweeper game = new Minesweeper();

    public ServerMain(int port) {
        this.port = port;
    }

    public static void main(String[] args) {
        ServerMain server = new ServerMain(SERVER_PORT);
        server.waitForConnection();
    }

    public void waitForConnection() {

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            Log.e(TAG, "Couldn't open server socket on port: " + port);
            return;
        }

        Log.i(TAG, "Server Booted Successfully on port: " + port);

        while(true) {
            try {
                Socket clientSocket = serverSocket.accept();

                try {
                    // TODO: Offload this into a new thread
                    handleClient(clientSocket);
                } catch(Exception e) {
                    Log.e(TAG, "Error handling connection", e);
                    try {
                        clientSocket.close();
                    } catch(IOException e2) {
                        Log.e(TAG, "Couldn't close socket", e2);
                    }
                }

            } catch(IOException e) {
                Log.e(TAG, "Error waiting for client socket", e);
                break;
            }
        }

        Log.i(TAG, "Shutting down");
        try {
            serverSocket.close();
        } catch(IOException e) {
            Log.e(TAG, "Couldn't close server socket", e);
        }

    }

    public void handleClient(Socket clientSocket) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String content = bufferedReader.readLine();
        System.out.println(content);

        JSONObject jObject = null;

        try {
            jObject = new JSONObject(content);
        } catch(JSONException e) {
            Log.e(TAG, "Couldn't read JSON object from client", e);
            return;
        }

        ConnectionType type = null;
        try {
            String rawType = jObject.getString(JSON_TYPE_FIELD);
            type = ConnectionType.valueOf(rawType);

            if(type == null) {
                throw new Exception("Invalid type " + rawType);
            }
        } catch(Exception e) {
            Log.e(TAG, "Invalid type field", e);
            return;
        }

        String payloadString = null;

        try {
            payloadString = jObject.getString(JSON_PAYLOAD_FIELD);
        } catch(Exception e) {
            Log.e(TAG, "No payload for type: " + type, e);
        }

        switch(type) {
            case RESTART:
                handleGameRestart();
                break;
            case MARK:
                handleTileMark(payloadString);
                break;
            case KEY_PRESS:
                handleKeyPress(payloadString);
                break;
            case GAME_STATE:
                addGameStateListener(clientSocket);
                return;
            default:
                Log.e(TAG, "Missing case for type: " + type);
        }

        try {
            clientSocket.close();
        } catch(IOException e) {
            Log.e(TAG, "Couldn't close client socket", e);
        }

    }

    public void handleGameRestart() {
        game = new Minesweeper();
        updateListeners("New Game Started!");
    }

    public void handleTileMark(String payload) {

    	String[] location = payload.split(":");
    	
    	int row = Integer.parseInt(location[0]);
    	int column = Integer.parseInt(location[1]);

        game.markMine(row, column);

        updateListeners(String.format("Marked mine at location (%d, %d)!", row, column));

    }

    public void handleKeyPress(String rowcol) {
    	String[] split = rowcol.split(":");
    	int row = Integer.parseInt(split[0]);
    	int col = Integer.parseInt(split[1]);
    	
        if(row >= Minesweeper.BOARD_SIZE || row < 0 ||
           col >= Minesweeper.BOARD_SIZE || col < 0) {
            Log.e(TAG, "Invalid row / col: " + rowcol);
            return;
        }

        Log.i(TAG, String.format("Key press detected at (%d, %d)", row, col));
        game.pressMine(row, col);
        updateListeners(String.format("Stepped on mine at location (%d, %d)!", row, col));
    }

    public void addGameStateListener(Socket clientSocket) {
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
			listeners.add(out);
		} catch (IOException e) {
			Log.e(TAG, "Couldn't create a buffered writer for socket", e);
		}
    }

    public void updateListeners(String cause) {

    	String jsonString = null;
    	try {
    		JSONArray array = new JSONArray(game.getBoard());
    		  
        	
            JSONObject state = new JSONObject().put("cause", cause)
                .put("time", game.getTime())
                .put("board", array)
                .put("mineCount", game.getMineCount())
                .put("status", game.getStatus().toString());

            jsonString = state.toString();
    	} catch(JSONException e) {
    		Log.e(TAG,  "Failed to build JSON Object", e);
    		return;
    	}
    	
        List<BufferedWriter> listenerList = new ArrayList<>(listeners);
        for(BufferedWriter out: listenerList) {
            try {
                out.write(jsonString);
                out.flush();
            } catch(IOException e) {
                Log.e(TAG, "Couldn't write to listener", e);
                listeners.remove(out);
            }
        }
    }

}
