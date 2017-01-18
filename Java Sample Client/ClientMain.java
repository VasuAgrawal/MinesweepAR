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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ClientMain {

    private static final int SERVER_PORT = 8000;
    private static final String SERVER_HOST = "localhost";
    private static final String TAG = "ClientMain";

    private static final String JSON_TYPE_FIELD = "type";
    private static final String JSON_PAYLOAD_FIELD = "payload";

    enum ConnectionType {
        RESTART, MARK, KEY_PRESS, GAME_STATE
    };

    private static void attachListener() {
    	Socket gameStateSocket = null;
    	try {
    		gameStateSocket = new Socket(SERVER_HOST, SERVER_PORT);
			
		} catch (IOException e) {
			Log.e(TAG, "Couldn't connect to server", e);
			return;
		}
    	
    	try {
			JSONObject request = new JSONObject()
					.put(JSON_TYPE_FIELD, ConnectionType.GAME_STATE.toString())
					.put(JSON_PAYLOAD_FIELD, "");
			
			String jsonString = request.toString();
			
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(gameStateSocket.getOutputStream()));
			out.write(jsonString);
			out.write("\n");
			out.flush();
			
			Log.i(TAG, "Successfully connected to server");
			
		} catch (JSONException e) {
			Log.e(TAG, "Unable to create JSON Payload");
		} catch (IOException e) {
			Log.e(TAG, "Unable to send json payload");
		}
    	
    	final Socket listenSocket = gameStateSocket;
    	
    	Thread t = new Thread(new Runnable() {
    		public void run() {
    			Log.i(TAG, "Listerner Thread Starting up");
    			try {
					BufferedReader in = new BufferedReader(new InputStreamReader(listenSocket.getInputStream()));
					
					while(true) {
						String line = in.readLine();
						Log.i(TAG, "Game State Update:" + line);
					}
				} catch (IOException e) {
					Log.i(TAG, "Couldn't get the input stream");
				}
    			
    		}
    	});
    	
    	t.start();
    }
    
    private static void sendKeyPress() {
    	Socket socket = null;
    	try {
    		socket = new Socket(SERVER_HOST, SERVER_PORT);
			
		} catch (IOException e) {
			Log.e(TAG, "Couldn't connect to server", e);
			return;
		}
    	
    	try {
			JSONObject request = new JSONObject()
					.put(JSON_TYPE_FIELD, ConnectionType.KEY_PRESS.toString())
					.put(JSON_PAYLOAD_FIELD, "7:8");
			
			String jsonString = request.toString();
			
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			out.write(jsonString);
			out.write("\n");
			out.flush();
			
			Log.i(TAG, "Sent Key press");
			
			socket.close();
			
		} catch (JSONException e) {
			Log.e(TAG, "Unable to create JSON Payload");
		} catch (IOException e) {
			Log.e(TAG, "Unable to send json payload");
		}
    	
    	
    }
    
    private static void restartGame() {
    	Socket socket = null;
    	try {
    		socket = new Socket(SERVER_HOST, SERVER_PORT);
			
		} catch (IOException e) {
			Log.e(TAG, "Couldn't connect to server", e);
			return;
		}
    	
    	try {
			JSONObject request = new JSONObject()
					.put(JSON_TYPE_FIELD, ConnectionType.RESTART.toString())
					.put(JSON_PAYLOAD_FIELD, "");
			
			String jsonString = request.toString();
			
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			out.write(jsonString);
			out.write("\n");
			out.flush();
			
			Log.i(TAG, "Sent Restart");
			
			socket.close();
			
		} catch (JSONException e) {
			Log.e(TAG, "Unable to create JSON Payload");
		} catch (IOException e) {
			Log.e(TAG, "Unable to send json payload");
		}
    }
    
    private static void markSquare() {
    	Socket socket = null;
    	try {
    		socket = new Socket(SERVER_HOST, SERVER_PORT);
			
		} catch (IOException e) {
			Log.e(TAG, "Couldn't connect to server", e);
			return;
		}
    	
    	try {
			JSONObject request = new JSONObject()
					.put(JSON_TYPE_FIELD, ConnectionType.MARK.toString())
					.put(JSON_PAYLOAD_FIELD, "4:5");
			
			String jsonString = request.toString();
			
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			out.write(jsonString);
			out.write("\n");
			out.flush();
			
			Log.i(TAG, "Sent Mark press");
			
			socket.close();
			
		} catch (JSONException e) {
			Log.e(TAG, "Unable to create JSON Payload");
		} catch (IOException e) {
			Log.e(TAG, "Unable to send json payload");
		}
    }

    public static void main(String[] args) {
    	attachListener();
    	restartGame();
    	markSquare();
    	sendKeyPress();
    }
    

}
