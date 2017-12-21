#include <jni.h>
#include <string>
#include "swype_detect.h"
#include "common.h"

void parseRowPaddedPlane(const unsigned char *frameData, unsigned int width, unsigned int height,
                         unsigned int rowStride, unsigned char *dest) {
    int i = 0;
    if (frameData == dest) {
        frameData += rowStride;
        dest += width;
        i = 1;
    }
    for (; i < height; i++) {
        memcpy(dest, frameData, width);
        frameData += rowStride;
        dest += width;
    }
}

void parsePixelPaddedPlane(const unsigned char *frameData, unsigned int width, unsigned int height,
                           unsigned int rowStride, unsigned int pixelStride,
                           unsigned char *dest) {
    for (int row = 0; row < height; row++) {
        const unsigned char *srcPos = frameData;
        for (int col = 0; col < width; col++) {
            *dest = *srcPos;
            ++dest;
            srcPos += pixelStride;
        }
        frameData += rowStride;
    }
}

extern "C" {

JNIEXPORT jlong JNICALL
Java_io_prover_provermvp_detector_ProverDetector_initSwype(JNIEnv *env, jobject instance,
                                                           jfloat videoAspectRatio,
                                                           jint detectorWidth,
                                                           jint detectorHeight) {
    SwypeDetect *detector = new SwypeDetect();
    detector->init(videoAspectRatio, detectorWidth, detectorHeight);
    logLevel = 1;
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
    detector->setSwype(swype);
}

JNIEXPORT void JNICALL
Java_io_prover_provermvp_detector_ProverDetector_detectFrameNV21(JNIEnv *env, jobject instance,
                                                                 jlong nativeHandler,
                                                                 jbyteArray frameData_, jint width,
                                                                 jint height, jint timestamp,
                                                                 jintArray result_) {

    SwypeDetect *detector = (SwypeDetect *) nativeHandler;
    jbyte *frameData = env->GetByteArrayElements(frameData_, NULL);
    jint *res = env->GetIntArrayElements(result_, NULL);

    static long counter = 0;
    LOGI_NATIVE("start frame detection %ld", counter);

    detector->processFrame_new((const unsigned char *) frameData, width, height, (uint) timestamp,
                               res[0], res[1], res[2], res[3], res[4]);

    env->ReleaseIntArrayElements(result_, res, JNI_COMMIT_AND_RELEASE);
    env->ReleaseByteArrayElements(frameData_, frameData, JNI_ABORT);
}

JNIEXPORT void JNICALL
Java_io_prover_provermvp_detector_ProverDetector_detectFrameY_18BufStrided(JNIEnv *env,
                                                                           jobject instance,
                                                                           jlong nativeHandler,
                                                                           jobject planeY,
                                                                           jint rowStride,
                                                                           jint pixelStride,
                                                                           jint width, jint height,
                                                                           jint timestamp,
                                                                           jintArray result_) {
    jint *res = env->GetIntArrayElements(result_, NULL);

    int rowWidth = width * pixelStride;
    int rowPadding = rowStride - rowWidth;
    int pixelPadding = pixelStride - 1;
    jlong extectedBufferSize = height * rowStride - rowPadding - pixelPadding;
    jlong len = env->GetDirectBufferCapacity(planeY);

    if (len < extectedBufferSize) {
        LOGE_NATIVE("detector buffers sizes: %zd, expected: %zd", len, extectedBufferSize);
        res[0] = static_cast<jint>(len);
        res[1] = static_cast<jint>(extectedBufferSize);
        env->ReleaseIntArrayElements(result_, res, JNI_COMMIT_AND_RELEASE);
        return;
    }

    unsigned char *frameData = (unsigned char *) env->GetDirectBufferAddress(planeY);

    if (rowStride > width) {
        if (pixelStride == 1) {
            parseRowPaddedPlane(frameData, (unsigned int) width, (unsigned int) height,
                                (unsigned int) rowStride, frameData);
        } else {
            parsePixelPaddedPlane(frameData, (unsigned int) width, (unsigned int) height,
                                  (unsigned int) rowStride, (unsigned int) pixelStride, frameData);
        }
    }

    SwypeDetect *detector = (SwypeDetect *) nativeHandler;
    detector->processFrame_new(frameData, width, height, (uint) timestamp, res[0], res[1], res[2],
                               res[3], res[4]);

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