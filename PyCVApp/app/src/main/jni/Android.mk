LOCAL_PATH := $(call my-dir)

# Python-for-Android paths

PYTHON_FOR_ANDROID_PATH := $(LOCAL_PATH)/../../../../../python-for-android
PYTHON_PATH := $(PYTHON_FOR_ANDROID_PATH)/libs/arm64-v8a


# Build libpybridge.so

include $(CLEAR_VARS)
LOCAL_MODULE    := pybridge
LOCAL_SRC_FILES := pybridge.c
LOCAL_LDLIBS := -llog
LOCAL_SHARED_LIBRARIES := python3.7m
include $(BUILD_SHARED_LIBRARY)


# Include libpython3.7m.so

include $(CLEAR_VARS)
LOCAL_MODULE    := python3.7m
LOCAL_SRC_FILES := $(PYTHON_PATH)/libpython3.7m.so
LOCAL_EXPORT_CFLAGS := -I $(PYTHON_PATH)/Include
include $(PREBUILT_SHARED_LIBRARY)
