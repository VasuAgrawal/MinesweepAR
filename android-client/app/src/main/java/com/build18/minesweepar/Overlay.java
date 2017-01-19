package com.build18.minesweepar;

import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.util.List;

/**
 * Created by Reid on 1/19/17.
 */

public class Overlay {

    enum Tile {
        ONE(1), TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8), BLANK(0),
        MINE(-1), FLAG(-2);
        
        private int value;
        Tile(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return this.value;
        }
    }

    private TagDetector tagDetector;

    public Overlay() {
        this.tagDetector = new TagDetector();
    }

    private void processDetections(List<TagDetection> detections, Point opticalCenter, Mat frame) {

    }

    public Mat overlayTiles(Mat rawRGBA) {

        Point opticalCenter = new Point(rawRGBA.cols() * .5, rawRGBA.rows() * .5);
        List<TagDetection> detections = tagDetector.process(rawRGBA, opticalCenter);

        processDetections(detections, opticalCenter, rawRGBA);

        return rawRGBA;
    }

    public void release() {
        // Release any temporary Mat objects
    }
}
