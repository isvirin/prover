//
// Created by babay on 07.12.2017.
//

#include "VectorExplained.h"

void VectorExplained::Set(cv::Point2d other) {
    _x = (float) other.x;
    _y = (float) other.y;
    _mod = sqrtf(_x * _x + _y * _y);
    CalculateAngle();
}

void VectorExplained::SetMul(cv::Point2d other, float mulX, float mulY) {
    _x = (float) other.x * mulX;
    _y = (float) other.y * mulY;
    _mod = sqrtf(_x * _x + _y * _y);
    CalculateAngle();
}

void VectorExplained::Add(VectorExplained other) {
    if (other._mod > 0) {
        (*this) += other;
        CalculateAngle();
    }
}

void VectorExplained::CalculateAngle() {
    if (_mod <= 0) {
        _angle = 0;
        return;
    }

    if (_x == 0) {
        _angle = _y > 0 ? 270 : 90;
    } else {
        float k = -_y / _x;
        _angle = (float) (atan(k) * 180 / CV_PI);
        if (_x < 0)
            _angle += 180.0f;
        _angle = fmodf(_angle + 360.0f, 360.0f);
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


