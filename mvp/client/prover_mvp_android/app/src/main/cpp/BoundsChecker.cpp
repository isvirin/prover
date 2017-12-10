//
// Created by babay on 09.12.2017.
//

#include "BoundsChecker.h"

void BoundsChecker::SetDirection(int targetDirection) {
    _isDiagonal = targetDirection % 2 == 0;
    int turnToDirection = _isDiagonal ? 8 : 7;
    int directionDiff = (turnToDirection - targetDirection + 12) % 8 - 4;
    SetTurnMatForDirectionDiff(directionDiff);
}

void BoundsChecker::SetTurnMatForDirectionDiff(int directionDiff) {
    switch (directionDiff) {
        case 0:
            _turnMat[0][0] = _turnMat[1][1] = 1;
            _turnMat[1][0] = _turnMat[0][1] = 0;
            break;
        case -4:
            _turnMat[0][0] = _turnMat[1][1] = -1;
            _turnMat[1][0] = _turnMat[0][1] = 0;
            break;
        case 2:
            _turnMat[0][0] = _turnMat[1][1] = 0;
            _turnMat[1][0] = 1;
            _turnMat[0][1] = -1;
            break;
        case -2:
            _turnMat[0][0] = _turnMat[1][1] = 0;
            _turnMat[1][0] = -1;
            _turnMat[0][1] = 1;
            break;
    }
}

/**
 *
 * @param current -- current position vector
 * @return true if we're still OK
 */
bool BoundsChecker::CheckBounds(Vector current) {
    // turn _current so we should move to +x (of +x, +y for Diagonal)
    // fail if we've got too close to another swipe-point
    current.Mul(_turnMat);
    float x = current._x;
    float y = current._y;

    if (_isDiagonal) {
        if (x < -FIT_FACTOR_H || y < -FIT_FACTOR_H)
            return false;

        if (x + y <= 0)
            return true;

        if (y > x) {
            x = current._y;
            y = current._y = current._x;
            current._x = x;
        }
        //distance to line x = y -- it is the line that goes to target point
        float d1 = (x - y) / _sqrt2;
        // distance to remaining non-target swipe point
        float r2 = current.DistanceTo(1, 0);

        return d1 / FIT_FACTOR_D < r2 / (1 - FIT_FACTOR_D);
    } else {
        return x >= -FIT_FACTOR_H && fabsf(y) <= FIT_FACTOR_H;
    }
}
