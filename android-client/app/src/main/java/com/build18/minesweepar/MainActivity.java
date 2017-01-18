package com.build18.minesweepar;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.os.Bundle;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.Manifest;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import java.util.Arrays;

public class MainActivity extends Activity {

    /*
     * PRIVATE CONSTANTS
     */

    private static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;
    private static final int PERMISSIONS_REQUEST_CAMERA_CODE = 1;
    private static final String TAG = "MINESWEEPAR";

    /*
     * PRIVATE VARIABLES
     */

    private TextureView previewView;

    /*
     * PRIVATE CALLBACKS
     */

    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture,
                                              int width, int height) {
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

    private CameraDevice.StateCallback cameraDeviceCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            previewCamera(cameraDevice);
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            Log.d("TAG", "Camera disconnected.");
            cameraDevice.close();
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            Log.d("TAG", "Camera errored.");
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
            manager.openCamera(cameraID, cameraDeviceCallback, null);
        } catch (CameraAccessException e) {
            Toast.makeText(this, "Cannot access the camera.", Toast.LENGTH_SHORT).show();
        }
    }

    private void previewCamera(CameraDevice cameraDevice) {
        TextureView previewView = (TextureView)findViewById(R.id.preview_view);
        SurfaceTexture previewTexture = previewView.getSurfaceTexture();
        Surface previewSurface = new Surface(previewTexture);

        final CaptureRequest.Builder previewBuilder;
        try {
            previewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        } catch (CameraAccessException e) {
            Log.d("TAG", "Could not create capture request.");
            return;
        }
        previewBuilder.addTarget(previewSurface);

        try {
            cameraDevice.createCaptureSession(Arrays.asList(previewSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    try {
                        cameraCaptureSession.setRepeatingRequest(previewBuilder.build(), null, null);
                    } catch (CameraAccessException e) {
                        Log.d("TAG", "Could not set repeating request for frames.");
                        return;
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    Log.d("TAG", "Configuring camera failed.");
                }
            }, null);
        } catch (CameraAccessException e) {
            Log.d("TAG", "Could not create capture session.");
        }
    }

    /*
     * LIFECYCLE
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        previewView = (TextureView) findViewById(R.id.preview_view);
        previewView.setSurfaceTextureListener(surfaceTextureListener);
    }

}
