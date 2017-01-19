package com.build18.minesweepar;

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

//    enum Tile {
//        ONE(1), TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8), BLANK(0),
//        MINE(-1), FLAG(-2);
//
//        private int value;
//        Tile(int value) {
//            this.value = value;
//        }
//
//        public int getValue() {
//            return this.value;
//        }
//    }

//    private TagDetector tagDetector;
//
//    private static final double TAG_SIZE_METERS = .1540;
//    private static final double FOCAL_DISTANCE = 500;

    public Overlay() {
//        this.tagDetector = new TagDetector();
    }

//    private static final Mat distCoeffs = Mat.zeros(4, 1, CV_64FC1);
//
//    private void processDetections(List<TagDetection> detections, Point opticalCenter, Mat frame) {
//
//        Mat k = new Mat(3, 3, CV_64FC1);
//        int row = 0, col = 0;
//        k.put(row, col, FOCAL_DISTANCE, 0, opticalCenter.x, 0, FOCAL_DISTANCE, opticalCenter.y, 0, 0, 1);
//
//        List<List<Point>> allFramePoints = new ArrayList<>();
//
//        for(TagDetection detection: detections) {
//            if(!detection.isGood()) {
//                continue;
//            }
//            // First, we get the homography from the april tag.
//            Mat r, t; // Rotation and translation matrices
//
//
//
//
//        }
//
//        for (const auto& detection : detections) {
//            if (!detection.good) continue;
//
//            cv::Mat r, t;
//            CameraUtil::homographyToPoseCV(f, f, s, detection.homography, r, t);
//
//            // The offset to apply to the entire set of corner points. Essentially,
//            // we're shifting the grid to be centered on the center of the current april
//            // tag.
//            auto offset = tile_offsets[detection.id];
//            auto corners(corners_all);
//            for (auto& corner : corners) {
//                corner -= offset;
//            }
//
//            // Project the corners of the ENTIRE TILE (16" x 16") into frame points,
//            // which are image coordinates. These image coordinates then need to get
//            // saved and eventually averaged.
//            std::vector<cv::Point2d> frame_points;
//            cv::projectPoints(corners, r, t, K, distCoeffs, frame_points);
//            all_frame_points.push_back(frame_points);
//        }
//
//        // Now we have a vector of vectors, containing a bunch of theoretical frame
//        // points. They need to all be averaged together and distilled into a single
//        // vector.
//        std::vector<cv::Point2d> frame_points;
//        for (int i = 0; i < corners_all.size(); ++i) {
//            cv::Point2d current_point(0, 0);
//
//            for (int j = 0; j < all_frame_points.size(); ++j) {
//                current_point += all_frame_points[j][i];
//            }
//
//            //Push back the average of all of the points.
//            frame_points.push_back(current_point / (double)all_frame_points.size());
//        }
//
//        for (int i = 0; i < 2; ++i) {
//            std::vector<cv::Point2d>::const_iterator begin = (
//                    frame_points.begin() + i * 4);
//            std::vector<cv::Point2d>::const_iterator end = (
//                    frame_points.begin() + (i + 1) * 4);
//            std::vector<cv::Point2d> points(begin, end);
//
//            // Find a mapping from the full tile to the image coordindates that the
//            // warped image is supposed to be at.
//            cv::Mat H = cv::findHomography(tile_points, points, 0);
//
//            // Finally, wth the warp calculated above, warp the actual image.
//            cv::Mat tile_warped;
//            cv::Mat mask_warped;
//            cv::warpPerspective(tile_images[Tile(i % 8)], tile_warped, H,
//                    frame->size());
//            cv::warpPerspective(tile_mask, mask_warped, H, frame->size());
//            tile_warped.copyTo(*frame, mask_warped);
//        }
//
//    }

//    public native Mat overlayTilesNative(Mat rgba);

    public Mat overlayTilesWrapper(Mat rawRGBA) {

//        Point opticalCenter = new Point(rawRGBA.cols() * .5, rawRGBA.rows() * .5);
//        List<TagDetection> detections = tagDetector.process(rawRGBA, opticalCenter);
//
//        processDetections(detections, opticalCenter, rawRGBA);
//
//        return rawRGBA;
    }

    public void release() {
        // Release any temporary Mat objects
    }
}
