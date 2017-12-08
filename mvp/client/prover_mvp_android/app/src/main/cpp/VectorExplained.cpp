//
// Created by babay on 07.12.2017.
//

#include "VectorExplained.h"

void VectorExplained::Set(cv::Point2d other) {
    _x = (float) other.x;
    _y = (float) other.y;
    CalculateValues();
}

void VectorExplained::SetMul(cv::Point2d other, float mulX, float mulY) {
    _x = (float) other.x * mulX;
    _y = (float) other.y * mulY;
    CalculateValues();
}

void VectorExplained::Add(VectorExplained other) {
    if (other._mod > 0) {
        _x += other._x;
        _y += other._y;
        CalculateValues();
    }
}

void VectorExplained::CalculateValues() {
    _mod = (float) sqrt(_x * _x + _y * _y);
    if (_mod <= 0) {
        _angle = 0;
        return;
    }

    if (_x == 0) {
        _angle = _y > 0 ? 270 : 90;
    } else {
        float k = _y / _x;
        float atan_deg = (float) (fabs(atan((k)) * 180 / CV_PI));
        if (_x > 0 && _y >= 0) {
            _angle = 360 - atan_deg;
        } else if (_x < 0 && _y >= 0) {
            _angle = 180 + atan_deg;
        } else if (_x < 0) {
            _angle = 180 - atan_deg;
        } else {
            _angle = atan_deg;
        }
    }

    _direction = (int) (floor((360 - _angle - 22.5) / 45));
    if (_direction <= 0)
        _direction += 8;

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

void VectorExplained::SetLength(float length) {
    if (_mod > 0) {
        float mul = length / _mod;
        _x *= mul;
        _y *= mul;
        _mod = length;
    }
}

void VectorExplained::Reset() {
    _x = 0;
    _y = 0;
    _mod = 0;
    _angle = 0;
    _direction = 0;
}

void VectorExplained::operator*=(float mul) {
    _x *= mul;
    _y *= mul;
    _mod *= fabsf(mul);
    if (mul < 0) {
        _direction = _direction < 5 ? _direction + 4 : _direction - 4;
        _angle = _angle < 180 ? _angle + 180 : _angle - 180;
    }
}


