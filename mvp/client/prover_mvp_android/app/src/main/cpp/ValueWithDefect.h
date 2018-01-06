//
// Created by babay on 06.01.2018.
//

#ifndef PROVER_MVP_ANDROID_VALUEWITHDEFECT_H
#define PROVER_MVP_ANDROID_VALUEWITHDEFECT_H


#include <math.h>

class ValueWithDefect {
public:

    ValueWithDefect() {}

    ValueWithDefect(double value, float defect) : value(value), defect(defect) {}

    double value;
    float defect;

    inline ValueWithDefect operator/(ValueWithDefect divider) {
        ValueWithDefect res;
        res.value = value / divider.value;
        float t1 = (float) (defect / divider.value);
        float t2 = (float) (value * divider.defect / divider.value / divider.value);
        res.defect = sqrtf(t1 * t1 + t2 * t2);
        return res;
    }

    inline ValueWithDefect operator*(ValueWithDefect y) {
        ValueWithDefect res;
        res.value = value * y.value;
        float t1 = defect * (float) y.value;
        float t2 = y.defect * (float) value;
        res.defect = sqrtf(t1 * t1 + t2 * t2);
        return res;
    }

    inline void operator*=(ValueWithDefect y) {
        float t1 = defect * (float) y.value;
        float t2 = y.defect * (float) value;
        defect = sqrtf(t1 * t1 + t2 * t2);
        value *= y.value;
    }

    inline void operator/=(double div) {
        value /= div;
        defect /= div;
    }
};


#endif //PROVER_MVP_ANDROID_VALUEWITHDEFECT_H
