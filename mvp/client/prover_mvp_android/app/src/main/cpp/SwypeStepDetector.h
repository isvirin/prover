//
// Created by babay on 07.12.2017.
//

#ifndef PROVER_MVP_ANDROID_SWYPESTEPDETECTOR_H
#define PROVER_MVP_ANDROID_SWYPESTEPDETECTOR_H

#include "VectorExplained.h"

//#define REQUIRE_REACH_BOUNDS

/**
 * coordinate convention:
 * (0,0) is at current swipe-point; 1 is a distance between two swipe-points (non diagonal).
 * X axis directed from left to right
 * Y axis directed from top to bottom
 *
 */
class SwypeStepDetector : public VectorExplained {
public:

    virtual void Add(VectorExplained other);

    virtual void Reset();

    /**
     * configures detector for specific width, height and speed multiplier
     * @param width
     * @param height
     * @param speedMult
     */
    void Configure(int width, int height, float speedMult, float maxDeviation);

    /**
     * configures for movement between adjacent swype points currentPoint to nextPoint
     * @param currentPoint -- current point number, 1-based
     * @param nextPoint -- next point number; 1-based
     * @return true if configured OK; fails if points are not adjacent
     */
    bool SetSwipeStep(int currentPoint, int nextPoint);

    bool AdvanceSwipeStep(int nextPoint);

    /**
     * checks current state;
     * @return
     *         1 if we've reached target point
     *         -1 if we've failed to reach point
     *         0 if we're still in progress reaching target point
     */
    int CheckState();

private:
    int _count = 0;
    float _targetX = 0;
    float _targetY = 0;
    float _speedMultX = 0;
    float _speedMultY = 0;
    float _maxDeviation = 0;
    float _targetRadius = 0;

    int _targetDirection = 0;
    int _currentSwypePoint = 0;
    int _nextSwypePoint = 0;
    bool _isDiagonal;

    float _sqrt2 = sqrtf(2.0f);

    /**
     * if we move to a totally different direction (differs from target direction for more then 2)
     * then we can move not further then this value
     */
    const float _badDirectionMaxRadius = 0.3f;

    bool SetNextSwipePoint(int nextPoint);
};


#endif //PROVER_MVP_ANDROID_SWYPESTEPDETECTOR_H
