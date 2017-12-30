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
    _currentSwypePoint = 0;
    _nextSwypePoint = 0;
    _isDiagonal = false;
}

void SwypeStepDetector::Configure(double speedMult, double maxDeviation, double attraction) {
    _speedMultX = speedMult;
    _speedMultY = speedMult;
    _maxDeviation = maxDeviation;
    _attraction = attraction;
}

bool SwypeStepDetector::SetSwipeStep(int currentPoint, int nextPoint) {
    Reset();
    _currentSwypePoint = currentPoint;
    return SetNextSwipePoint(nextPoint);
}

bool SwypeStepDetector::AdvanceSwipeStep(int nextPoint) {
    FinishStep();
    return SetNextSwipePoint(nextPoint);
}

void SwypeStepDetector::FinishStep() {
    _count = 0;
    _current -= _target;
    _currentSwypePoint = _nextSwypePoint;

    if (logLevel > 0) {
        LOGI_NATIVE("FinishStep (%f %f) ", _current._x, _current._y);
    }
}

int SwypeStepDetector::CheckState(bool withDefect) {

    double distance = withDefect ? _current.MinDistanceToWithDefect(_target) :
                      _current.DistanceTo(_target);

    if (logLevel > 0) {
        LOGI_NATIVE(
                "CheckState |(%+.4f %+.4f) - (%+.4f %+.4f)|= %.4f, total: |%+.4f+-%.4f %+.4f+-%.4f| = %.4f+-%.4f defSum |%.4f,%.4f|= %.4f",
                _current._x, _current._y, _target._x, _target._y, distance,
                _total._x, _total._defectX, _total._y, _total._defectY, _total._mod,
                _total.ModDefect(), _current._defectX, _current._defectY, _current.ModDefect());
    }

    if (distance <= _targetRadius) {
        LOGI_NATIVE("CheckState reached ");
        return 1;
    }

    bool boundsCheckResult = withDefect ? _BoundsChecker.CheckBoundsWithDefect(_current)
                                        : _BoundsChecker.CheckBounds(_current);

    if (!boundsCheckResult) {
        LOGI_NATIVE("CheckState boundsCheck failing ");
    }

    return boundsCheckResult ? 0 : -1;
}

bool SwypeStepDetector::SetNextSwipePoint(int nextPoint) {
    int currentPoint = _currentSwypePoint - 1;
    --nextPoint;
    int sourceX = currentPoint % 3;
    int sourceY = currentPoint / 3;
    int targetX = nextPoint % 3;
    int targetY = nextPoint / 3;
    int dx = targetX - sourceX;
    int dy = targetY - sourceY;
    if (abs(dx) > 1 || abs(dy) > 1)
        return false;

    _isDiagonal = dx != 0 && dy != 0;
    _target.Set(dx, dy);
    _targetRadius = (float) _maxDeviation;
    _nextSwypePoint = nextPoint + 1;

    _BoundsChecker.SetDirection(_target._direction);
    _BoundsChecker.SetTargetRadius(_targetRadius);

    LOGI_NATIVE("SetNextSwipePoint select %d => %d, (%d,%d) dir %d",
                currentPoint + 1, nextPoint + 1, dx, dy, _target._direction);
    return true;
}

