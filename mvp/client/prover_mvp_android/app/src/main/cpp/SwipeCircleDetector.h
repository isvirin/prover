//
// Created by babay on 08.12.2017.
//

#ifndef PROVER_MVP_ANDROID_SWIPECIRCLEDETECTOR_H
#define PROVER_MVP_ANDROID_SWIPECIRCLEDETECTOR_H


#include "VectorExplained.h"

#define SHIFTS 64

#define MAX_CIRCLE_DURATION_MS 2500

#define MIN_CIRCLE_AREA 0.14
#define MAX_DEVIATION 0.09
#define MIN_AREA_BY_P2_TO_CIRCLE 0.67

#define MIN_CIRCLE_AREA_RELAXED 0.128
#define MAX_DEVIATION_RELAXED 0.11
#define MIN_AREA_BY_P2_TO_CIRCLE_RELAXED 0.6


class SwipeCircleDetector {
public:
    void AddShift(VectorExplained shift);

    bool IsCircle();

    void Reset() {
        pos_ = 0;
        total_ = 0;
    }

    const double Circle_S_by_P2 = 0.25 / CV_PI;

    void setTolerance(double tolerance);

private:
    double Area(int amount, double &perimeter);

    VectorExplained shifts_[SHIFTS];
    int pos_ = 0;
    int total_ = 0;

    double _minCircleArea = MIN_CIRCLE_AREA_RELAXED;
    double _maxDeviation = MAX_DEVIATION_RELAXED;
    double _minAreaByP2toCircle = MIN_AREA_BY_P2_TO_CIRCLE_RELAXED;
};


#endif //PROVER_MVP_ANDROID_SWIPECIRCLEDETECTOR_H
