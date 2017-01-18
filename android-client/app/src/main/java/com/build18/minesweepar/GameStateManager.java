package com.build18.minesweepar;


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
import android.util.Log;

/**
 * Created by Reid on 1/18/17.
 */

public class GameStateManager {

    private GameState gameState;
    private GameStateChangedHandler handler;

    private static final String SERVER_HOST = "192.168.1.62";
    private static final int SERVER_PORT = 8000;
    private static final String TAG = "GameStateManager";

    public synchronized GameState getLatestGameState() {
        return this.gameState;
    }

    private synchronized void setGameState(GameState newGameState) {
        this.gameState = newGameState;
        handler.gameStateChanged(newGameState);
    }

    private Socket sendRequest(JSONObject request) {
        Socket socket = null;
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
        } catch(IOException e) {
            Log.e(TAG, String.format("Couldn't connect to server @ %s:%d", SERVER_HOST, SERVER_PORT), e);
            return null;
        }

        String jsonString = request.toString() + "\n";

        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            out.write(jsonString);
            out.flush();

            Log.i(TAG, "Successfully connected to server. Sent: " + jsonString);
        } catch(IOException e) {
            Log.e(TAG, "Unable to send JSON payload: " + jsonString, e);
            return null;
        }

        return socket;

    }

    enum ConnectionType {
        RESTART, MARK, KEY_PRESS, GAME_STATE
    };

    private static final String JSON_TYPE_FIELD = "type";
    private static final String JSON_PAYLOAD_FIELD = "payload";

    private JSONObject createRequest(ConnectionType type, String payload) {
        try {
            return new JSONObject().put(JSON_TYPE_FIELD, type).put(JSON_PAYLOAD_FIELD, payload);
        } catch(JSONException e) {
            Log.e(TAG, "Couldn't generate JSON payload", e);
            return null;
        }
    }

    private void updateGameState(String update) {

        GameState newGameState = null;
        try {
            JSONObject state = new JSONObject(update);
            String cause = state.getString("cause");
            int time = state.getInt("time");
            JSONArray jsonBoard = state.getJSONArray("board");
            char[] board = new char[jsonBoard.length()];
            for(int index = 0; index < jsonBoard.length(); index++) {
                board[index] = jsonBoard.getString(index).charAt(0);
            }
            int mineCount = state.getInt("mineCount");
            String stringStatus = state.getString("status");

            newGameState = new GameState(cause, time, board, mineCount, stringStatus);

        } catch(JSONException e) {
            Log.e(TAG, "Error reading update payload: " + update, e);
            return;
        }

        setGameState(newGameState);
    }

    public GameStateManager(GameStateChangedHandler handler) {
        this.gameState = null;
        this.handler = handler;

        Log.i(TAG, "Trying to start game state background thread");

        new Thread(new Runnable() {
            public void run() {
                Socket gameStateSocket = sendRequest(createRequest(ConnectionType.GAME_STATE, ""));

                if(gameStateSocket == null) {
                    Log.e(TAG, "Null Server Socket, failing");
                    return;
                }

                BufferedReader in = null;
                try {
                    in = new BufferedReader(new InputStreamReader(gameStateSocket.getInputStream()));
                } catch(IOException e) {
                    Log.e(TAG, "Couldn't get an input reader", e);
                    return;
                }

                Log.i(TAG, "Waiting for game state updates");

                while(true) {
                    try {
                        String update = in.readLine();
                        Log.i(TAG, "Game State Update: " + update);
                        updateGameState(update);
                    } catch(IOException e) {
                        Log.e(TAG, "Bad response from server", e);
                        return;
                    }
                }
            }
        }).start();
    }

    public void toggleMark(int row, int column) {
        // TODO:
    }

    public void restartGame() {

    }

}
