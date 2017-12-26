//
// Created by babay on 07.12.2017.
//

#include "SwypeStepDetector.h"
#include "common.h"

void SwypeStepDetector::Add(VectorExplained shift) {
    shift._x *= _speedMultX;
    shift._y *= _speedMultY;

    /*double angle = fabs(shift.AngleTo(_target));
    if (angle < MAX_ATTRACT_ANGLE) {
        shift._mod = shift.Length();
        //float attraction = angle/MAX_ATTRACT_ANGLE * CV_PI / 2
        double attraction = (MAX_ATTRACT_ANGLE - angle) / MAX_ATTRACT_ANGLE;
        attraction *= attraction * _attraction;
        shift.AttractTo(_target, attraction);
    }*/

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

int SwypeStepDetector::CheckState() {
    bool reachedBounds = _current._mod > (_isDiagonal ? _sqrt2 : 1.0);
    double distance = _current.DistanceTo(_target);

    if (logLevel > 0) {
        LOGI_NATIVE("CheckState |(%+.4f %+.4f) - (%+.4f %+.4f)|= %.4f, total: (%+.4f %+.4f)",
                    _current._x, _current._y, _target._x, _target._y, distance, _total._x,
                    _total._y);
    }

#ifdef REQUIRE_REACH_BOUNDS
    if (reachedBounds) {
        float distance = pointDistance(_x, _y, _targetX, _targetY);
        //LOGI_NATIVE("detect2 reached bounds, target: (%f %f), current: (%f %f), distance: %f", _targetX, _targetY, _x, _y, distance);
        return distance <= _targetRadius ? 1 : -1;
    }
#else
    if (distance <= _targetRadius) {
        LOGI_NATIVE("CheckState reached ");
        return 1;
    }
    /*if (reachedBounds) {
        LOGI_NATIVE("CheckState failing ");
        return -1;
    }*/
#endif

    bool boundsCheckResult = _BoundsChecker.CheckBounds(_current);

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
    //_targetRadius = _isDiagonal ? _maxDeviation * _sqrt2 : _maxDeviation;
    _targetRadius = _maxDeviation;
    _nextSwypePoint = nextPoint + 1;

    _BoundsChecker.SetDirection(_target._direction);

    LOGI_NATIVE("SetNextSwipePoint select %d => %d, (%d,%d) dir %d",
                currentPoint + 1, nextPoint + 1, dx, dy, _target._direction);
    return true;
}

void SwypeStepDetector::setTolerance(double tolerance) {
    _BoundsChecker.setTolerance(tolerance);
}

