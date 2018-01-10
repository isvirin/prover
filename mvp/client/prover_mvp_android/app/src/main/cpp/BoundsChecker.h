//
// Created by babay on 09.12.2017.
//

#ifndef PROVER_MVP_ANDROID_BOUNDSCHECKER_H
#define PROVER_MVP_ANDROID_BOUNDSCHECKER_H


#include "Vector.h"
#include "VectorExplained.h"

// fit-factor for SimplestBoundsCheck, in (0, 1)
// FIT_FACTOR_H is for horizontal and vertical movement;
// FIT_FACTOR_H is for diagonal

#define FIT_FACTOR_H 0.45f
#define FIT_FACTOR_H_RELAXED 0.6f

/**
 * Checks that we are closer to source or target swype points (and a line between them) then to other swipe points
 * based on a simple Voronoi diagram
 *
 * rotates original vector to direction 7 or 8 to simplify calculations
 */

class BoundsChecker {
public:
    void SetDirection(int targetDirection);

    bool CheckBounds(VectorExplained p);

    bool CheckBoundsWithDefect(VectorExplained p);

    void SetTargetRadius(float _targetRadius, float targetRadiusOther);

private:

    void SetTurnMatForDirectionDiff(int directionDiff);

    void SetMatForDirection(int direction);

    const double _sqrt2 = sqrt(2.0f);

    bool _isDiagonal;
    double _turnMat[2][2];

    float _targetRadius;
    // target radius for non-target points
    float _targetRadiusOther;
};


#endif //PROVER_MVP_ANDROID_BOUNDSCHECKER_H
