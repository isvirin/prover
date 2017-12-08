//
// Created by babay on 08.12.2017.
//

#include "SwipeCircleDetector.h"
#include "common.h"

void SwipeCircleDetector::AddShift(VectorExplained shift) {
    shifts_[pos_] = shift;
    pos_ = ++pos_ % SHIFTS;
    ++total_;
    if (total_ > SHIFTS)
        total_ = SHIFTS;
}

bool SwipeCircleDetector::IsCircle() {
    Vector sum = shifts_[pos_];
    //Vector centerSum = sum;

    for (int i = 1; i <= total_; i++) {
        int pos = (pos_ - i + SHIFTS) % SHIFTS;
        sum += shifts_[pos];
        //centerSum += sum;
        if (sum._mod < 5 && i > 5) {
            float area = Area(pos);
            LOGI_NATIVE("detect2: vertices: %d, area: %f", i + 1, area);
            if (fabsf(area) < MIN_CIRCLE_AREA)
                return false;

            return true;
        }
    }

    return false;
}

float SwipeCircleDetector::Area(int amount) {
    Vector sum = shifts_[pos_];
    Vector sumPrev = sum;
    float area = 0;

    for (int i = 1; i <= amount; i++) {
        int pos = (pos_ - i + SHIFTS) % SHIFTS;
        sum += shifts_[pos];
        float triangleArea = (sum._x * sumPrev._y - sum._y * sumPrev._x) / 2;
        area += triangleArea;
    }
    return area;
}
