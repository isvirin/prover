//
// Created by babay on 08.12.2017.
//

#include "SwipeCircleDetector.h"
#include "common.h"

void SwipeCircleDetector::AddShift(VectorExplained shift) {
    shifts_[pos_] = shift;
    pos_ = (pos_ + 1) % SHIFTS;
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
        pos = (pos_ - i + SHIFTS) % SHIFTS;
        if (shifts_[pos]._timestamp < noFramesBefore) {
            return false;
        }

        sum.Add(shifts_[pos]);
        if (sum._mod < _maxDeviation && i > 5) {
            double perimeter;
            double area = fabs(Area(i, perimeter));
            double areaByP2 = area / perimeter / perimeter;
            double areaByP2ToCircle = areaByP2 / Circle_S_by_P2;
            LOGI_NATIVE("IsCircle %d vertices: %d, diff: %f, area: %f, areaByP2 to target: %f",
                        timestamp, i + 1, sum._mod, area, areaByP2 / Circle_S_by_P2);
            if (fabs(area) >= _minCircleArea && areaByP2ToCircle >= _minAreaByP2toCircle)
                return true;
        }
    }
    return false;
}

double SwipeCircleDetector::Area(int amount, double &perimeter) {
    Vector sum = shifts_[pos_];
    Vector sumPrev = sum;
    perimeter = shifts_[pos_]._mod;
    float area = 0;

    for (int i = 2; i <= amount; i++) {
        int pos = (pos_ - i + SHIFTS) % SHIFTS;
        sum += shifts_[pos];
        perimeter += shifts_[pos]._mod;
        double triangleArea = (sum._x * sumPrev._y - sum._y * sumPrev._x) / 2;
        area += triangleArea;
        sumPrev = sum;
    }
    return area;
}

void SwipeCircleDetector::setRelaxed(bool relaxed) {
    if (relaxed) {
        _minCircleArea = MIN_CIRCLE_AREA_RELAXED;
        _maxDeviation = MAX_DEVIATION_RELAXED;
        _minAreaByP2toCircle = MIN_AREA_BY_P2_TO_CIRCLE_RELAXED;
    } else {
        _minCircleArea = MIN_CIRCLE_AREA_STRICT;
        _maxDeviation = MAX_DEVIATION_STRICT;
        _minAreaByP2toCircle = MIN_AREA_BY_P2_TO_CIRCLE_STRICT;
    }
}
