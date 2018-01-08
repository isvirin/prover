//
// Created by babay on 07.12.2017.
//

#include "VectorExplained.h"
#include "common.h"

void VectorExplained::Set(cv::Point2d other) {
    _x = (float) other.x;
    _y = (float) other.y;
    CalculateExplained();
}

void VectorExplained::SetMul(cv::Point2d other, double mulX, double mulY) {
    _x = (float) other.x * mulX;
    _y = (float) other.y * mulY;
    CalculateExplained();
}

void VectorExplained::Add(VectorExplained other) {
    if (other._mod > 0) {
        (*this) += other;
        CalculateExplained();
        _defectX2sum += other._defectX * other._defectX;
        _defectY2sum += other._defectY * other._defectY;
        _defectX = sqrtf((float) _defectX2sum);
        _defectY = sqrtf((float) _defectY2sum);
    }
}

void VectorExplained::CalculateExplained() {
    CalculateMod();
    if (_mod <= 0) {
        _angle = 0;
        return;
    }

    if (_x == 0) {
        _angle = _y > 0 ? 270 : 90;
    } else {
        double k = -_y / _x;
        _angle = atan(k) * 180 / CV_PI;
        if (_x < 0)
            _angle += 180.0f;
        _angle = fmod(_angle + 360.0, 360.0);
    }

    _direction = (int) (floor((360 - _angle - 22.5) / 45));
    _direction = (_direction + 7) % 8 + 1;

    /*
     if (((_angle >= 337) && (_angle <= 360)) ||
        ((_angle >= 0) && (_angle < 22.5)))
        _direction = 7;
    if ((_angle >= 22.5) && (_angle < 67.5)) _direction = 8;
    if ((_angle >= 67.5) && (_angle < 112.5)) _direction = 1;
    if ((_angle >= 112.5) && (_angle < 157.5)) _direction = 2;
    if ((_angle >= 157.5) && (_angle < 202.5)) _direction = 3;
    if ((_angle >= 202.5) && (_angle < 247.5)) _direction = 4;
    if ((_angle >= 247.5) && (_angle < 292.5)) _direction = 5;
    if ((_angle >= 292.5) && (_angle < 337.5)) _direction = 6;
     */
}

void VectorExplained::operator*=(double mul) {
    _x *= mul;
    _y *= mul;
    _mod *= fabs(mul);
    if (mul < 0) {
        _direction = _direction < 5 ? _direction + 4 : _direction - 4;
        _angle = _angle < 180 ? _angle + 180 : _angle - 180;
    }
}

void VectorExplained::SetLength(double length) {
    if (_mod > 0) {
        double mul = length / _mod;
        _x *= mul;
        _y *= mul;
        _mod = fabs(length);
    }
}

void VectorExplained::AttractTo(Vector other, double force) {
    double mod = _mod;
    Vector::Add(other._x * force, other._y * force);
    _mod = Length();
    SetLength(mod);
}

void VectorExplained::Log() {
    LOGI_NATIVE("VectorExplained (%f,%f) angle %f, mod %f ", _x, _y, _angle, _mod);
}

void VectorExplained::ApplyWindow(double windowStart, double windowEnd) {
    if (_mod <= windowStart) {
        Reset();
    } else if (_mod < windowEnd) {
        double arg = (_mod - windowStart) / (windowEnd - windowStart);
        *this *= arg;
    }
}

bool VectorExplained::CheckWithinRectWithDefect(float left, float top, float right, float bottom) {
    if (_x >= left && _x <= right) {
        return top - _defectY <= _y && _y <= bottom + _defectY;
    } else if (_y >= top && _y <= bottom)
        return left - _defectX <= _x && _x <= right + _defectX;

    float cx = (left + right) / 2;
    float cy = (top + bottom) / 2;

    // coordinate center to rect's center; reflect to positive
    Vector v = Vector(fabs(_x - cx), fabs(_y - cy));
    // coordinate center to rect's right-bottom corner
    v._x -= (right - cx);
    v._y -= (bottom - cy);
    Vector shifted = v.EllipticalShiftMagnet(_defectX, _defectY, 0, 0);
    // if (0,0) within defect area then vector becomes shifted to 0,0
    return shifted._x == 0 && shifted._y == 0;
}

void VectorExplained::SetDirection(int direction) {
    _direction = direction;
    --direction;
    if (direction % 4 == 0)
        _x = 0;
    else
        _x = direction / 4 == 0 ? -1 : 1;

    direction = (direction + 6) % 8;
    if (direction % 4 == 0)
        _y = 0;
    else
        _y = direction / 4 == 0 ? -1 : 1;
}

void VectorExplained::SetSwipePoints(int from, int to) {
    --from;
    --to;
    int sourceX = from % 3;
    int sourceY = from / 3;
    int targetX = to % 3;
    int targetY = to / 3;
    _x = targetX - sourceX;
    _y = targetY - sourceY;
    CalculateExplained();
}

