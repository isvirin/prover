//
// Created by babay on 08.12.2017.
//

#ifndef PROVER_MVP_ANDROID_SWIPECIRCLEDETECTOR_H
#define PROVER_MVP_ANDROID_SWIPECIRCLEDETECTOR_H


#include "VectorExplained.h"

#define SHIFTS 64
#define MIN_CIRCLE_AREA 2000
#define MAX_CIRCLE_DURATION_MS 3000

class SwipeCircleDetector {
public:
    void AddShift(VectorExplained shift);

    bool IsCircle();

    void Reset() {
        pos_ = 0;
        total_ = 0;
    }

    const float Circle_S_by_P2 = (const float) (0.25f / CV_PI);

private:
    float Area(int amount, float &perimeter);

    VectorExplained shifts_[SHIFTS];
    int pos_ = 0;
    int total_ = 0;
};


#endif //PROVER_MVP_ANDROID_SWIPECIRCLEDETECTOR_H
