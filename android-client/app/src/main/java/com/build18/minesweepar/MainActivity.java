package com.build18.minesweepar;


import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.core.Point;
import org.opencv.core.*;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.Arrays;

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

    @Override
    public void onCameraViewStarted(int width, int height) {
        this.overlay = new Overlay();

    }

    @Override
    public void onCameraViewStopped() {
        if(this.overlay != null) {
            overlay.release();
        }
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        Mat rgba = inputFrame.rgba();

        rgba = overlay.overlayTiles(rgba);

        Imgproc.line(rgba, new Point(5, 5), new Point(200, 200), new Scalar(255, 255, 0), 20);

        return rgba;
    }

//    // new code
//    static {
//        System.loadLibrary("overlay-jni");
//    }
//    public native String getMsgFromJni();
//    // new code done
}
