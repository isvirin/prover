//
// Created by babay on 06.01.2018.
//

#ifndef PROVER_MVP_ANDROID_SWIPECODE_H
#define PROVER_MVP_ANDROID_SWIPECODE_H

#include "VectorExplained.h"

class SwipeCode {
public:
    inline SwipeCode() {}

    inline void Init(std::string swype) {
        if (swype == "") {
            _length = 0;
        } else {
            _length = (unsigned int) (swype.length() - 1);
            if (_length > 16)
                _length = 16;
            int current, prev;
            VectorExplained tmp;
            prev = swype.at(0) - '0';
            for (uint i = 0; i < _length; i++) {
                current = swype.at(i + 1) - '0';
                tmp.SetSwipePoints(prev, current);
                _directions[i] = (char) tmp._direction;
                prev = current;
            }
        }
        if (logLevel > 0) {
            char tmp[17];
            for (int i = 0; i < _length; i++) {
                tmp[i] = _directions[i] + '0';
            }
            tmp[_length] = 0;
            LOGI_NATIVE("Set swipe code: %s, directions: %s", swype.c_str(), tmp);
        }
    }

    inline bool empty() { return _length == 0; }

    unsigned int _length = 0;
    char _directions[16];
};


#endif //PROVER_MVP_ANDROID_SWIPECODE_H
