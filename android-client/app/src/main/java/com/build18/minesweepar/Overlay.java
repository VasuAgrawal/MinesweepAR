package com.build18.minesweepar;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Point;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.opencv.core.CvType.CV_64FC1;
import static org.opencv.core.CvType.CV_8UC1;

/**
 * Created by Reid on 1/19/17.
 */

public class Overlay {
    private static final String TAG = "Overlay";

    public enum Tile {
        BLANK(0), ONE(1), TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8),
        MINE(9), FLAG(10);

        private int index;

        Tile(int index) {
            this.index = index;
        }

        public int getIndex() {
            return this.index;
        }

        public String getFilename() {
            return name().toLowerCase() + ".png";
        }
    }



    static {
        System.loadLibrary("Overlay");
    }

    public native void overlayTilesNative(long matPointer);
    public native void setupOverlayNative(String[] imageResources);


    private void copy(File src, File dest) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dest);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }


    public void setupOverlay(String[] imageResources) {

        Log.d(TAG, Arrays.toString(imageResources));

        setupOverlayNative(imageResources);
    }

    public Mat overlayTiles(Mat rawRGBA) {

        Log.d(TAG, "Calling overlay");
        overlayTilesNative(rawRGBA.getNativeObjAddr());

        return rawRGBA;
    }
}
