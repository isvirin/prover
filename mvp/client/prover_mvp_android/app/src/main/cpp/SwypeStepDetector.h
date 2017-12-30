//
// Created by babay on 07.12.2017.
//

#ifndef PROVER_MVP_ANDROID_SWYPESTEPDETECTOR_H
#define PROVER_MVP_ANDROID_SWYPESTEPDETECTOR_H

#include "VectorExplained.h"
#include "BoundsChecker.h"

/**
 * coordinate convention:
 * (0,0) is at current swipe-point; 1 is a distance between two swipe-points (non diagonal).
 * X axis directed from left to right
 * Y axis directed from top to bottom
 *
 */

class SwypeStepDetector {
public:

    virtual void Add(VectorExplained);

    void Reset();

    /**
     * configures detector for specific width, height and speed multiplier
     * @param width
     * @param height
     * @param speedMult
     */
    void Configure(double speedMult, float maxDeviation);

    /**
     * configures for movement between adjacent swype points currentPoint to nextPoint
     * @param currentPoint -- current point number, 1-based
     * @param nextPoint -- next point number; 1-based
     * @return true if configured OK; fails if points are not adjacent
     */
    bool SetSwipeStep(int currentPoint, int nextPoint);

    bool AdvanceSwipeStep(int nextPoint);

    void FinishStep();

    /**
     * checks current state;
     * @return
     *         1 if we've reached target point
     *         -1 if we've failed to reach point
     *         0 if we're still in progress reaching target point
     */
    int CheckState(bool withDefect);

    VectorExplained _target;
    VectorExplained _current;

private:
    bool SetNextSwipePoint(int nextPoint);

    int _count = 0;

    double _speedMultX = 0;
    double _speedMultY = 0;
    float _targetRadius = 0;

    int _currentSwypePoint = 0;
    int _nextSwypePoint = 0;

    BoundsChecker _BoundsChecker;
    VectorExplained _total;
};


#endif //PROVER_MVP_ANDROID_SWYPESTEPDETECTOR_H
