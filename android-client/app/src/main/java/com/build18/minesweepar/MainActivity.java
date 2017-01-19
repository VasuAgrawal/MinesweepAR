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
import android.widget.Toast;

import java.util.Arrays;

public class MainActivity extends Activity implements GameStateChangedHandler {

    /*
     * PRIVATE CONSTANTS
     */

    private static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;
    private static final int PERMISSIONS_REQUEST_CAMERA_CODE = 1;
    private static final String TAG = "MINESWEEPAR";

    /*
     * PRIVATE VARIABLES
     */

    private TextureView mTextureView;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCameraCaptureSession;
    private Size mPreviewSize;
    private GameStateManager mGameStateManager;

    /*
     * PRIVATE CALLBACKS
     */

    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture,
                                              int width, int height) {
            Log.d(TAG, "Surface Texture Available");
            startCamera();
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

    private CameraDevice.StateCallback mCameraDeviceCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            previewCamera(cameraDevice);
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            Log.d(TAG, "Camera disconnected.");
            cameraDevice.close();
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            Log.d(TAG, "Camera errored.");
            cameraDevice.close();
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
     * CAMERA
     */

    private void startCamera() {
        if (checkSelfPermission(CAMERA_PERMISSION) == PackageManager.PERMISSION_DENIED) {
            requestCameraPermission();
            return;
        }
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraID = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraID);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mPreviewSize = choosePreviewSize(map);
            manager.openCamera(cameraID, mCameraDeviceCallback, null);
        } catch (CameraAccessException e) {
            Toast.makeText(this, "Cannot access the camera.", Toast.LENGTH_SHORT).show();
        }
    }

    private void previewCamera(CameraDevice cameraDevice) {
        SurfaceTexture previewTexture = mTextureView.getSurfaceTexture();
        previewTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface previewSurface = new Surface(previewTexture);

        final CaptureRequest.Builder previewBuilder;
        try {
            previewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        } catch (CameraAccessException e) {
            Log.d(TAG, "Could not create capture request.");
            return;
        }
        previewBuilder.addTarget(previewSurface);

        try {
            cameraDevice.createCaptureSession(Arrays.asList(previewSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    mCameraCaptureSession = cameraCaptureSession;
                    try {
                        cameraCaptureSession.setRepeatingRequest(previewBuilder.build(), null, null);
                    } catch (CameraAccessException e) {
                        Log.d(TAG, "Could not set repeating request for frames.");
                        return;
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    Log.d(TAG, "Configuring camera failed.");
                }
            }, null);
        } catch (CameraAccessException e) {
            Log.d(TAG, "Could not create capture session.");
        }
    }

    private void stopCamera() {
        if (mCameraCaptureSession != null) {
            mCameraCaptureSession.close();
            mCameraCaptureSession = null;
        }
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    private Size choosePreviewSize(StreamConfigurationMap map) {
        Size sizes[] = map.getOutputSizes(ImageFormat.JPEG);
        for (Size s : sizes) {
            if (s.getWidth() == 864 && s.getHeight() == 480) {
                return s;
            }
        }
        Log.d(TAG, "Couldn't find the desired Size!");
        return sizes[0];
    }

    /*
     * GAME STATE
     */

    public void gameStateChanged(GameState newGameState) {
        // Write code here to access the game state and update the UI fields

        // TODO: Do the background computation here
        Log.d(TAG, "Doing background computation for update");

        // Draw on the UI Thread in here
        runOnUiThread(new Runnable() {
            public void run() {
                Log.d(TAG, "Running on UI Thread");
                // TODO: Do the drawing stuff here
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

        setContentView(R.layout.activity_main);
        mTextureView = (TextureView) findViewById(R.id.preview_view);
        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // TODO: This needs to be called when the app launches, but I'm not sure this is the
        // right place for it....
        Log.i(TAG, "onStart Calling game State manager");
        mGameStateManager = new GameStateManager(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        if (mTextureView.isAvailable()) {
            startCamera();
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    protected void onPause() {
        stopCamera();
        super.onPause();
    }
}
