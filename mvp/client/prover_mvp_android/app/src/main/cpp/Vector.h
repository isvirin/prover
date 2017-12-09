//
// Created by babay on 08.12.2017.
//

#ifndef PROVER_MVP_ANDROID_VECTOR_H
#define PROVER_MVP_ANDROID_VECTOR_H


#include <cmath>

class Vector {
public:

    Vector() {}

    Vector(float _x, float _y) : _x(_x), _y(_y) {}

    inline void operator+=(Vector other) {
        _x += other._x;
        _y += other._y;
    };

    inline Vector operator+(Vector other) {
        return Vector(_x + other._x, _y + other._y);
    }

    inline float operator*(Vector other) {
        return _x * other._x + _y * other._y;
    }

    inline void operator-=(Vector other) {
        _x -= other._x;
        _y -= other._y;
    }

    inline float Length() {
        return sqrtf(_x * _x + _y * _y);
    }

    inline float DistanceTo(Vector other) {
        float dx = other._x - _x;
        float dy = other._y - _y;
        return sqrtf(dx * dx + dy * dy);
    }

    inline float DistanceTo(float x, float y) {
        float dx = x - _x;
        float dy = y - _y;
        return sqrtf(dx * dx + dy * dy);
    }

    inline void Mul(float mat[2][2]) {
        float x = mat[0][0] * _x + mat[0][1] * _y;
        _y = mat[1][0] * _x + mat[1][1] * _y;
        _x = x;
    }

    float _x = 0;
    float _y = 0;

protected:
    inline void Add(float x, float y) {
        _x += x;
        _y += y;
    }
};


#endif //PROVER_MVP_ANDROID_VECTOR_H
