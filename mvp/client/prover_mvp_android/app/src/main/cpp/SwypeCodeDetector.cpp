//
// Created by babay on 06.01.2018.
//

#include "SwypeCodeDetector.h"
#include "VectorExplained.h"

unsigned int SwypeCodeDetector::counter = 0;

SwypeCodeDetector::SwypeCodeDetector(SwipeCode &code, double speedMult, float maxDeviation,
                                     bool relaxed, unsigned int timestamp) : _code(code),
                                                                             _relaxed(relaxed),
                                                                             _id(++counter),
                                                                             _stepDetector(_id) {
    _stepDetector.Configure(speedMult, maxDeviation, _relaxed);
    _stepDetector.SetDirection(_code._directions[0]);
    _startTimestamp = timestamp + PAUSE_TO_ST3_MS_PER_STEP * (_code._length - 1);
    _maxTimestamp = _startTimestamp + MS_PER_SWIPE_STEP * _code._length;
}

void SwypeCodeDetector::Init(SwipeCode &code, double speedMult, float maxDeviation, bool relaxed,
                             unsigned int timestamp) {
    _code = code;
    _relaxed = relaxed;
    _stepDetector.Configure(speedMult, maxDeviation, relaxed);
    _stepDetector.SetDirection(_code._directions[0]);
    _currentStep = 0;
    _status = 2;
    _startTimestamp = timestamp + PAUSE_TO_ST3_MS_PER_STEP * (_code._length - 1);
    _maxTimestamp = _startTimestamp + MS_PER_SWIPE_STEP * _code._length;
}

void SwypeCodeDetector::Add(VectorExplained &shift) {
    if (shift._timestamp >= _startTimestamp) {
        if (shift._timestamp > _maxTimestamp) {
            _status = -2;
        } else if (shift._mod <= 0) {
            _status = 0;
        } else {
            _stepDetector.Add(shift);
            _status = _stepDetector.CheckState(_relaxed);
            if (_status == 1) {
                if (++_currentStep >= _code._length) {
                    _stepDetector.FinishStep();
                } else {
                    _stepDetector.AdvanceDirection(_code._directions[_currentStep]);
                    _status = 0;
                }
            }
        }
    }
}

void SwypeCodeDetector::FillResult(int &status, int &index, int &x, int &y, int &debug) {
    index = _currentStep + 1;
    x = (int) (_stepDetector._current._x * 1024);
    y = (int) (_stepDetector._current._y * 1024);

    debug = (int) (_stepDetector._current._defectX * 1024);
    debug = debug << 16;
    debug += _stepDetector._current._defectY * 1024;

    if (_status < 0)
        status = 0;
    else if (_status == 2)
        status = 2;
    else if (_status == 1)
        status = 4;
    else
        status = 3;
}
