#ifndef COMMON_C_
#define COMMON_C_

#define APPNAME "ProverMVPDetector"

#ifdef __ANDROID_API__

#include <android/log.h>

//#define RECT_DEFECT

#define DEFECT 0.2

#define LOGI_NATIVE(...) ((void)__android_log_print(ANDROID_LOG_INFO, APPNAME, __VA_ARGS__))
#define LOGE_NATIVE(...) ((void)__android_log_print(ANDROID_LOG_ERROR, APPNAME, __VA_ARGS__))

#else

#include <stdio.h>

#define LOGI_NATIVE(fmt, ...) ((void)fprintf(stderr, fmt "\n", ##__VA_ARGS__))
#define LOGE_NATIVE(fmt, ...) ((void)fprintf(stderr, fmt "\n", ##__VA_ARGS__))

#endif

extern int logLevel;

#define JNI_COMMIT_AND_RELEASE 0

#endif