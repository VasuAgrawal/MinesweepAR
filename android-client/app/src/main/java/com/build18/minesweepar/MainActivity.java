package com.build18.minesweepar;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.core.Point;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.OverlappingFileLockException;

public class MainActivity extends Activity implements GameStateChangedHandler, CameraBridgeViewBase.CvCameraViewListener2 {

    /*
     * PRIVATE CONSTANTS
     */

    private static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;
    private static final int PERMISSIONS_REQUEST_CAMERA_CODE = 1;
    private static final String TAG = "MINESWEEPAR";

    /*
     * PRIVATE VARIABLES
     */

    private GameStateManager mGameStateManager;

    private View mNewGameOverlay;
    private View mNewGameView;
    private TextView mNewGameTitle;
    private Button mNewGameButton;
    private TextView mFlagsRemainingTextView;
    private TextView mTimeElapsedTextView;
    private TextView mUncoveredPercentageTextView;

    private int mFlagsLeft;
    private int mSecondsElapsed;
    private int mUncoveredPercentage;
    private boolean mGameBegun;
    private GameState.GameStatus mGameStatus;

    private CameraBridgeViewBase mOpenCvCameraView;

    private Overlay overlay;

    /*
     * PRIVATE CALLBACKS
     */

    private Button.OnClickListener mNewGameClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            new Thread(new Runnable() {
                public void run() {
                    mGameStateManager.restartGame();
                }
            }).start();
        }
    };

    /*
     * PERMISSIONS
     */

    private void requestCameraPermission() {
        if (checkSelfPermission(CAMERA_PERMISSION) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[] {CAMERA_PERMISSION}, PERMISSIONS_REQUEST_CAMERA_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CAMERA_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCamera();
                } else {
                    Toast.makeText(this,
                            "You must give permission to use the camera in order to play MinesweepAR. Please close the app and relaunch it.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
        }
    }

    /*
     * UI
     */

    private void updateUI() {
        mFlagsRemainingTextView.setText(mFlagsLeft+"");
        mTimeElapsedTextView.setText(mSecondsElapsed+"");
        mUncoveredPercentageTextView.setText(mUncoveredPercentage+"");

        if (!mGameBegun) {
            mNewGameTitle.setText("Welcome to MinesweepAR!");
            mNewGameOverlay.setVisibility(View.VISIBLE);
            mNewGameView.setVisibility(View.VISIBLE);
        } else {
            switch (mGameStatus) {
                case IN_GAME:
                    mNewGameOverlay.setVisibility(View.INVISIBLE);
                    mNewGameView.setVisibility(View.INVISIBLE);
                    break;
                case WIN:
                    mNewGameTitle.setText("Congratulations, you've won!");
                    mNewGameOverlay.setVisibility(View.VISIBLE);
                    mNewGameView.setVisibility(View.VISIBLE);
                    break;
                case LOSS:
                    mNewGameTitle.setText("Aww, you've blown up!");
                    mNewGameOverlay.setVisibility(View.VISIBLE);
                    mNewGameView.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    /*
     * CAMERA
     */

    private void startCamera() {
        if (checkSelfPermission(CAMERA_PERMISSION) == PackageManager.PERMISSION_DENIED) {
            requestCameraPermission();
            return;
        }

        mOpenCvCameraView.setMaxFrameSize(500, 500);
        mOpenCvCameraView.enableView();


    }

    private void stopCamera() {
        if(mOpenCvCameraView != null) {

            mOpenCvCameraView.disableView();

        }
    }

    /*
     * GAME STATE
     */

    public void gameStateChanged(GameState newGameState) {
        // Write code here to access the game state and update the UI fields

        Log.d(TAG, "Doing background computation for update");

        mFlagsLeft = newGameState.getMineCount();
        mSecondsElapsed = newGameState.getSecondsElapsed();
        mUncoveredPercentage = newGameState.getUncoveredPercentage();
        mGameBegun = true;
        mGameStatus = newGameState.getStatus();

        // Draw on the UI Thread in here
        runOnUiThread(new Runnable() {
            public void run() {
                Log.d(TAG, "Running on UI Thread");
                updateUI();
            }
        });

        // Alternatively access fields from the cached game state in other parts of the program like
        // GameState latestGameState = mGameStateManager.getLatestGameState();
        // String latestCause = latestGameState.getCause();
        // int secondsElapsed = latestGameState.getSecondsElapsed();
        // char symbol = latestGameState.getSymbolAtLocation(0, 3); // Get the character on the board at location 0,3
        // int mineCount = latestGameState.getMineCount();
        // GameStatus status = latestGameState.getStatus(); // GameState.GameStatus.IN_GAME (or WIN or LOSS)
        // int boardSize = latestGameState.boardSize(); // Should always be 9

        // To interact with the server:
        // mGameStateManager.toggleMark(row, column);
        // mGameStateManager.restartGame();
    }

    /*
     * LIFECYCLE
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "Called onCreate");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.preview_view);
        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        // TODO: Load images for overlay

        mNewGameOverlay = findViewById(R.id.new_game_overlay);
        mNewGameView = findViewById(R.id.new_game_layout);
        mFlagsRemainingTextView = (TextView) findViewById(R.id.flags_remaining);
        mTimeElapsedTextView = (TextView) findViewById(R.id.time_elapsed);
        mUncoveredPercentageTextView = (TextView) findViewById(R.id.percent_uncovered);
        mNewGameTitle = (TextView) findViewById(R.id.new_game_title);
        mNewGameButton = (Button) findViewById(R.id.new_game_button);
        mNewGameButton.setOnClickListener(mNewGameClickListener);

        mFlagsLeft = 0;
        mSecondsElapsed = 0;
        mUncoveredPercentage = 0;
        mGameBegun = false;
        mGameStatus = GameState.GameStatus.WIN;

        mGameStateManager = new GameStateManager(this);

        updateUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        if(!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV Library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, openCVLoaderCallback);
        } else {
            Log.d(TAG, "Open CV Library found inside package, using it");
            openCVLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }

        // TODO: Release any resources created in onCreate
    }

    private BaseLoaderCallback openCVLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch(status) {
                case SUCCESS:
                    Log.i(TAG, "Open CV loaded successfully");
                    startCamera();
                    break;
                default:
                    super.onManagerConnected(status);
            }
        }
    };

    private void getImagePath(String[] imageResources, int resourceId, Overlay.Tile t) {

        BitmapDrawable d = (BitmapDrawable)getResources().getDrawable(resourceId, null);
        Bitmap bitmap = d.getBitmap();

        File sdCardDirectory = Environment.getExternalStorageDirectory();
        File image = new File(sdCardDirectory, t.getFilename());

        // Encode the file as a PNG image.
        FileOutputStream outStream;
        try {

            outStream = new FileOutputStream(image);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
        /* 100 to keep full quality of the image */

            outStream.flush();
            outStream.close();

            imageResources[t.getIndex()] = image.getPath();

        } catch (FileNotFoundException e) {
            Log.e(TAG, "File Not Found", e);
        } catch (IOException e) {
            Log.e(TAG, "IO Exception", e);
        }

    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        this.overlay = new Overlay();

        String[] imageResources = new String[Overlay.Tile.values().length];

        getImagePath(imageResources, R.drawable.zero, Overlay.Tile.ZERO);
        getImagePath(imageResources, R.drawable.one, Overlay.Tile.ONE);
        getImagePath(imageResources, R.drawable.two, Overlay.Tile.TWO);
        getImagePath(imageResources, R.drawable.three, Overlay.Tile.THREE);
        getImagePath(imageResources, R.drawable.four, Overlay.Tile.FOUR);
        getImagePath(imageResources, R.drawable.five, Overlay.Tile.FIVE);
        getImagePath(imageResources, R.drawable.six, Overlay.Tile.SIX);
        getImagePath(imageResources, R.drawable.seven, Overlay.Tile.SEVEN);
        getImagePath(imageResources, R.drawable.eight, Overlay.Tile.EIGHT);
        getImagePath(imageResources, R.drawable.flag, Overlay.Tile.FLAG);
        getImagePath(imageResources, R.drawable.mine, Overlay.Tile.MINE);
        getImagePath(imageResources, R.drawable.blank, Overlay.Tile.BLANK);

        overlay.setupOverlay(imageResources);

    }

    @Override
    public void onCameraViewStopped() {

    }

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

    private int mapToBoardState(char gameState) {
        if(Character.isDigit(gameState)) {
            return (int)(gameState - '0');
        }
        switch(gameState) {
            case ' ': return Overlay.Tile.BLANK.getIndex();
            case '*': return Overlay.Tile.FLAG.getIndex();
            case 'X': return Overlay.Tile.MINE.getIndex();
            case 'M': return Overlay.Tile.FLAG.getIndex();
            case '?': return Overlay.Tile.MINE.getIndex();
            case '&': return Overlay.Tile.FLAG.getIndex();
        }

        Log.e(TAG, "Couldn't find a Tile type for " + gameState);
        return -1;
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat rgba = inputFrame.rgba();

        GameState gameState = mGameStateManager.getLatestGameState();
        if(gameState == null) {
            Log.d(TAG, "Game state not setup, skipping frame");
            return rgba;
        }
        int[] boardState = new int[gameState.board.length];

        for(int index = 0; index < boardState.length; index++) {
            boardState[index] = mapToBoardState(gameState.board[index]);
        }

        Mat copiedMat = new Mat();
        rgba.copyTo(copiedMat);

        copiedMat = overlay.overlayTiles(copiedMat, boardState);

        copiedMat.copyTo(rgba);

//        Imgproc.line(rgba, new Point(5, 5), new Point(200, 200), new Scalar(255, 255, 0), 20);

        return rgba;
    }

}
