
#ifndef COMMON_C_
#define COMMON_C_

#define APPNAME "ProverMVP"

#ifdef __ANDROID_API__

#include <android/log.h>

#define LOGI_NATIVE(...) ((void)__android_log_print(ANDROID_LOG_INFO, APPNAME, __VA_ARGS__))
#define LOGE_NATIVE(...) ((void)__android_log_print(ANDROID_LOG_ERROR, APPNAME, __VA_ARGS__))

#else
#define LOGI_NATIVE(...) ((void)fprintf(stderr, __VA_ARGS__))
#define LOGE_NATIVE(...) ((void)fprintf(stderr, __VA_ARGS__))

#endif

#define JNI_COMMIT_AND_RELEASE 0


inline float pointDistance(float x1, float y1, float x2, float y2) {
    float dx = x2 - x1;
    float dy = y2 - y1;
    return sqrtf(dx * dx + dy * dy);
}

#endif