//
// Created by babay on 06.01.2018.
//

#ifndef PROVER_MVP_ANDROID_AREA_H
#define PROVER_MVP_ANDROID_AREA_H


#include "VectorExplained.h"

class Area {
public:
    Area(const VectorExplained &v) : sum(v), sumPrev(v) {}

    inline void AppendVector(VectorExplained &v) {
        sum.Add(v);
        float triangleArea = (float) (sum._x * sumPrev._y - sum._y * sumPrev._x) / 2;
        _area += triangleArea;

        double t1 = (sum._x * sumPrev._defectY);
        double t2 = (sum._defectX * sumPrev._y);
        double t3 = (sum._y * sumPrev._defectX);
        double t4 = (sum._defectY * sumPrev._x);

        _defect2 += t1 * t1 + t2 * t2 + t3 * t3 + t4 * t4;
        sumPrev = sum;
    }

    double _area = 0;

    inline double GetDefect() {
        return sqrt(_defect2);
    }

    VectorExplained sum;

private:

    VectorExplained sumPrev;
    double _defect2 = 0;
};


#endif //PROVER_MVP_ANDROID_AREA_H
