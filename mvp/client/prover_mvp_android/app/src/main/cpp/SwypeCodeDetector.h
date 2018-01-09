//
// Created by babay on 06.01.2018.
//

#ifndef PROVER_MVP_ANDROID_SWYPECODEDETECTOR_H
#define PROVER_MVP_ANDROID_SWYPECODEDETECTOR_H


#include "SwypeStepDetector.h"
#include "SwipeCode.h"

class SwypeCodeDetector {
public:
    SwypeCodeDetector() : _id(++counter), _stepDetector(_id) {};

    SwypeCodeDetector(SwipeCode &code, double speedMult, float maxDeviation,
                      bool relaxed, unsigned int timestamp);

    void Init(SwipeCode &code, double speedMult, float maxDeviation, bool relaxed,
              unsigned int timestamp);

    /**
     * @param shift
     */
    void Add(VectorExplained &shift);

    void FillResult(int &status, int &index, int &x, int &y, int &debug);

    /*
     *    1 -- swipe code completed
     *    0 -- processing swipe code
     *    2 -- waiting to start swipe code processing
     *   -1 -- swipe code failed
     *   -2 -- swipe input timeout
     */
    int _status = 2;

    unsigned int _id;

private:
    SwipeCode _code;
    SwypeStepDetector _stepDetector;

    unsigned int _maxTimestamp = 0;

    unsigned int _currentStep = 0;

    bool _relaxed = true;

    unsigned int _startTimestamp = 0;

    static unsigned int counter;
};

#endif //PROVER_MVP_ANDROID_SWYPECODEDETECTOR_H