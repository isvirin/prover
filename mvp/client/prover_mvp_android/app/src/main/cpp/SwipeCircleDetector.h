//
// Created by babay on 08.12.2017.
//

#ifndef PROVER_MVP_ANDROID_SWIPECIRCLEDETECTOR_H
#define PROVER_MVP_ANDROID_SWIPECIRCLEDETECTOR_H


#include "VectorExplained.h"
#include "ValueWithDefect.h"

#define SHIFTS 64

#define MAX_CIRCLE_DURATION_MS 1500

#define MIN_CIRCLE_AREA 0.14
#define MAX_DEVIATION 0.09
#define MIN_AREA_BY_P2_TO_CIRCLE 0.75

class SwipeCircleDetector {
public:
    void AddShift(VectorExplained shift);

    bool IsCircle();

    void Reset() {
        pos_ = 0;
        total_ = 0;
    }

    const double Circle_S_by_P2 = 0.25 / CV_PI;

    void SetRelaxed(bool relaxed);

private:
    ValueWithDefect CalculateArea(int amount, ValueWithDefect &perResult);

    VectorExplained shifts_[SHIFTS];
    int pos_ = 0;
    int total_ = 0;

    double _minCircleArea = MIN_CIRCLE_AREA;
    double _maxDeviation = MAX_DEVIATION;
    double _minAreaByP2toCircle = MIN_AREA_BY_P2_TO_CIRCLE;
    bool _relaxed = true;
};


#endif //PROVER_MVP_ANDROID_SWIPECIRCLEDETECTOR_H
