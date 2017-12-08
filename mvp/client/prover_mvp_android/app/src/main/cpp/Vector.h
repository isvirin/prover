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

    inline float distanceTo(Vector other) {
        float dx = other._x - _x;
        float dy = other._y - _y;
        return sqrtf(dx * dx + dy * dy);
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
