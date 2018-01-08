//
// Created by babay on 08.12.2017.
//

#include "SwipeCircleDetector.h"
#include "common.h"
#include "Perimeter.h"
#include "Area.h"
#include "ValueWithDefect.h"

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
    double minDeviation = 10;
    double minDeviationDefect = 0;

    for (int i = 2; i <= total_; i++) {
        pos = (pos_ - i + SHIFTS) % SHIFTS;
        if (shifts_[pos]._timestamp < noFramesBefore) {
            if (logLevel >= 1 && minDeviation < 0.5) {
                LOGI_NATIVE("IsCircle minDeviation: %.4f+-%.4f", minDeviation, minDeviationDefect);
            }
            return false;
        }

        sum.Add(shifts_[pos]);
        if (i > 5) {
            double dist = _relaxed ? sum._mod - sum.ModDefect() : sum._mod;
            if (dist < _maxDeviation) {
                ValueWithDefect perimeter;
                ValueWithDefect area = CalculateArea(i, perimeter);
                ValueWithDefect areaByP2ToCircle = area / (perimeter * perimeter);
                areaByP2ToCircle /= Circle_S_by_P2;

                LOGI_NATIVE(
                        "IsCircle %d vertices: %d, diff: %.4f-+%.4f (%.4f, %.4f) , area: %.4f+%.4f, areaByP2 to target: %.4f+%.4f",
                        timestamp, i + 1, sum._mod, sum.ModDefect(), sum._defectX, sum._defectY,
                        area.value, area.defect, areaByP2ToCircle.value, areaByP2ToCircle.defect);
                double areaVal = _relaxed ? area.value + area.defect : area.value;
                double aToPValue = _relaxed ? areaByP2ToCircle.value + areaByP2ToCircle.defect
                                            : areaByP2ToCircle.value;
                if (areaVal >= _minCircleArea && aToPValue >= _minAreaByP2toCircle)
                    return true;
            }
            if (sum._mod < minDeviation) {
                minDeviation = sum._mod;
                minDeviationDefect = sum.ModDefect();
            }
        }
    }
    return false;
}

ValueWithDefect SwipeCircleDetector::CalculateArea(int amount, ValueWithDefect &perResult) {
    Perimeter perimeter;
    Area area(shifts_[pos_]);

    for (int i = 2; i <= amount; i++) {
        int pos = (pos_ - i + SHIFTS) % SHIFTS;
        perimeter.Add(shifts_[pos]);
        area.AppendVector(shifts_[pos]);
    }
    VectorExplained last(area.sum._x - shifts_[pos_]._x, area.sum._y - shifts_[pos_]._y);
    last.CalculateMod();
    perimeter.Add(last);
    area.AppendVector(last);

    perResult.value = perimeter._perimeter;
    perResult.defect = (float) perimeter.GetDefect();
    return ValueWithDefect(fabs(area._area), (float) area.GetDefect());
}

void SwipeCircleDetector::SetRelaxed(bool relaxed) {
    _relaxed = relaxed;
    _minCircleArea = MIN_CIRCLE_AREA;
    _maxDeviation = MAX_DEVIATION;
    _minAreaByP2toCircle = MIN_AREA_BY_P2_TO_CIRCLE;
}
