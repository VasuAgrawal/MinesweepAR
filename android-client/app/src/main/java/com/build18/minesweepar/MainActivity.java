package com.build18.minesweepar;


import java.util.Arrays;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

public class MainActivity extends Activity implements CvCameraViewListener2 {
    private static final String TAG = "OCVSample::Activity";

    public static final int VIEW_MODE_RGBA = 0;
    public static final int VIEW_MODE_HIST = 1;
    public static final int VIEW_MODE_CANNY = 2;
    public static final int VIEW_MODE_SEPIA = 3;
    public static final int VIEW_MODE_SOBEL = 4;
    public static final int VIEW_MODE_ZOOM = 5;
    public static final int VIEW_MODE_PIXELIZE = 6;
    public static final int VIEW_MODE_POSTERIZE = 7;

    private MenuItem mItemPreviewRGBA;
    private MenuItem mItemPreviewHist;
    private MenuItem mItemPreviewCanny;
    private MenuItem mItemPreviewSepia;
    private MenuItem mItemPreviewSobel;
    private MenuItem mItemPreviewZoom;
    private MenuItem mItemPreviewPixelize;
    private MenuItem mItemPreviewPosterize;
    private CameraBridgeViewBase mOpenCvCameraView;

    private Size mSize0;

    private Mat mIntermediateMat;
    private Mat mMat0;
    private MatOfInt mChannels[];
    private MatOfInt mHistSize;
    private int mHistSizeNum = 25;
    private MatOfFloat mRanges;
    private Scalar mColorsRGB[];
    private Scalar mColorsHue[];
    private Scalar mWhilte;
    private Point mP1;
    private Point mP2;
    private float mBuff[];
    private Mat mSepiaKernel;

    public static int viewMode = VIEW_MODE_HIST;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);



        setContentView(R.layout.activity_main);

//        setContentView(R.layout.image_manipulations_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.image_manipulations_activity_surface_view);
        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemPreviewRGBA = menu.add("Preview RGBA");
        mItemPreviewHist = menu.add("Histograms");
        mItemPreviewCanny = menu.add("Canny");
        mItemPreviewSepia = menu.add("Sepia");
        mItemPreviewSobel = menu.add("Sobel");
        mItemPreviewZoom = menu.add("Zoom");
        mItemPreviewPixelize = menu.add("Pixelize");
        mItemPreviewPosterize = menu.add("Posterize");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item == mItemPreviewRGBA)
            viewMode = VIEW_MODE_RGBA;
        if (item == mItemPreviewHist)
            viewMode = VIEW_MODE_HIST;
        else if (item == mItemPreviewCanny)
            viewMode = VIEW_MODE_CANNY;
        else if (item == mItemPreviewSepia)
            viewMode = VIEW_MODE_SEPIA;
        else if (item == mItemPreviewSobel)
            viewMode = VIEW_MODE_SOBEL;
        else if (item == mItemPreviewZoom)
            viewMode = VIEW_MODE_ZOOM;
        else if (item == mItemPreviewPixelize)
            viewMode = VIEW_MODE_PIXELIZE;
        else if (item == mItemPreviewPosterize)
            viewMode = VIEW_MODE_POSTERIZE;
        return true;
    }

    public void onCameraViewStarted(int width, int height) {
        mIntermediateMat = new Mat();
        mSize0 = new Size();
        mChannels = new MatOfInt[]{new MatOfInt(0), new MatOfInt(1), new MatOfInt(2)};
        mBuff = new float[mHistSizeNum];
        mHistSize = new MatOfInt(mHistSizeNum);
        mRanges = new MatOfFloat(0f, 256f);
        mMat0 = new Mat();
        mColorsRGB = new Scalar[]{new Scalar(200, 0, 0, 255), new Scalar(0, 200, 0, 255), new Scalar(0, 0, 200, 255)};
        mColorsHue = new Scalar[]{
                new Scalar(255, 0, 0, 255), new Scalar(255, 60, 0, 255), new Scalar(255, 120, 0, 255), new Scalar(255, 180, 0, 255), new Scalar(255, 240, 0, 255),
                new Scalar(215, 213, 0, 255), new Scalar(150, 255, 0, 255), new Scalar(85, 255, 0, 255), new Scalar(20, 255, 0, 255), new Scalar(0, 255, 30, 255),
                new Scalar(0, 255, 85, 255), new Scalar(0, 255, 150, 255), new Scalar(0, 255, 215, 255), new Scalar(0, 234, 255, 255), new Scalar(0, 170, 255, 255),
                new Scalar(0, 120, 255, 255), new Scalar(0, 60, 255, 255), new Scalar(0, 0, 255, 255), new Scalar(64, 0, 255, 255), new Scalar(120, 0, 255, 255),
                new Scalar(180, 0, 255, 255), new Scalar(255, 0, 255, 255), new Scalar(255, 0, 215, 255), new Scalar(255, 0, 85, 255), new Scalar(255, 0, 0, 255)
        };
        mWhilte = Scalar.all(255);
        mP1 = new Point();
        mP2 = new Point();

        // Fill sepia kernel
        mSepiaKernel = new Mat(4, 4, CvType.CV_32F);
        mSepiaKernel.put(0, 0, /* R */0.189f, 0.769f, 0.393f, 0f);
        mSepiaKernel.put(1, 0, /* G */0.168f, 0.686f, 0.349f, 0f);
        mSepiaKernel.put(2, 0, /* B */0.131f, 0.534f, 0.272f, 0f);
        mSepiaKernel.put(3, 0, /* A */0.000f, 0.000f, 0.000f, 1f);
    }

    public void onCameraViewStopped() {
        // Explicitly deallocate Mats
        if (mIntermediateMat != null)
            mIntermediateMat.release();

        mIntermediateMat = null;
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Mat rgba = inputFrame.rgba();
        Size sizeRgba = rgba.size();

        Mat rgbaInnerWindow;

        int rows = (int) sizeRgba.height;
        int cols = (int) sizeRgba.width;

        int left = cols / 8;
        int top = rows / 8;

        int width = cols * 3 / 4;
        int height = rows * 3 / 4;

        switch (MainActivity.viewMode) {
            case MainActivity.VIEW_MODE_RGBA:
                break;

            case MainActivity.VIEW_MODE_HIST:
                Mat hist = new Mat();
                int thikness = (int) (sizeRgba.width / (mHistSizeNum + 10) / 5);
                if (thikness > 5) thikness = 5;
                int offset = (int) ((sizeRgba.width - (5 * mHistSizeNum + 4 * 10) * thikness) / 2);
                // RGB
                for (int c = 0; c < 3; c++) {
                    Imgproc.calcHist(Arrays.asList(rgba), mChannels[c], mMat0, hist, mHistSize, mRanges);
                    Core.normalize(hist, hist, sizeRgba.height / 2, 0, Core.NORM_INF);
                    hist.get(0, 0, mBuff);
                    for (int h = 0; h < mHistSizeNum; h++) {
                        mP1.x = mP2.x = offset + (c * (mHistSizeNum + 10) + h) * thikness;
                        mP1.y = sizeRgba.height - 1;
                        mP2.y = mP1.y - 2 - (int) mBuff[h];
                        Imgproc.line(rgba, mP1, mP2, mColorsRGB[c], thikness);
                    }
                }
                // Value and Hue
                Imgproc.cvtColor(rgba, mIntermediateMat, Imgproc.COLOR_RGB2HSV_FULL);
                // Value
                Imgproc.calcHist(Arrays.asList(mIntermediateMat), mChannels[2], mMat0, hist, mHistSize, mRanges);
                Core.normalize(hist, hist, sizeRgba.height / 2, 0, Core.NORM_INF);
                hist.get(0, 0, mBuff);
                for (int h = 0; h < mHistSizeNum; h++) {
                    mP1.x = mP2.x = offset + (3 * (mHistSizeNum + 10) + h) * thikness;
                    mP1.y = sizeRgba.height - 1;
                    mP2.y = mP1.y - 2 - (int) mBuff[h];
                    Imgproc.line(rgba, mP1, mP2, mWhilte, thikness);
                }

                // Hue
                Imgproc.calcHist(Arrays.asList(mIntermediateMat), mChannels[0], mMat0, hist, mHistSize, mRanges);
                Core.normalize(hist, hist, sizeRgba.height / 2, 0, Core.NORM_INF);
                hist.get(0, 0, mBuff);
                for (int h = 0; h < mHistSizeNum; h++) {
                    mP1.x = mP2.x = offset + (4 * (mHistSizeNum + 10) + h) * thikness;
                    mP1.y = sizeRgba.height - 1;
                    mP2.y = mP1.y - 2 - (int) mBuff[h];
                    Imgproc.line(rgba, mP1, mP2, mColorsHue[h], thikness);
                }
                break;

            case MainActivity.VIEW_MODE_CANNY:
                rgbaInnerWindow = rgba.submat(top, top + height, left, left + width);
                Imgproc.Canny(rgbaInnerWindow, mIntermediateMat, 80, 90);
                Imgproc.cvtColor(mIntermediateMat, rgbaInnerWindow, Imgproc.COLOR_GRAY2BGRA, 4);
                rgbaInnerWindow.release();
                break;

            case MainActivity.VIEW_MODE_SOBEL:
                Mat gray = inputFrame.gray();
                Mat grayInnerWindow = gray.submat(top, top + height, left, left + width);
                rgbaInnerWindow = rgba.submat(top, top + height, left, left + width);
                Imgproc.Sobel(grayInnerWindow, mIntermediateMat, CvType.CV_8U, 1, 1);
                Core.convertScaleAbs(mIntermediateMat, mIntermediateMat, 10, 0);
                Imgproc.cvtColor(mIntermediateMat, rgbaInnerWindow, Imgproc.COLOR_GRAY2BGRA, 4);
                grayInnerWindow.release();
                rgbaInnerWindow.release();
                break;

            case MainActivity.VIEW_MODE_SEPIA:
                rgbaInnerWindow = rgba.submat(top, top + height, left, left + width);
                Core.transform(rgbaInnerWindow, rgbaInnerWindow, mSepiaKernel);
                rgbaInnerWindow.release();
                break;

            case MainActivity.VIEW_MODE_ZOOM:
                Mat zoomCorner = rgba.submat(0, rows / 2 - rows / 10, 0, cols / 2 - cols / 10);
                Mat mZoomWindow = rgba.submat(rows / 2 - 9 * rows / 100, rows / 2 + 9 * rows / 100, cols / 2 - 9 * cols / 100, cols / 2 + 9 * cols / 100);
                Imgproc.resize(mZoomWindow, zoomCorner, zoomCorner.size());
                Size wsize = mZoomWindow.size();
                Imgproc.rectangle(mZoomWindow, new Point(1, 1), new Point(wsize.width - 2, wsize.height - 2), new Scalar(255, 0, 0, 255), 2);
                zoomCorner.release();
                mZoomWindow.release();
                break;

            case MainActivity.VIEW_MODE_PIXELIZE:
                rgbaInnerWindow = rgba.submat(top, top + height, left, left + width);
                Imgproc.resize(rgbaInnerWindow, mIntermediateMat, mSize0, 0.1, 0.1, Imgproc.INTER_NEAREST);
                Imgproc.resize(mIntermediateMat, rgbaInnerWindow, rgbaInnerWindow.size(), 0., 0., Imgproc.INTER_NEAREST);
                rgbaInnerWindow.release();
                break;

            case MainActivity.VIEW_MODE_POSTERIZE:
            /*
            Imgproc.cvtColor(rgbaInnerWindow, mIntermediateMat, Imgproc.COLOR_RGBA2RGB);
            Imgproc.pyrMeanShiftFiltering(mIntermediateMat, mIntermediateMat, 5, 50);
            Imgproc.cvtColor(mIntermediateMat, rgbaInnerWindow, Imgproc.COLOR_RGB2RGBA);
            */
                rgbaInnerWindow = rgba.submat(top, top + height, left, left + width);
                Imgproc.Canny(rgbaInnerWindow, mIntermediateMat, 80, 90);
                rgbaInnerWindow.setTo(new Scalar(0, 0, 0, 255), mIntermediateMat);
                Core.convertScaleAbs(rgbaInnerWindow, mIntermediateMat, 1. / 16, 0);
                Core.convertScaleAbs(mIntermediateMat, rgbaInnerWindow, 16, 0);
                rgbaInnerWindow.release();
                break;
        }

        return rgba;
    }
}


//
//import android.app.Activity;
//import android.content.Context;
//import android.content.pm.PackageManager;
//import android.graphics.SurfaceTexture;
//import android.graphics.ImageFormat;
//import android.hardware.camera2.CameraAccessException;
//import android.hardware.camera2.CameraCaptureSession;
//import android.hardware.camera2.CameraCharacteristics;
//import android.hardware.camera2.CameraDevice;
//import android.hardware.camera2.CameraManager;
//import android.hardware.camera2.CaptureRequest;
//import android.hardware.camera2.params.StreamConfigurationMap;
//import android.Manifest;
//import android.os.Bundle;
//import android.util.Log;
//import android.util.Size;
//import android.view.Surface;
//import android.view.TextureView;
//import android.view.View;
//import android.widget.Button;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import java.util.Arrays;
//
//public class MainActivity extends Activity implements GameStateChangedHandler {
//
//    /*
//     * PRIVATE CONSTANTS
//     */
//
//    private static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;
//    private static final int PERMISSIONS_REQUEST_CAMERA_CODE = 1;
//    private static final String TAG = "MINESWEEPAR";
//
//    /*
//     * PRIVATE VARIABLES
//     */
//
//    private TextureView mTextureView;
//    private CameraDevice mCameraDevice;
//    private CameraCaptureSession mCameraCaptureSession;
//    private Size mPreviewSize;
//    private GameStateManager mGameStateManager;
//
//    private View mNewGameOverlay;
//    private View mNewGameView;
//    private TextView mNewGameTitle;
//    private Button mNewGameButton;
//    private TextView mFlagsRemainingTextView;
//    private TextView mTimeElapsedTextView;
//    private TextView mUncoveredPercentageTextView;
//
//    private int mFlagsLeft;
//    private int mSecondsElapsed;
//    private int mUncoveredPercentage;
//    private boolean mGameBegun;
//    private GameState.GameStatus mGameStatus;
//
//    /*
//     * PRIVATE CALLBACKS
//     */
//
//    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
//        @Override
//        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture,
//                                              int width, int height) {
//            Log.d(TAG, "Surface Texture Available");
//            startCamera();
//        }
//
//        @Override
//        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture,
//                                                int width, int height) {
//            return;
//        }
//
//        @Override
//        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
//            return true;
//        }
//
//        @Override
//        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
//        }
//    };
//
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
//
//    private Button.OnClickListener mNewGameClickListener = new Button.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            new Thread(new Runnable() {
//                public void run() {
//                    mGameStateManager.restartGame();
//                }
//            }).start();
//        }
//    };
//
//    /*
//     * PERMISSIONS
//     */
//
//    private void requestCameraPermission() {
//        if (checkSelfPermission(CAMERA_PERMISSION) == PackageManager.PERMISSION_DENIED) {
//            requestPermissions(new String[] {CAMERA_PERMISSION}, PERMISSIONS_REQUEST_CAMERA_CODE);
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case PERMISSIONS_REQUEST_CAMERA_CODE:
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    startCamera();
//                } else {
//                    Toast.makeText(this,
//                            "You must give permission to use the camera in order to play MinesweepAR. Please close the app and relaunch it.",
//                            Toast.LENGTH_SHORT
//                    ).show();
//                }
//        }
//    }
//
//    /*
//     * UI
//     */
//
//    private void updateUI() {
//        mFlagsRemainingTextView.setText(mFlagsLeft+"");
//        mTimeElapsedTextView.setText(mSecondsElapsed+"");
//        mUncoveredPercentageTextView.setText(mUncoveredPercentage+"");
//
//        if (!mGameBegun) {
//            mNewGameTitle.setText("Welcome to MinesweepAR!");
//            mNewGameOverlay.setVisibility(View.VISIBLE);
//            mNewGameView.setVisibility(View.VISIBLE);
//        } else {
//            switch (mGameStatus) {
//                case IN_GAME:
//                    mNewGameOverlay.setVisibility(View.INVISIBLE);
//                    mNewGameView.setVisibility(View.INVISIBLE);
//                    break;
//                case WIN:
//                    mNewGameTitle.setText("Congratulations, you've won!");
//                    mNewGameOverlay.setVisibility(View.VISIBLE);
//                    mNewGameView.setVisibility(View.VISIBLE);
//                    break;
//                case LOSS:
//                    mNewGameTitle.setText("Aww, you've blown up!");
//                    mNewGameOverlay.setVisibility(View.VISIBLE);
//                    mNewGameView.setVisibility(View.VISIBLE);
//                    break;
//            }
//        }
//    }
//
//    /*
//     * CAMERA
//     */
//
//    private void startCamera() {
//        if (checkSelfPermission(CAMERA_PERMISSION) == PackageManager.PERMISSION_DENIED) {
//            requestCameraPermission();
//            return;
//        }
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
//    }
//
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
//
//    private void stopCamera() {
//        if (mCameraCaptureSession != null) {
//            mCameraCaptureSession.close();
//            mCameraCaptureSession = null;
//        }
//        if (mCameraDevice != null) {
//            mCameraDevice.close();
//            mCameraDevice = null;
//        }
//    }
//
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
//
//    /*
//     * GAME STATE
//     */
//
//    public void gameStateChanged(GameState newGameState) {
//        // Write code here to access the game state and update the UI fields
//
//        Log.d(TAG, "Doing background computation for update");
//
//        mFlagsLeft = newGameState.getMineCount();
//        mSecondsElapsed = newGameState.getSecondsElapsed();
//        mUncoveredPercentage = newGameState.getUncoveredPercentage();
//        mGameBegun = true;
//        mGameStatus = newGameState.getStatus();
//
//        // Draw on the UI Thread in here
//        runOnUiThread(new Runnable() {
//            public void run() {
//                Log.d(TAG, "Running on UI Thread");
//                updateUI();
//            }
//        });
//
//        // Alternatively access fields from the cached game state in other parts of the program like
//        // GameState latestGameState = mGameStateManager.getLatestGameState();
//        // String latestCause = latestGameState.getCause();
//        // int secondsElapsed = latestGameState.getSecondsElapsed();
//        // char symbol = latestGameState.getSymbolAtLocation(0, 3); // Get the character on the board at location 0,3
//        // int mineCount = latestGameState.getMineCount();
//        // GameStatus status = latestGameState.getStatus(); // GameState.GameStatus.IN_GAME (or WIN or LOSS)
//        // int boardSize = latestGameState.boardSize(); // Should always be 9
//
//        // To interact with the server:
//        // mGameStateManager.toggleMark(row, column);
//        // mGameStateManager.restartGame();
//    }
//
//    /*
//     * LIFECYCLE
//     */
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        setContentView(R.layout.activity_main);
//        mTextureView = (TextureView) findViewById(R.id.preview_view);
//        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
//
//        mNewGameOverlay = (View) findViewById(R.id.new_game_overlay);
//        mNewGameView = (View) findViewById(R.id.new_game_layout);
//        mFlagsRemainingTextView = (TextView) findViewById(R.id.flags_remaining);
//        mTimeElapsedTextView = (TextView) findViewById(R.id.time_elapsed);
//        mUncoveredPercentageTextView = (TextView) findViewById(R.id.percent_uncovered);
//        mNewGameTitle = (TextView) findViewById(R.id.new_game_title);
//        mNewGameButton = (Button) findViewById(R.id.new_game_button);
//        mNewGameButton.setOnClickListener(mNewGameClickListener);
//
//        mFlagsLeft = 0;
//        mSecondsElapsed = 0;
//        mUncoveredPercentage = 0;
//        mGameBegun = false;
//        mGameStatus = GameState.GameStatus.WIN;
//        updateUI();
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        // TODO: This needs to be called when the app launches, but I'm not sure this is the
//        // right place for it....
//        Log.i(TAG, "onStart Calling game State manager");
//        mGameStateManager = new GameStateManager(this);
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        View decorView = getWindow().getDecorView();
//        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_FULLSCREEN;
//        decorView.setSystemUiVisibility(uiOptions);
//
//        if (mTextureView.isAvailable()) {
//            startCamera();
//        } else {
//            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
//        }
//    }
//
//    @Override
//    protected void onPause() {
//        stopCamera();
//        super.onPause();
//    }
//}
