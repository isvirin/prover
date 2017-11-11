
#ifndef COMMON_C_
#define COMMON_C_

#define APPNAME "ProverMVP"

#include <android/log.h>

#define LOGI_NATIVE(...) ((void)__android_log_print(ANDROID_LOG_INFO, APPNAME, __VA_ARGS__))
#define LOGE_NATIVE(...) ((void)__android_log_print(ANDROID_LOG_ERROR, APPNAME, __VA_ARGS__))

#define JNI_COMMIT_AND_RELEASE 0


#endif