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


    public static void main(String[] args) {
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
			out.flush();
			
			Log.i(TAG, "Successfully connected to server");
			
		} catch (JSONException e) {
			Log.e(TAG, "Unable to create JSON Payload");
		} catch (IOException e) {
			Log.e(TAG, "Unable to send json payload");
		}
    }
    

}
