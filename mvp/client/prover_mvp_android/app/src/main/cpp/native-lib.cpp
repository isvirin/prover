#include <jni.h>
#include <string>
#include "swype_detect.h"
#include "common.h"

extern "C" {

JNIEXPORT jlong JNICALL
Java_io_prover_provermvp_detector_ProverDetector_initSwype(JNIEnv *env, jobject instance, jint fps,
                                                           jstring swype_) {
    std::string swype;

    if (swype_ == NULL) {
        swype = "";
    } else {
        const char *chars = env->GetStringUTFChars(swype_, 0);
        int len = env->GetStringUTFLength(swype_);
        char *chars2 = new char[len + 1];;
        chars2[len] = 0;
        memcpy(chars2, chars, len);
        swype = std::string(chars);
        delete[] chars2;
        env->ReleaseStringUTFChars(swype_, chars);
    }

    LOGI_NATIVE("initialising detector, fps %d, swype %s ", fps, swype.c_str());

    SwypeDetect *detector = new SwypeDetect();
    detector->init(fps, swype);
    return (jlong) detector;
}

JNIEXPORT void JNICALL
Java_io_prover_provermvp_detector_ProverDetector_setSwype(JNIEnv *env, jobject instance,
                                                          jlong nativeHandler, jstring swype_,
                                                          jint fps) {
    std::string swype;

    if (swype_ == NULL) {
        swype = "";
    } else {
        const char *chars = env->GetStringUTFChars(swype_, 0);
        int len = env->GetStringUTFLength(swype_);
        char *chars2 = new char[len + 1];;
        chars2[len] = 0;
        memcpy(chars2, chars, len);
        swype = std::string(chars);
        delete[] chars2;
        env->ReleaseStringUTFChars(swype_, chars);
    }

    LOGI_NATIVE("detection: set swype %s", swype.c_str());

    SwypeDetect *detector = (SwypeDetect *) nativeHandler;
    detector->init(fps, swype);
}

JNIEXPORT void JNICALL
Java_io_prover_provermvp_detector_ProverDetector_detectFrameNV21(
        JNIEnv *env, jobject instance,
        jlong nativeHandler,
        jbyteArray frameData_, jint width,
        jint height, jintArray result_) {

    SwypeDetect *detector = (SwypeDetect *) nativeHandler;
    jbyte *frameData = env->GetByteArrayElements(frameData_, NULL);
    jint *res = env->GetIntArrayElements(result_, NULL);

    static long counter = 0;
    LOGI_NATIVE("start frame detection %ld", counter);

    int state = 0, index = 0, x = 0, y = 0, d = 0;

    detector->processFrame_new((const unsigned char *) frameData, width, height, 0, state, index, x,
                               y, d);
    res[0] = static_cast<jint>(state);
    res[1] = static_cast<jint>(index);
    res[2] = static_cast<jint>(x);
    res[3] = static_cast<jint>(y);
    res[4] = static_cast<jint>(d);
//    LOGI_NATIVE("done frame detection %ld", counter++);

    env->ReleaseIntArrayElements(result_, res, JNI_COMMIT_AND_RELEASE);
    env->ReleaseByteArrayElements(frameData_, frameData, JNI_ABORT);
}

JNIEXPORT jlong JNICALL
Java_io_prover_provermvp_detector_ProverDetector_detectFrameY_18Buf(JNIEnv *env, jobject instance,
                                                                    jlong nativeHandler,
                                                                    jobject planeY, jint width,
                                                                    jint height, jint timestamp,
                                                                    jintArray result_) {
    jint *res = env->GetIntArrayElements(result_, NULL);
    SwypeDetect *detector = (SwypeDetect *) nativeHandler;

    const unsigned char *frameData = (unsigned char *) env->GetDirectBufferAddress(planeY);
    jlong len = env->GetDirectBufferCapacity(planeY);
    jlong expectedLength = width * height;


    if (len < expectedLength) {
        LOGE_NATIVE("detector buffers sizes: %zd, expected: %zd", len, expectedLength);
        res[0] = static_cast<jint>(len);
        res[1] = static_cast<jint>(expectedLength);
        env->ReleaseIntArrayElements(result_, res, JNI_COMMIT_AND_RELEASE);
        return 0;
    }

    int state = 0, index = 0, x = 0, y = 0, d = 0;

    detector->processFrame_new(frameData, width, height, (uint) timestamp, state, index, x, y, d);
    res[0] = state;
    res[1] = index;
    res[2] = x;
    res[3] = y;
    res[4] = d;

    env->ReleaseIntArrayElements(result_, res, 0);
    return d;
}

JNIEXPORT void JNICALL
Java_io_prover_provermvp_detector_ProverDetector_releaseNativeHandler(JNIEnv *env, jobject instance,
                                                                      jlong nativeHandler) {

    SwypeDetect *detector = (SwypeDetect *) nativeHandler;
    if (detector == NULL) {
        LOGE_NATIVE("trying to close NULL detector");
    } else {
        LOGI_NATIVE("requested detector close");
        delete detector;
        LOGI_NATIVE("detector closed");
    }
}

}