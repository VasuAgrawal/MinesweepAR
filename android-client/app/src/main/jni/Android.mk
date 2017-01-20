LOCAL_PATH := $(call my-dir)

include $(CLEAN_VARS)

OPENCV_INSTALL_MODULES:=on
OPENCV_CAMERA_MODULES:=off
OPENCV_LIB_TYPE:=STATIC

include /Users/Reid/Developer/CMU/Build18/MinesweepAR/OpenCV-android-sdk/sdk/native/jni/OpenCV.mk

LOCAL_MODULE := Overlay
LOCAL_MODULE_FILENAME := libOverlay
LOCAL_SRC_FILES := Overlay.cpp
include $(BUILD_SHARED_LIBRARY)
