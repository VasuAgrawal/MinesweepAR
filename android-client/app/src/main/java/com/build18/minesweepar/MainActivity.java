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

//    private TextureView mTextureView;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCameraCaptureSession;
    private Size mPreviewSize;
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

    /*
     * PRIVATE CALLBACKS
     */

    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture,
                                              int width, int height) {
            Log.d(TAG, "Surface Texture Available");
            // TODO: This was needed for some race condition with the camera that I don't understand
            // @Andrew, please advise
//            startCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture,
                                                int width, int height) {
            return;
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }
    };

//    private CameraDevice.StateCallback mCameraDeviceCallback = new CameraDevice.StateCallback() {
//        @Override
//        public void onOpened(CameraDevice cameraDevice) {
//            mCameraDevice = cameraDevice;
//            previewCamera(cameraDevice);
//        }
//
//        @Override
//        public void onDisconnected(CameraDevice cameraDevice) {
//            Log.d(TAG, "Camera disconnected.");
//            cameraDevice.close();
//        }
//
//        @Override
//        public void onError(CameraDevice cameraDevice, int error) {
//            Log.d(TAG, "Camera errored.");
//            cameraDevice.close();
//        }
//    };

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

//        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
//        try {
//            String cameraID = manager.getCameraIdList()[0];
//            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraID);
//            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
//            mPreviewSize = choosePreviewSize(map);
//            manager.openCamera(cameraID, mCameraDeviceCallback, null);
//        } catch (CameraAccessException e) {
//            Toast.makeText(this, "Cannot access the camera.", Toast.LENGTH_SHORT).show();
//        }
    }

//    private void previewCamera(CameraDevice cameraDevice) {
//        SurfaceTexture previewTexture = mTextureView.getSurfaceTexture();
//        previewTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
//        Surface previewSurface = new Surface(previewTexture);
//
//        final CaptureRequest.Builder previewBuilder;
//        try {
//            previewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
//        } catch (CameraAccessException e) {
//            Log.d(TAG, "Could not create capture request.");
//            return;
//        }
//        previewBuilder.addTarget(previewSurface);
//
//        try {
//            cameraDevice.createCaptureSession(Arrays.asList(previewSurface), new CameraCaptureSession.StateCallback() {
//                @Override
//                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
//                    mCameraCaptureSession = cameraCaptureSession;
//                    try {
//                        cameraCaptureSession.setRepeatingRequest(previewBuilder.build(), null, null);
//                    } catch (CameraAccessException e) {
//                        Log.d(TAG, "Could not set repeating request for frames.");
//                        return;
//                    }
//                }
//
//                @Override
//                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
//                    Log.d(TAG, "Configuring camera failed.");
//                }
//            }, null);
//        } catch (CameraAccessException e) {
//            Log.d(TAG, "Could not create capture session.");
//        }
//    }

    private void stopCamera() {
        if(mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

//    private Size choosePreviewSize(StreamConfigurationMap map) {
//        Size sizes[] = map.getOutputSizes(ImageFormat.JPEG);
//        for (Size s : sizes) {
//            if (s.getWidth() == 864 && s.getHeight() == 480) {
//                return s;
//            }
//        }
//        Log.d(TAG, "Couldn't find the desired Size!");
//        return sizes[0];
//    }

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

//        mTextureView = (TextureView) findViewById(R.id.preview_view);
//        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);


        // TODO: Load images for overlay

        mNewGameOverlay = (View) findViewById(R.id.new_game_overlay);
        mNewGameView = (View) findViewById(R.id.new_game_layout);
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

//        if (mTextureView.isAvailable()) {
//            startCamera();
//        } else {
//            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
//        }
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
        // TODO: Do setup stuff for camera resources here

    }

    @Override
    public void onCameraViewStopped() {
        // TODO: Release any resources created in onCameraViewStarted
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat rgba = inputFrame.rgba();

        Imgproc.line(rgba, new Point(5, 5), new Point(50, 50), new Scalar(255, 255, 0), 10);


        // TODO: Implement the overlay algorithm by modifying the rgba matrix


        return rgba;
    }
}
