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

JNIEXPORT jstring JNICALL Java_com_build18_minesweepar_Overlay_overlayTilesNative
        (JNIEnv * env, jobject) {

    cv::Mat frame;
    TagDetectorParams params;

    double f = 500;
    double x = 30;
    double y = 50;

    cv::Mat K = (cv::Mat_<double>(3, 3) <<
                 f, 0, x,
            0, f, y,
            0, 0, 1
    );



//    char s[100];
//    sprintf(s,"%d",K.dims);
//
//
//    return env->NewStringUTF((std::string("Basic") + std::string(s)).c_str());


    jmethodID cnstrctr;
    jclass c = (*env)->FindClass(env, "com/test/DeviceId");
        if (c == 0) {
            printf("Find Class Failed.\n");
         }else{
            printf("Found class.\n");
         }

        cnstrctr = (*env)->GetMethodID(env, c, "<init>", "(Ljava/lang/String;[B)V");
        if (cnstrctr == 0) {
            printf("Find method Failed.\n");
        }else {
            printf("Found method.\n");
        }

        return (*env)->NewObject(env, c, cnstrctr, id, cache);


//    return K;


}