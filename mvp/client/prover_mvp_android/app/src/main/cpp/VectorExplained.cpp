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
    }
}

void VectorExplained::CalculateExplained() {
    _mod = Length();
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
    LOGI_NATIVE("Detect2 vector (%f,%f) angle %f, mod %f ", _x, _y, _angle, _mod);
}

void VectorExplained::ApplyWindow(double windowStart, double windowEnd) {
    if (_mod <= windowStart) {
        Reset();
    } else if (_mod < windowEnd) {
        double arg = (_mod - windowStart) / (windowEnd - windowStart) * 0.5 * CV_PI;
        double window = 0.5 - 0.5 * cos(arg);
        *this *= window;
    }
}

