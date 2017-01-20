//
// Created by Reid Long on 1/19/17.
//

#include "com_build18_minesweepar_Overlay.h"

JNIEXPORT jstring JNICALL Java_com_build18_minesweepar_Overlay_overlayTilesNative
        (JNIEnv * env, jobject) {
    return env->NewStringUTF("This is from JNI");
}