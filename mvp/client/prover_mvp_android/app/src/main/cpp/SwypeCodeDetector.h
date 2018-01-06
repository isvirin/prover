//
// Created by babay on 06.01.2018.
//

#ifndef PROVER_MVP_ANDROID_SWYPECODEDETECTOR_H
#define PROVER_MVP_ANDROID_SWYPECODEDETECTOR_H


#include "SwypeStepDetector.h"
#include "SwipeCode.h"

class SwypeCodeDetector {
public:
    SwypeCodeDetector() {};

    SwypeCodeDetector(SwipeCode code, double speedMult, float maxDeviation,
                      bool relaxed, unsigned int timestamp);

    void Init(SwipeCode &code, double speedMult, float maxDeviation, bool relaxed,
              unsigned int timestamp);

    /**
     * @param shift
     * @return  1 -- swipe code completed
     *         -1 -- swipe code failed
     *         -2 -- swipe input timeout
     *          0 -- processing swipe code
     */
    int Add(VectorExplained &shift);

    void FillResult(int &index, int &x, int &y, int &debug);

private:
    SwipeCode _code;
    SwypeStepDetector _stepDetector;

    unsigned int _maxTimestamp = 0;

    unsigned int _currentStep = 0;

    bool _relaxed = true;
};


#endif //PROVER_MVP_ANDROID_SWYPECODEDETECTOR_H