LOCAL_PATH := $(call my-dir)
LOCAL_LDLIBS := -llog

include $(CLEAN_VARS)

OPENCV_INSTALL_MODULES:=on
OPENCV_CAMERA_MODULES:=off
OPENCV_LIB_TYPE:=STATIC

include /Users/Reid/Developer/CMU/Build18/MinesweepAR/OpenCV-android-sdk/sdk/native/jni/OpenCV.mk

LOCAL_MODULE := Overlay
LOCAL_MODULE_FILENAME := libOverlay
LOCAL_SRC_FILES := Overlay.cpp CameraUtil.cpp DebugImage.cpp Geometry.cpp GrayModel.cpp MathUtil.cpp
LOCAL_SRC_FILES += Refine.cpp TagDetector.cpp TagFamily.cpp TagFamilies.cpp UnionFindSimple.cpp

include $(BUILD_SHARED_LIBRARY)
