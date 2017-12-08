//
// Created by babay on 08.12.2017.
//

#include "Vector.h"


void Vector::SetLength(float length) {
    if (_mod > 0) {
        float mul = length / _mod;
        _x *= mul;
        _y *= mul;
        _mod = fabsf(length);
    }
}
