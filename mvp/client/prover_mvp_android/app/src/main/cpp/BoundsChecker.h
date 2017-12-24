//
// Created by babay on 09.12.2017.
//

#ifndef PROVER_MVP_ANDROID_BOUNDSCHECKER_H
#define PROVER_MVP_ANDROID_BOUNDSCHECKER_H


#include "Vector.h"

// fit-factor for SimplestBoundsCheck, in (0, 1)
// FIT_FACTOR_H is for horizontal and vertical movement;
// FIT_FACTOR_H is for diagonal

#define FIT_FACTOR_H_STRICT 0.55f
#define FIT_FACTOR_D_STRICT 0.53f

#define FIT_FACTOR_H_RELAXED 0.65f
#define FIT_FACTOR_D_RELAXED 0.63f

/**
 * Checks that we are closer to source or target swipe points (and a line between them) then to other swipe points
 * based on a simple Voronoi diagram
 *
 * rotates original vector to direction 7 or 8 to simplify calculations
 */

class BoundsChecker {
public:
    void SetDirection(int targetDirection);

    bool CheckBounds(Vector current);

    void setRelaxed(bool relaxed);

private:

    void SetTurnMatForDirectionDiff(int directionDiff);

    const double _sqrt2 = sqrtf(2.0f);

    bool _isDiagonal;
    double _turnMat[2][2];

    double _fitFactorHoriz = FIT_FACTOR_H_RELAXED;
    double _fitFactorDiag = FIT_FACTOR_D_RELAXED;
};


#endif //PROVER_MVP_ANDROID_BOUNDSCHECKER_H
