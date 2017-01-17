import org.json.JSONException;
import org.json.JSONObject;

public class ServerMain {

    private static final int SERVER_PORT = 8000;
    private static final String TAG = "ServerMain";

    private static final String JSON_TYPE_FIELD = "type";

    enum ConnectionType {
        RESTART, MARK, KEY_PRESS, GAME_STATE
    };

    private final int port;

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
                } catch(IOException e) {
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

    public void handleGameRestart() {
        // TODO: Implement
    }

    public void handleTileMark(String payload) {
        // TODO: Implement
    }

    public void handleKeyPress(String symbol) {
        // TODO: Implement
    }

    public void addGameStateListener(Socket clientSocket) {
        // TODO: Implement
    }




    public void handleClient(Socket clientSocket) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            final String content = br.readLine();

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

                if(type == NULL) {
                    throw new Exception("Invalid type " + rawType);
                }
            } catch(Exception e) {
                Log.e(TAG, "Invalid type field", e);
                return;
            }

            switch(type) {
                case RESTART:

            }












        }
    }



    public void setupServer() {

    }





}
