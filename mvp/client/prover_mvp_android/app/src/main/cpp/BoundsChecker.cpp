//
// Created by babay on 09.12.2017.
//

#include "BoundsChecker.h"
#include "common.h"

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
    double x = current._x;
    double y = current._y;

    if (_isDiagonal) {
        if (x < -_fitFactorHoriz || y < -_fitFactorHoriz) {
            if (logLevel > 0) {
                LOGI_NATIVE("Bounds_f1 %.4f, %.4f", x, y);
            }
            return false;
        }

        if (x + y <= _fitFactorSum) {
            if (logLevel > 0) {
                LOGI_NATIVE("Bounds_f2 %.4f, %.4f", x, y);
            }
            return true;
        }

        if (y > x) {
            x = current._y;
            y = current._y = current._x;
            current._x = x;
        }
        //distance to line x = y -- it is the line that goes to target point
        double d1 = (x - y) / _sqrt2;
        // distance to remaining non-target swipe point
        double r2 = current.DistanceTo(1, 0);
        if (logLevel == 0)
            return d1 / _fitFactorDiag < r2 / (1 - _fitFactorDiag);
        else {
            if (d1 / _fitFactorDiag < r2 / (1 - _fitFactorDiag))
                return true;
            if (logLevel > 0) {
                LOGI_NATIVE("Bounds_f3 %.4f, %.4f, %f, %f", x, y, d1 / _fitFactorDiag,
                            r2 / (1 - _fitFactorDiag));
            }
            return false;
        }
    } else {
        return x >= -_fitFactorHoriz && fabs(y) <= _fitFactorHoriz;
    }
}

void BoundsChecker::setRelaxed(bool relaxed) {
    if (relaxed) {
        _fitFactorHoriz = FIT_FACTOR_H_RELAXED;
        _fitFactorDiag = FIT_FACTOR_D_RELAXED;
        _fitFactorSum = -0.15;
    } else {
        _fitFactorHoriz = FIT_FACTOR_H_STRICT;
        _fitFactorDiag = FIT_FACTOR_D_STRICT;
        _fitFactorSum = 0;
    }
}
