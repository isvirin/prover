//
// Created by babay on 06.01.2018.
//

#include "SwypeCodeDetector.h"
#include "VectorExplained.h"

SwypeCodeDetector::SwypeCodeDetector(SwipeCode code, double speedMult, float maxDeviation,
                                     bool relaxed, unsigned int timestamp) : _code(code),
                                                                             _relaxed(relaxed) {
    _maxTimestamp = timestamp + MS_PER_SWIPE_STEP * (_code._length + 1);
    _stepDetector.Configure(speedMult, maxDeviation, _relaxed);
    _stepDetector.SetDirection(_code._directions[0]);
}

void SwypeCodeDetector::Init(SwipeCode &code, double speedMult, float maxDeviation, bool relaxed,
                             unsigned int timestamp) {
    _code = code;
    _stepDetector.Configure(speedMult, maxDeviation, relaxed);
    _stepDetector.SetDirection(_code._directions[0]);
    _relaxed = relaxed;
    _currentStep = 0;
    _maxTimestamp = timestamp + MS_PER_SWIPE_STEP * (_code._length + 1);
}

int SwypeCodeDetector::Add(VectorExplained &shift) {
    if (shift._timestamp > _maxTimestamp)
        return -2;
    if (shift._mod <= 0)
        return 0;
    _stepDetector.Add(shift);
    int res = _stepDetector.CheckState(_relaxed);
    if (res == 1) {
        if (++_currentStep >= _code._length) {
            _stepDetector.FinishStep();
            return 1;
        }
        _stepDetector.AdvanceDirection(_code._directions[_currentStep]);
        return 0;
    }
    return res;
}

void SwypeCodeDetector::FillResult(int &index, int &x, int &y, int &debug) {
    index = _currentStep + 1;
    x = (int) (_stepDetector._current._x * 1024);
    y = (int) (_stepDetector._current._y * 1024);

    debug = (int) (_stepDetector._current._defectX * 1024);
    debug = debug << 16;
    debug += _stepDetector._current._defectY * 1024;
}
