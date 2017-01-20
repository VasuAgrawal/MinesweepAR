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

#include <opencv2/highgui/highgui.hpp>
#include <opencv2/calib3d/calib3d.hpp>
#include <opencv2/imgproc/imgproc.hpp>

#include "CameraUtil.h"
#include "TagDetector.h"

enum Tile {
    ONE = 1,
    TWO = 2,
    THREE = 3,
    FOUR = 4,
    FIVE = 5,
    SIX = 6,
    SEVEN = 7,
    EIGHT = 8,
    BLANK = 0,
    MINE = -1,
    FLAG = -2,
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

JNIEXPORT void JNICALL Java_com_build18_minesweepar_Overlay_overlayTilesNative
        (JNIEnv *, jobject, jlong address) {

    cv::Mat* pInputImage = (cv::Mat*)address;





}