LOCAL_PATH := $(call my-dir)

include $(CLEAN_VARS)

LOCAL_MODULE := Overlay
LOCAL_MODULE_FILENAME := libOverlay
LOCAL_SRC_FILES := Overlay.cpp
include $(BUILD_SHARED_LIBRARY)
