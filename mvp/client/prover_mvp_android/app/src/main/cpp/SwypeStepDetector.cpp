//
// Created by babay on 07.12.2017.
//

#include "SwypeStepDetector.h"
#include "common.h"

void SwypeStepDetector::Add(VectorExplained shift) {
    shift._x *= _speedMultX;
    shift._defectX *= _speedMultX;
    shift._y *= _speedMultY;
    shift._defectY *= _speedMultY;

    _current.Add(shift);
    _current._timestamp = shift._timestamp;
    _total.Add(shift);
    _count++;
}

void SwypeStepDetector::Reset() {
    _current.Reset();
    _target.Reset();
    _total.Reset();
    _count = 0;
}

void SwypeStepDetector::Configure(double speedMult, float maxDeviation, bool relaxed) {
    _speedMultX = speedMult;
    _speedMultY = speedMult;
    _defaultTargetRadius = maxDeviation;
    _relaxed = relaxed;
}

void SwypeStepDetector::FinishStep() {
    _count = 0;
    _current -= _target;

    if (logLevel > 0) {
        LOGI_NATIVE("FinishStep %d (%f %f) ", _id, _current._x, _current._y);
    }
}

int SwypeStepDetector::CheckState(bool withDefect) {
    double distance = withDefect ? _current.MinDistanceToWithDefect(_target) :
                      _current.DistanceTo(_target);

    if (logLevel > 0) {
        LOGI_NATIVE(
                "CheckState %d |(%+.4f %+.4f) - (%+.4f %+.4f)|= %.4f, total: |%+.4f+-%.4f %+.4f+-%.4f| = %.4f+-%.4f defSum |%.4f,%.4f|= %.4f",
                _id,
                _current._x, _current._y, _target._x, _target._y, distance,
                _total._x, _total._defectX, _total._y, _total._defectY, _total._mod,
                _total.ModDefect(), _current._defectX, _current._defectY, _current.ModDefect());
    }

    if (distance <= _targetRadius) {
        if (logLevel > 0) {
            LOGI_NATIVE("CheckState %d reached ", _id);
        }
        return 1;
    }

    bool boundsCheckResult = withDefect ? _BoundsChecker.CheckBoundsWithDefect(_current)
                                        : _BoundsChecker.CheckBounds(_current);

    if (!boundsCheckResult) {
        if (logLevel > 0) {
            LOGI_NATIVE("CheckState %d boundsCheck failing ", _id);
        }
    }

    return boundsCheckResult ? 0 : -1;
}

void SwypeStepDetector::SetDirection(int dir) {
    Reset();
    _target.SetDirection(dir);
    SetTarget(_target);
}


void SwypeStepDetector::AdvanceDirection(int dir) {
    FinishStep();
    _target.SetDirection(dir);
    SetTarget(_target);
}

void SwypeStepDetector::SetTarget(VectorExplained target) {
    _target = target;

    _targetRadius = _defaultTargetRadius;
    if (_relaxed && _target._direction % 2 == 0) {// for diagonal target at server
        _targetRadius *= 1.65;
    }

    _BoundsChecker.SetDirection(_target._direction);
    _BoundsChecker.SetTargetRadius(_targetRadius, _defaultTargetRadius);

    if (logLevel > 0) {
        LOGI_NATIVE("SetTarget %d (%.1f %.1f) d %d", _id, target._x, target._y, target._direction);
    }
}