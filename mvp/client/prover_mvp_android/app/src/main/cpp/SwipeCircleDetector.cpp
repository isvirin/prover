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
    int pos = (pos_ - 1 + SHIFTS) % SHIFTS;
    VectorExplained sum = shifts_[pos];

    uint noFramesBefore = shifts_[pos]._timestamp - MAX_CIRCLE_DURATION_MS;
    int timestamp = shifts_[pos]._timestamp;

    for (int i = 2; i <= total_; i++) {
        int pos = (pos_ - i + SHIFTS) % SHIFTS;
        if (shifts_[pos]._timestamp < noFramesBefore) {
            return false;
        }

        sum.Add(shifts_[pos]);

        if (sum._mod < MAX_DEVIATION && i > 5) {
            float perimeter;
            float area = fabsf(Area(i, perimeter));
            float areaByP2 = area / perimeter / perimeter;
            LOGI_NATIVE("detect2: %d vertices: %d, area: %f, areaByP2 to target: %f", timestamp,
                        i + 1, area, areaByP2 / Circle_S_by_P2);
            return !(fabsf(area) < MIN_CIRCLE_AREA || areaByP2 < Circle_S_by_P2 / 1.5f);
        }
    }

    return false;
}

float SwipeCircleDetector::Area(int amount, float &perimeter) {
    Vector sum = shifts_[pos_];
    Vector sumPrev = sum;
    perimeter = shifts_[pos_]._mod;
    float area = 0;
    //shifts_[pos_].Log();

    for (int i = 2; i <= amount; i++) {
        int pos = (pos_ - i + SHIFTS) % SHIFTS;
        sum += shifts_[pos];
        perimeter += shifts_[pos]._mod;
        float triangleArea = (sum._x * sumPrev._y - sum._y * sumPrev._x) / 2;
        area += triangleArea;
        sumPrev = sum;
        //shifts_[pos].Log();
    }
    return area;
}
