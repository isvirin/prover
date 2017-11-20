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
        swype = std::string(chars);
        env->ReleaseStringUTFChars(swype_, chars);
    }

    LOGI_NATIVE("initialising detector, fps %d, swype %s ", fps, swype.c_str());

    SwypeDetect *detector = new SwypeDetect();
    detector->init(fps, swype);
    return (jlong) detector;
}

JNIEXPORT void JNICALL
Java_io_prover_provermvp_detector_ProverDetector_setSwype(JNIEnv *env, jobject instance,
                                                          jlong nativeHandler, jstring swype_) {
    std::string swype;

    if (swype_ == NULL) {
        swype = "";
    } else {
        const char *chars = env->GetStringUTFChars(swype_, 0);
        swype = std::string(chars);
        env->ReleaseStringUTFChars(swype_, chars);
    }

    LOGI_NATIVE("detection: set swype %s", swype.c_str());

    SwypeDetect *detector = (SwypeDetect *) nativeHandler;
    detector->setSwype(swype);
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

//    static long counter = 0;
//    LOGI_NATIVE("start frame detection %ld", counter);
    detector->processFrame((const unsigned char *) frameData, width, height, res[0], res[1], res[2],
                           res[3]);
//    LOGI_NATIVE("done frame detection %ld", counter++);

    env->ReleaseIntArrayElements(result_, res, JNI_COMMIT_AND_RELEASE);
    env->ReleaseByteArrayElements(frameData_, frameData, JNI_ABORT);
}

JNIEXPORT void JNICALL
Java_io_prover_provermvp_detector_ProverDetector_detectFrameNV21Buf(JNIEnv *env, jobject instance,
                                                                    jlong nativeHandler,
                                                                    jobject data, jint width,
                                                                    jint height,
                                                                    jintArray result_) {
    SwypeDetect *detector = (SwypeDetect *) nativeHandler;
    jint *res = env->GetIntArrayElements(result_, NULL);
    const unsigned char *frameData = (unsigned char *) env->GetDirectBufferAddress(data);
    jlong len = env->GetDirectBufferCapacity(data);
    jlong expectedLength = width * height * 3 / 2;

    LOGI_NATIVE("start frameDetectionNV12buf, buffer: %ld, capacyty: %lld/%lld", (long) frameData,
                len, expectedLength);

    detector->processFrame(frameData, width, height, res[0], res[1], res[2], res[3]);

    env->ReleaseIntArrayElements(result_, res, JNI_COMMIT_AND_RELEASE);
}

JNIEXPORT void JNICALL
Java_io_prover_provermvp_detector_ProverDetector_detectFrameYUV420_1888Buf(JNIEnv *env,
                                                                           jobject instance,
                                                                           jlong nativeHandler,
                                                                           jobject planeY,
                                                                           jobject planeU,
                                                                           jobject planeV,
                                                                           jint width, jint height,
                                                                           jintArray result_) {
    size_t size = (size_t) width * (size_t) height * 3 / 2;
    size_t arrYSize = (size_t) env->GetDirectBufferCapacity(planeY);
    size_t arrUSize = (size_t) env->GetDirectBufferCapacity(planeU);
    size_t arrVSize = (size_t) env->GetDirectBufferCapacity(planeV);
    if (size != arrYSize + arrUSize + arrVSize) {
        LOGE_NATIVE("detector buffers sizes: %zd, %zd, %zd, expected sum: %zd", arrYSize, arrUSize,
                    arrVSize, size);
        return;
    }

    SwypeDetect *detector = (SwypeDetect *) nativeHandler;
    jint *res = env->GetIntArrayElements(result_, NULL);

    unsigned char *frameData = (unsigned char *) malloc(size);

    memcpy(frameData, env->GetDirectBufferAddress(planeY), arrYSize);
    memcpy(frameData + arrYSize, env->GetDirectBufferAddress(planeU), arrUSize);
    memcpy(frameData + arrVSize + arrUSize, env->GetDirectBufferAddress(planeV), arrUSize);

    detector->processFrame(frameData, width, height, res[0], res[1], res[2], res[3]);

    free(frameData);

    env->ReleaseIntArrayElements(result_, res, JNI_COMMIT_AND_RELEASE);
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
