package com.build18.minesweepar;

import android.net.Uri;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.CvType.CV_64FC1;
import static org.opencv.core.CvType.CV_8UC1;

/**
 * Created by Reid on 1/19/17.
 */

public class Overlay {
    private static final String TAG = "Overlay";

    enum Tile {
        ONE(1, "1"), TWO(2, "2"), THREE(3, "3"), FOUR(4, "4"), FIVE(5, "5"), SIX(6, "6"), SEVEN(7, "7"), EIGHT(8, "8"), BLANK(0, "blank"),
        MINE(9, "x"), FLAG(10, "f");

        private int index;
        private String name;
        Tile(int index, String name) {
            this.index = index;
        }

        public int getIndex() {
            return this.index;
        }

        public String getName() {
            return this.name;
        }
    }



    static {
        System.loadLibrary("Overlay");
    }

    private static boolean isSetup = false;

    public native void overlayTilesNative(long matPointer);
    public native void setupOverlayNative(String[] imageResources);

    public synchronized void setupOverlay() {
        if(isSetup) {
            return;
        }

        String[] imageResources = new String[Tile.values().length];

        for(Tile t: Tile.values()) {
            Uri path = Uri.parse(String.format("android.resource://com.build18.minesweepar/drawable/%s.png", t.getName()));
            imageResources[t.getIndex()] = path.toString();
        }

        setupOverlayNative(imageResources);

        isSetup = true;
    }

    public Mat overlayTiles(Mat rawRGBA) {

        setupOverlay();

        overlayTilesNative(rawRGBA.getNativeObjAddr());

        return rawRGBA;
    }
}
