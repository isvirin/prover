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
    void Configure(double speedMult, float maxDeviation, bool relaxed);

    /**
     * configures to start movement in specified direction
     * @param dir
     */
    void SetDirection(int dir);

    /**
     * set new direction, taking in account current position and previous targer shift vector
     * @param dir
     */
    void AdvanceDirection(int dir);

    void SetTarget(VectorExplained target);

    void FinishStep();

    /**
     * checks current state;
     * @return
     *         1 if we've reached target point
     *         -1 if we've failed to reach point
     *         0 if we're still in progress reaching target point
     */
    int CheckState(bool withDefect);

    VectorExplained _current;

private:
    int _count = 0;

    double _speedMultX = 0;
    double _speedMultY = 0;
    float _defaultTargetRadius = 0;
    float _targetRadius = 0;

    BoundsChecker _BoundsChecker;
    VectorExplained _total;
    VectorExplained _target;
    bool _relaxed;
    unsigned int _id;
};


#endif //PROVER_MVP_ANDROID_SWYPESTEPDETECTOR_H
