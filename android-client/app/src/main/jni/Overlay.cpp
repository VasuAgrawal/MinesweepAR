//
// Created by Reid Long on 1/19/17.
//

#include "com_build18_minesweepar_Overlay.h"
#include <sys/time.h>
#include <iostream>
#include <stdio.h>
#include <getopt.h>
#include <vector>
#include <map>
#include <string>
#include <cstdio>
#include <android/log.h>
#include <algorithm>

#include <opencv2/highgui/highgui.hpp>
#include <opencv2/calib3d/calib3d.hpp>
#include <opencv2/imgproc/imgproc.hpp>

#include "CameraUtil.h"
#include "TagDetector.h"
#include "jni.h"

enum Tile {
    ZERO = 0,
    ONE = 1,
    TWO = 2,
    THREE = 3,
    FOUR = 4,
    FIVE = 5,
    SIX = 6,
    SEVEN = 7,
    EIGHT = 8,
    BLANK = 11,
    MINE = 9,
    FLAG = 10,
};

std::map<Tile, cv::Mat> tile_images;
cv::Mat tile_mask;
cv::Mat tile_points;

// We need to map the id to an offset from 0, 0. The origin, 0, 0, will be found
// at the top left of the board. Numbers will increment across the board (across
// a row).
std::vector<cv::Point3d> tile_offsets;
// All of the corners of the different tiles in the image, in the order:
//       1 * * * * * 2
//       * * * * * * *
//       * * * * * * *
//       4 * * * * * 3
std::vector<cv::Point3d> corners_all;

inline double IN2M(double x) {
    return .0254 * x;
}

void processs_detections(const TagDetectionArray& detections,
                         const cv::Point2d opticalCenter, cv::Mat* frame, jint * body) {
    static double s = IN2M(6 * .8); // tag size in meters
    static double ss = IN2M(7.5);
    static double f = 500;

    cv::Mat K = (cv::Mat_<double>(3, 3) <<
            f, 0, opticalCenter.x,
            0, f, opticalCenter.y,
            0, 0, 1
    );

    static cv::Mat basic_corners = (cv::Mat_<double>(4, 3) <<
            -ss,  ss, 0,
            ss,  ss, 0,
            ss, -ss, 0,
            -ss, -ss, 0



    );

    // Shouldn't usually be static, but it's set to zeros always.
    static cv::Mat_<double> distCoeffs = cv::Mat_<double>::zeros(4,1);

//    std::vector<cv::Point2d> frame_points;
//    for (const auto& detection : detections) {
//        if (!detection.good) continue;
//        int i = (int)detection.id;
//        cv::Mat r, t; // Rotation and translation matrices
//        CameraUtil::homographyToPoseCV(f, f, s, detection.homography, r, t);
//
//        // From the homography that was calculated earlier, figure out the four
//        // corners of the tile.
//        cv::projectPoints(basic_corners, r, t, K, distCoeffs, frame_points);
//
//        cv::Mat H = cv::findHomography(tile_points, frame_points, 0);
//
//
//
//        if (H.rows == 3 && H.cols == 3) {
//            if(body[i] < 0 || body[i] > 12) {
//                __android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "Bad Body Value");
//            } else {
//                cv::Mat tile_warped;
//                cv::Mat mask_warped;
//                cv::warpPerspective(tile_images[Tile(body[i])], tile_warped, H,
//                                    frame->size());
//                cv::warpPerspective(tile_mask, mask_warped, H, frame->size());
//                tile_warped.copyTo(*frame, mask_warped);
//            }
//        } else {
//            __android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "Homography matrix is wrong?");
//        }
//    }

    std::vector<std::vector<cv::Point2d>> all_frame_points;
    std::vector<size_t> detected_ids;
    for (const auto& detection : detections) {
        if (!detection.good) continue;

        // First, we get the homography from the april tag.
        cv::Mat r, t; // Rotation and translation matrices
        CameraUtil::homographyToPoseCV(f, f, s, detection.homography, r, t);

        // The offset to apply to the entire set of corner points. Essentially,
        // we're shifting the grid to be centered on the center of the current april
        // tag.
        auto offset = tile_offsets[detection.id];
        auto corners(corners_all);
        for (auto& corner : corners) {
            corner -= offset;
        }

        // Project the corners of the ENTIRE TILE (16" x 16") into frame points,
        // which are image coordinates. These image coordinates then need to get
        // saved and eventually averaged.
        std::vector<cv::Point2d> frame_points;
        cv::projectPoints(corners, r, t, K, distCoeffs, frame_points);
        all_frame_points.push_back(frame_points);
        detected_ids.push_back(detection.id);
    }

    // Now we have a vector of vectors, containing a bunch of theoretical frame
    // points. They need to all be averaged together and distilled into a single
    // vector.
    std::vector<cv::Point2d> frame_points;
    for (int i = 0; i < corners_all.size(); ++i) {

        // Make a vector of all of the points
        std::vector<cv::Point2d> points;
        for (int j = 0; j < all_frame_points.size(); ++j) {
            points.push_back(all_frame_points[j][i]);
        }

//        __android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "Check 1");

        // As it turns out, finding outliers is, in general, a nontrivial
        // problem. To deal with this, we can compute the pairwise distances
        // between each point, and then throw away at most 2 points which are
        // the furthest away from everything.
        std::vector<std::pair<cv::Point2d, double>> point_distances;
        for (const auto& point1 : points) { // First point
          double distance = 0;
          for (const auto& point2 : points) { // Point to compare with
            distance += cv::norm(point2 - point1);
          }
          point_distances.push_back(std::pair<cv::Point2d, double>(point1, 
                distance));
        }

//        __android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "Check 2");

        std::sort(point_distances.begin(), point_distances.end(), 
            [](const std::pair<cv::Point2d, double>& lhs, 
              const std::pair<cv::Point2d, double>& rhs) {
                return lhs.second < rhs.second;
            }
        );

//        __android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "Check 3");

        cv::Point2d avg(0, 0);

        int limit = point_distances.size() == 1 ? 1 : (int)point_distances.size() - 1;


        for (int i = 0; i < limit; ++i) {
          avg += point_distances[i].first;
        }

//        __android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "Check 4");


        frame_points.push_back(avg / ((double)limit));






        //cv::Point2d current_point(0, 0);

        //for (int j = 0; j < all_frame_points.size(); ++j) {
            //current_point += all_frame_points[j][i];
        //}

        ////Push back the average of all of the points.
        //frame_points.push_back(current_point / (double)all_frame_points.size());
    }

    __android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "Finished averaging all of the points");

//    for (int i = 0; i < tile_offsets.size(); ++i) {
//    for (int i = 0; i < 2; ++i) {
    for (auto id : detected_ids) {
        int i = (int)id;
        std::vector<cv::Point2d>::const_iterator begin = (
                frame_points.begin() + i * 4);
        std::vector<cv::Point2d>::const_iterator end = (
                frame_points.begin() + (i + 1) * 4);
        std::vector<cv::Point2d> points(begin, end);

        // Find a mapping from the full tile to the image coordindates that the
        // warped image is supposed to be at.
        cv::Mat H = cv::findHomography(tile_points, points, 0);

        // Finally, wth the warp calculated above, warp the actual image.
        cv::Mat tile_warped;
        cv::Mat mask_warped;
        if (H.rows == 3 && H.cols == 3) {
            if(body[i] < 0 || body[i] > 12) {
                __android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "Bad Body Value");
            } else {
                Tile t = Tile(body[i]);

                cv::warpPerspective(tile_images[Tile(body[i])], tile_warped, H,
                                    frame->size());
                cv::warpPerspective(tile_mask, mask_warped, H, frame->size());
                tile_warped.copyTo(*frame, mask_warped);
            }

        } else {
            __android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "Homography matrix is wrong?");
        }
//        tile_warped.copyTo(*tile_addr, mask_warped);
//        mask_warped.copyTo(*mask_addr, mask_warped);
    }
    __android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "Finished warping all of the images");
}


JNIEXPORT void JNICALL Java_com_build18_minesweepar_Overlay_overlayTilesNative
        (JNIEnv * env, jobject, jlong address, jintArray imageMapArray) {

    cv::Mat* frame = (cv::Mat*)address;

    jint *body = env->GetIntArrayElements(imageMapArray, 0);


    // First we make a bgr version of the rgb frame.
    cv::Mat mat_split[4];
    cv::split(*frame, mat_split);
    cv::Mat bgr_frame;
    std::vector<cv::Mat> channels = {mat_split[2], mat_split[1], mat_split[0]};
    cv::merge(channels, bgr_frame);

    TagDetectorParams params;
    params.segDecimate = true;
    params.segSigma = .8;
    params.thetaThresh = 100.0;
    params.magThresh = 1200.0;
    params.adaptiveThresholdValue = 5;
    params.adaptiveThresholdRadius = 9;
    params.refineBad = false;
    params.refineQuads = true;
    params.newQuadAlgorithm = false;

    TagFamily family("Tag36h11");
    TagDetector detector(family, params);
    TagDetectionArray detections;

    cv::Point2d opticalCenter(bgr_frame.cols * .5, bgr_frame.rows * .5);
    detector.process(bgr_frame, opticalCenter, detections); // Grab detections
    __android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "Number of detections: %d", (int)detections.size());

    // This should add the overlay from just this tile onto the frame
    processs_detections(detections, opticalCenter, &bgr_frame, body);

    cv::Mat final;
    cv::Mat bgr_split[3];
    cv::split(bgr_frame, bgr_split);
    std::vector<cv::Mat> final_channels = {bgr_split[2], bgr_split[1], bgr_split[0], mat_split[3]};
    cv::merge(final_channels, final);
    *frame = final;

    env->ReleaseIntArrayElements(imageMapArray, body, 0);
}

JNIEXPORT void JNICALL Java_com_build18_minesweepar_Overlay_setupOverlayNative
        (JNIEnv *env, jobject, jobjectArray stringArray) {

    int stringCount = env->GetArrayLength(stringArray);

    for (int i = 0; i < stringCount; i++) {
        jstring string = (jstring) (env->GetObjectArrayElement(stringArray, i));
        const char *rawString = env->GetStringUTFChars(string, 0);
        tile_images[(Tile)i] = cv::imread(rawString, CV_LOAD_IMAGE_COLOR);

        env->ReleaseStringUTFChars(string, rawString);
    }

    const size_t tile_width = tile_images[BLANK].cols;
    const size_t tile_height = tile_images[BLANK].rows;

    tile_points = (cv::Mat_<double>(4, 2) <<
                   0.0f, 0.0f,
            tile_width, 0.0f,
            tile_width, tile_height,
            0.0f, tile_height
    );

    // All the tiles have the same mask, so it can be generated once.
    tile_mask = cv::Mat(tile_images[BLANK].size(), CV_8U, cv::Scalar(255));

    const double row_width = 16; // inches
    const double col_height = 16; // inches
    const double image_dim = 13; // inches
    auto top_left = cv::Point3d(IN2M(-image_dim / 2), IN2M(image_dim/ 2), 0);
    auto top_right = cv::Point3d(IN2M(image_dim/ 2), IN2M(image_dim/ 2), 0);
    auto bot_right = cv::Point3d(IN2M(image_dim/ 2), IN2M(-image_dim/ 2), 0);
    auto bot_left = cv::Point3d(IN2M(-image_dim/ 2), IN2M(-image_dim/ 2), 0);

    for (int row = 0; row < 9; ++row) {
        for (int col = 0; col < 9; ++col) {
            // Find the center of each tile, in a global coordinate space. The center
            // of the tile is treated as (0, 0) for the local coordinate frame, so all
            // of the points need to be translated to be relative to that origin,
            // which is why the center is being added to tile_offsets.
            auto center = cv::Point3d(IN2M(col * col_height + col_height / 2),
                                      -IN2M(row * row_width + row_width / 2), IN2M(0));
            tile_offsets.push_back(center);
            std::cout << "Center: " << center << std::endl;

            // We push the global corner points in the order specified above onto
            // corners_all to get points to project later.
            corners_all.push_back(center + top_left);
            corners_all.push_back(center + top_right);
            corners_all.push_back(center + bot_right);
            corners_all.push_back(center + bot_left);
        }
    }

}
