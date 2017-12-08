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

    VectorExplained(float x, float y) : Vector(x, y) {
        CalculateAngle();
    };

    void Set(cv::Point2d other);

    void SetMul(cv::Point2d other, float mulX, float mulY);

    virtual void Add(VectorExplained other);

    virtual void Reset();

    void operator*=(float mul);

    float _angle = 0;
    /**
     * 1 -- down, 3 -- left, 5 -- top, 7 -- right, 8 -- bottom-right
     */
    int _direction = 0;

private:
    void CalculateAngle();
};


#endif //PROVER_MVP_ANDROID_VECTOREXPLAINED_H
