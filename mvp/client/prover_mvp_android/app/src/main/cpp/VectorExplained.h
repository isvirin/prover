//
// Created by babay on 07.12.2017.
//

#ifndef PROVER_MVP_ANDROID_VECTOREXPLAINED_H
#define PROVER_MVP_ANDROID_VECTOREXPLAINED_H

#include <opencv2/opencv.hpp>
#include "Vector.h"

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
        return fmod(other._angle - _angle + 540.0f, 360.0f) - 180.0f;
    }

    double _mod = 0;

    double _angle = 0;
    /**
     * 1 -- down, 3 -- left, 5 -- top, 7 -- right, 8 -- bottom-right
     */
    int _direction = 0;

    uint _timestamp = 0;

private:
    void CalculateExplained();
};

inline void VectorExplained::Reset() {
    _x = 0;
    _y = 0;
    _mod = 0;
    _angle = 0;
    _direction = 0;
}

#endif //PROVER_MVP_ANDROID_VECTOREXPLAINED_H
