//
// Created by babay on 07.12.2017.
//

#include "SwypeStepDetector.h"
#include "common.h"

void SwypeStepDetector::Add(VectorExplained other) {
    //LOGI_NATIVE("detect2 add vector (%f, %f)", other._x, other._y);
    other._x *= _speedMultX;
    other._y *= _speedMultY;
    VectorExplained::Add(other);
    //LOGI_NATIVE("detect2 added vector (%f, %f), got (%f, %f)", other._x, other._y, _x, _y);
    _count++;
}

float pointDistance(float x1, float y1, float x2, float y2) {
    float dx = x2 - x1;
    float dy = y2 - y1;
    return sqrtf(dx * dx + dy * dy);
}

void SwypeStepDetector::Reset() {
    VectorExplained::Reset();
    _count = 0;
    _targetX = 0;
    _targetY = 0;
    _currentSwypePoint = 0;
    _nextSwypePoint = 0;
    _isDiagonal = false;
}

void SwypeStepDetector::Configure(int width, int height, float speedMult, float maxDeviation) {
    int size = width < height ? width : height;
    _speedMultX = 2.0f / size * speedMult;
    _speedMultY = 2.0f / size * speedMult;
    _maxDeviation = maxDeviation;
}

bool SwypeStepDetector::SetSwipeStep(int currentPoint, int nextPoint) {
    Reset();
    _currentSwypePoint = currentPoint;
    return SetNextSwipePoint(nextPoint);
}

bool SwypeStepDetector::AdvanceSwipeStep(int nextPoint) {
    _count = 0;
    _x -= _targetX;
    _y -= _targetY;
    _currentSwypePoint = _nextSwypePoint;
    return SetNextSwipePoint(nextPoint);
}

int SwypeStepDetector::CheckState() {
    bool reachedBounds = _mod > (_isDiagonal ? _sqrt2 : 1.0);

#ifdef REQUIRE_REACH_BOUNDS
    if (reachedBounds) {
        float distance = pointDistance(_x, _y, _targetX, _targetY);
        //LOGI_NATIVE("detect2 reached bounds, target: (%f %f), current: (%f %f), distance: %f", _targetX, _targetY, _x, _y, distance);
        return distance <= _targetRadius ? 1 : -1;
    }
#else
    float distance = pointDistance(_x, _y, _targetX, _targetY);
    if (distance <= _targetRadius)
        return 1;
    if (reachedBounds) {
        LOGI_NATIVE("detect2 failing (%f %f) target (%f %f) distance %f max %f mod %f", _x, _y,
                    _targetX, _targetY, distance, (_isDiagonal ? _sqrt2 : 1.0), _mod);
        return -1;
    }
#endif

    int directionDiff = (_direction - _targetDirection + 12) % 8 - 4;
    if (abs(directionDiff) > 2 && _mod > _badDirectionMaxRadius) {
        LOGI_NATIVE("detect2 bad direction: cur: %d, target: %d, diff: %d, mod: %f", _direction,
                    _targetDirection, directionDiff, _mod);
        return -1;
    }
    return 0;
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
    _targetX = dx;
    _targetY = dy;
    _targetRadius = _isDiagonal ? _maxDeviation * _sqrt2 : _maxDeviation;

    VectorExplained temp = VectorExplained(_targetX, _targetY);
    _targetDirection = temp._direction;
    _nextSwypePoint = nextPoint + 1;
    LOGI_NATIVE("detect2 select src: %d, dst: %d, dx %d, dy %d, dir %d", currentPoint, nextPoint,
                dx, dy, _targetDirection);
    return true;
}



