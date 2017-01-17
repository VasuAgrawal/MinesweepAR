import org.json.JSONException;
import org.json.JSONObject;

public class ServerMain {

    private static final int SERVER_PORT = 8000;
    private static final String TAG = "ServerMain";

    private static final String JSON_TYPE_FIELD = "type";
    private static final String JSON_PAYLOAD_FIELD = "payload";

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

    public void handleClient(Socket clientSocket) {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String content = br.readLine();

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

        String payloadString = NULL;

        try {
            payloadString = jObject.getString(JSON_PAYLOAD_FIELD);
        } catch(Exception e) {
            Log.i(TAG, "No payload for type: " + type, e);
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
        // TODO: Implement
    }

    public void handleTileMark(String payload) {

        int[] location = symbol.split(":");

        game.pressMine(location[0], location[1]);

        updateListeners(String.format("Marked mine at location (%d, %d)!", location[0], location[1]));

    }

    public void handleKeyPress(String symbol) {


        game.pressMine(location[0], location[1]);

        updateListeners(String.format("Marked mine at location (%d, %d)!", location[0], location[1]));
    }

    public void addGameStateListener(Socket clientSocket) {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        listeners.add(out);
    }

    public void updateListeners(String cause) {

        JsonArrayBuilder boardBuilder = Json.createArrayBuilder();

        for(char c: game.getBoard()) {
            boardBuilder.add(c);
        }

        JsonObject state = Json.createObjectBuilder().add("cause", cause)
            .add("time", game.getTime())
            .add("board", boardBuilder.build())
            .add("mineCount", game.getMineCount())
            .add("status", game.getStatus().toString()).build();

        String jsonString = state.toString();

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
