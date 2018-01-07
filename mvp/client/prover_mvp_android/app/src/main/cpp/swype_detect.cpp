#include "swype_detect.h"
#include "common.h"

using namespace cv;
using namespace std;

int logLevel = 0;

SwypeDetect::SwypeDetect() // initialization
{
    ocl::setUseOpenCL(true);
    S = 0;
}

SwypeDetect::~SwypeDetect() {
    ocl::setUseOpenCL(false);
}

void SwypeDetect::init(double sourceAspectRatio, int detectorWidth, int detectorHeight) {
    _videoAspect = sourceAspectRatio > 1 ? sourceAspectRatio : 1.0 / sourceAspectRatio;
    SetDetectorSize(detectorWidth, detectorHeight);
    setRelaxed(true);
}

void SwypeDetect::SetDetectorSize(int detectorWidth, int detectorHeight) {
    _detectorWidth = detectorWidth;
    _detecttorHeight = detectorHeight;
    if (detectorWidth > detectorHeight) {
        _yMult = -2.0 / detectorHeight;
        _xMult = -2.0 / detectorWidth * _videoAspect;
    } else {
        _xMult = -2.0 / detectorWidth;
        _yMult = -2.0 / detectorHeight * _videoAspect;
    }

    if (logLevel > 0) {
        LOGI_NATIVE("SetDetectorSize (%d, %d) sourceAspect %f, -> (%f, %f)", detectorWidth,
                    detectorHeight, _videoAspect, _xMult, _yMult);
    }
}

void SwypeDetect::setSwype(string swype) {
    swipeCode.Init(swype);
}

Point2d SwypeDetect::Frame_processor(cv::Mat &frame_i) {
    if (buf1ft.empty()) {
        frame_i.convertTo(buf1ft, CV_64F);// converting frames to CV_64F type
        createHanningWindow(hann, buf1ft.size(), CV_64F); //  create Hanning window
        _tickTock = false;
        return Point2d(0, 0);
    }

    _tickTock = !_tickTock;
    if (_tickTock) {
        frame_i.convertTo(buf2ft, CV_64F);// converting frames to CV_64F type
        return phaseCorrelate(buf1ft, buf2ft, hann); // we calculate a phase offset vector
    } else {
        frame_i.convertTo(buf1ft, CV_64F);// converting frames to CV_64F type
        return phaseCorrelate(buf2ft, buf1ft, hann); // we calculate a phase offset vector
    }
}

void SwypeDetect::processFrame_new(const unsigned char *frame_i, int width_i, int height_i,
                                   uint timestamp, int &state, int &index, int &x, int &y,
                                   int &debug) {
    Mat frame(height_i, width_i, CV_8UC1, (uchar *) frame_i);
    Point2d shift = Frame_processor(frame);

    if (_detectorWidth != width_i || _detecttorHeight != height_i) {
        SetDetectorSize(_detectorWidth, _detecttorHeight);
    }

    VectorExplained scaledShift;
    scaledShift.SetMul(shift, _xMult, _yMult);
    VectorExplained windowedShift = scaledShift;
    //if (_relaxed)
    windowedShift.ApplyWindow(VECTOR_WINDOW_START, VECTOR_WINDOW_END);
    windowedShift.setRelativeDefect(_relaxed ? DEFECT : DEFECT_CLIENT);
    windowedShift._timestamp = timestamp;

    if (logLevel > 0 && windowedShift._mod > 0) {
        LOGI_NATIVE(
                "t%d shift (%+6.2f,%+6.2f), scaled |%+.4f,%+.4f|=%.4f windowed |%+.4f,%+.4f|=%.4f_%3.0f_%d",
                timestamp, shift.x, shift.y,
                scaledShift._x, scaledShift._y, scaledShift._mod,
                windowedShift._x, windowedShift._y, windowedShift._mod, windowedShift._angle,
                windowedShift._direction);
    }

    index = 1;
    if (S == 0) {
        if (windowedShift._mod > 0) {
            _circleDetector.AddShift(windowedShift);
            if (_circleDetector.IsCircle()) {
                _circleDetector.Reset();
                if (swipeCode.empty()) {
                    MoveToState(1, timestamp);
                } else {
                    MoveToState(2, timestamp);
                    _codeDetector.Init(swipeCode, 1, MAX_DETECTOR_DEVIATION, _relaxed, timestamp);
                }
            }
        }
        x = (int) (windowedShift._x * 1024);
        y = (int) (windowedShift._y * 1024);
    } else if (S == 1) {
        if (!swipeCode.empty()) {
            MoveToState(2, timestamp);
            _codeDetector.Init(swipeCode, 1, MAX_DETECTOR_DEVIATION, _relaxed, timestamp);
        }
    } else if (S == 2) {
        _codeDetector.Add(windowedShift);
        if (_codeDetector._status < 2) {
            MoveToState(3, timestamp);
        }
    } else if (S == 3) {
        _codeDetector.Add(windowedShift);
        _codeDetector.FillResult(index, x, y, debug);
        if (_codeDetector._status == 0);
        else if (_codeDetector._status < 0) {
            MoveToState(0, timestamp);
        } else if (_codeDetector._status == 1) {
            MoveToState(4, timestamp);
        }
    } else if (S == 4) {
        _codeDetector.Add(windowedShift);
        _codeDetector.FillResult(index, x, y, debug);
    }
    state = S;
}

void SwypeDetect::MoveToState(int state, uint timestamp) {
    S = state;
    LOGI_NATIVE("MoveToState %d", S);
}

void SwypeDetect::setRelaxed(bool relaxed) {
    _circleDetector.SetRelaxed(relaxed);
    _relaxed = relaxed;
}