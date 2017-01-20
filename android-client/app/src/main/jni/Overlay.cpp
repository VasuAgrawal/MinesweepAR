//
// Created by Reid Long on 1/19/17.
//

#include "com_build18_minesweepar_Overlay.h"

#include <opencv2/highgui/highgui.hpp>
#include <opencv2/calib3d/calib3d.hpp>
#include <opencv2/imgproc/imgproc.hpp>

//#include "apriltags/CameraUtil.h"
//#include "apriltags/TagDetector.h"

JNIEXPORT jstring JNICALL Java_com_build18_minesweepar_Overlay_overlayTilesNative
        (JNIEnv * env, jobject) {

    cv::Mat frame;
//    TagDetectorParams params;

    return env->NewStringUTF("This is from JNI");


}