//
// Created by babay on 08.12.2017.
//

#ifndef PROVER_MVP_ANDROID_VECTOR_H
#define PROVER_MVP_ANDROID_VECTOR_H


#include <cmath>

class Vector {
public:

    Vector() {}

    Vector(float _x, float _y) : _x(_x), _y(_y) {
        _mod = sqrtf(_x * _x + _y * _y);
    }

    inline void operator+=(Vector other) {
        _x += other._x;
        _y += other._y;
        _mod = sqrtf(_x * _x + _y * _y);
    };

    void SetLength(float length);

    float _x = 0;
    float _y = 0;
    float _mod = 0;

protected:
    inline void Add(float x, float y) {
        _x += x;
        _y += y;
        _mod = sqrtf(_x * _x + _y * _y);
    }
};


#endif //PROVER_MVP_ANDROID_VECTOR_H
