//
// Created by babay on 07.12.2017.
//

#ifndef PROVER_MVP_ANDROID_VECTOREXPLAINED_H
#define PROVER_MVP_ANDROID_VECTOREXPLAINED_H

#include <opencv2/opencv.hpp>

class VectorExplained {
public:
    VectorExplained() {};

    VectorExplained(float x, float y) : _x(x), _y(y) {
        CalculateValues();
    };

    void Set(cv::Point2d other);

    void SetMul(cv::Point2d other, float mulX, float mulY);

    virtual void Add(VectorExplained other);

    void SetLength(float length);

    virtual void Reset();

    void operator*=(float mul);

    float _x = 0;
    float _y = 0;
    float _mod = 0;
    float _angle = 0;
    /**
     * 1 -- down, 3 -- left, 5 -- top, 7 -- right, 8 -- bottom-right
     */
    int _direction = 0;

protected:
    void CalculateValues();
};


#endif //PROVER_MVP_ANDROID_VECTOREXPLAINED_H
