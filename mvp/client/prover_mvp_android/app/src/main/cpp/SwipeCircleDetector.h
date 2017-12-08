//
// Created by babay on 08.12.2017.
//

#ifndef PROVER_MVP_ANDROID_SWIPECIRCLEDETECTOR_H
#define PROVER_MVP_ANDROID_SWIPECIRCLEDETECTOR_H


#include "VectorExplained.h"

#define SHIFTS 64
#define MIN_CIRCLE_AREA 40

class SwipeCircleDetector {
public:
    void AddShift(VectorExplained shift);

    bool IsCircle();

    void Reset() {
        pos_ = 0;
        total_ = 0;
    }

private:
    float Area(int amount);

    VectorExplained shifts_[SHIFTS];
    int pos_ = 0;
    int total_ = 0;
};


#endif //PROVER_MVP_ANDROID_SWIPECIRCLEDETECTOR_H
