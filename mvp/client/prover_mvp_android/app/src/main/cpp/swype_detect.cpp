#include "swype_detect.h"
#include "common.h"

using namespace cv;
using namespace std;

int logLevel = 0;

SwypeDetect::SwypeDetect() // initialization
{
    ocl::setUseOpenCL(true);
    count_num = -1;
    S = 0;
}

SwypeDetect::~SwypeDetect() {
    ocl::setUseOpenCL(false);
}

void SwypeDetect::init(double sourceAspectRatio, int detectorWidth, int detectorHeight) {
    _videoAspect = sourceAspectRatio > 1 ? sourceAspectRatio : 1.0 / sourceAspectRatio;
    SetDetectorSize(detectorWidth, detectorHeight);
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
        LOGI_NATIVE("detect2 SetDetectorSize (%d, %d) sourceAspect %f, -> (%f, %f)", detectorWidth,
                    detectorHeight, _videoAspect, _xMult, _yMult);
    }
}

void SwypeDetect::setSwype(string swype) {
    char t;
    int j;
    swype_Numbers.clear();
    swype_Numbers.resize(0);
    if (swype != "") {
        for (int i = 0; i < swype.length(); i++) {
            t = swype.at(i);
            j = t - '0';
            swype_Numbers.push_back(j);
        }
        count_num = 0;
    }
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
    scaledShift._timestamp = timestamp;
    if (logLevel > 0) {
        LOGI_NATIVE("detect2 t%d shift (%f, %f), scaled (f, %f)", timestamp, shift.x, shift.y,
                    scaledShift._x, scaledShift._y);
    }

    if (S == 0) {
        if (scaledShift._mod > MIN_SHIFT_RADIUS) {
            _circleDetector.AddShift(scaledShift);
            if (_circleDetector.IsCircle()) {
                _circleDetector.Reset();
                if (swype_Numbers.empty()) {
                    MoveToState(1, timestamp, 0);
                } else {
                    MoveToState(2, timestamp, PAUSE_TO_STATE_3_MS);
                }
            }
        }
        x = (int) (scaledShift._x * 1024);
        y = (int) (scaledShift._y * 1024);
    } else if (S == 2) {
        if (timestamp >= _maxStateEndTime) {
            _swipeStepDetector.Configure(1.5, MAX_DETECTOR_DEVIATION, 4);
            _swipeStepDetector.SetSwipeStep(swype_Numbers[0], swype_Numbers[1]);
            count_num = 0;
            MoveToState(3, timestamp, TIME_PER_EACH_SWIPE_STEP * (uint) (swype_Numbers.size()));
        }
    } else if (S == 3) {
        if (timestamp > _maxStateEndTime) {
            MoveToState(0, timestamp, 0);
        } else if (scaledShift._mod > MIN_SHIFT_RADIUS) {
            _swipeStepDetector.Add(scaledShift);
            int status = _swipeStepDetector.CheckState();
            if (status == 0) {}
            else if (status == 1) {
                ++count_num;
                if (swype_Numbers.size() == (count_num + 1)) {
                    MoveToState(4, timestamp, 0);
                    _swipeStepDetector.FinishStep();
                } else {
                    _swipeStepDetector.AdvanceSwipeStep(swype_Numbers[count_num + 1]);
                }
            } else if (status == -1) {
                MoveToState(0, timestamp, 0);
                count_num = 0;
            }
        }
        x = (int) (_swipeStepDetector._current._x * 1024);
        y = (int) (_swipeStepDetector._current._y * 1024);
    } else if (S == 4) {
        _swipeStepDetector.Add(scaledShift);
        x = (int) (_swipeStepDetector._current._x * 1024);
        y = (int) (_swipeStepDetector._current._y * 1024);
    }
    state = S;
    index = count_num + 1;
}

void SwypeDetect::MoveToState(int state, uint currentTimestamp, uint maxStateDuration) {
    S = state;
    _maxStateEndTime = maxStateDuration == 0 ? (uint) -1 : currentTimestamp + maxStateDuration;
}
