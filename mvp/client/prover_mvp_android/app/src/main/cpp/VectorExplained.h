//
// Created by babay on 07.12.2017.
//

#ifndef PROVER_MVP_ANDROID_VECTOREXPLAINED_H
#define PROVER_MVP_ANDROID_VECTOREXPLAINED_H

#include <opencv2/opencv.hpp>
#include "Vector.h"
#include "common.h"

class VectorExplained : public Vector {
public:
    VectorExplained() {};

    VectorExplained(double x, double y) : Vector(x, y) {
        CalculateExplained();
    };

    void Set(cv::Point2d other);

    void Set(double x, double y) {
        _x = x;
        _y = y;
        CalculateExplained();
    }

    void SetMul(cv::Point2d other, double mulX, double mulY);

    void ApplyWindow(double windowStart, double windowEnd);

    virtual void Add(VectorExplained other);

    inline void Reset();

    void SetLength(double length);

    void operator*=(double mul);

    void AttractTo(Vector other, double force);

    inline int DirectionDiff(VectorExplained other) {
        return (_direction - other._direction + 12) % 8 - 4;
    }

    void Log();

    /**
     * calculates angle to another vector, result in [-180, 180]
     * @param other
     * @return
     */
    inline double AngleTo(VectorExplained other) {
        return fmod(other._angle - _angle + 540.0, 360.0) - 180.0;
    }

    void setRelativeDefect(double relativeDefect) {
        _defectX = fabs(_x * relativeDefect);
        _defectY = fabs(_y * relativeDefect);
    }

    double ModDefect() {
        if (_mod == 0)
            return 0;

        return sqrt(_x * _x * _defectX2sum + _y * _y * _defectY2sum) / _mod;
    }

    double MinDistanceToWithDefect(Vector other) {
        double dx = fabs(other._x - _x);
        double dy = fabs(other._y - _y);
        dx = dx < _defectX ? 0.0 : dx - _defectX;
        dy = dy < _defectY ? 0.0 : dy - _defectY;
        return sqrt(dx * dx + dy * dy);
    }

    double _angle = 0;
    /**
     * 1 -- down, 3 -- left, 5 -- top, 7 -- right, 8 -- bottom-right
     */
    int _direction = 0;

    double _defectX = 0;
    double _defectY = 0;
    double _defectX2sum = 0;
    double _defectY2sum = 0;

private:
    void CalculateExplained();
};

inline void VectorExplained::Reset() {
    _x = 0;
    _y = 0;
    _mod = 0;
    _angle = 0;
    _direction = 0;
    _defectX = 0;
    _defectY = 0;
    _defectX2sum = 0;
    _defectY2sum = 0;
}


#endif //PROVER_MVP_ANDROID_VECTOREXPLAINED_H
