// Blashyrkh.maniac.coding
// Today is Setting Orange, the 56th day of Bureaucracy in the YOLD 3183

#include "swype.h"


std::string generateSwypeFromInteger(unsigned int n, unsigned int modulo)
{
    static const unsigned int neighbours[9][9]=
    {
        {3, 1, 3, 4, 0, 0, 0, 0, 0},
        {5, 0, 2, 3, 4, 5, 0, 0, 0},
        {3, 1, 4, 5, 0, 0, 0, 0, 0},
        {5, 0, 1, 4, 6, 7, 0, 0, 0},
        {8, 0, 1, 2, 3, 5, 6, 7, 8},
        {5, 1, 2, 4, 7, 8, 0, 0, 0},
        {3, 3, 4, 7, 0, 0, 0, 0, 0},
        {5, 3, 4, 5, 6, 8, 0, 0, 0},
        {3, 4, 5, 7, 0, 0, 0, 0, 0}
    };

    n%=modulo;

    std::string res;

    unsigned int m=1;

    unsigned int currentPoint=(n*9)/modulo;
    m*=9;
    n=(n*9)%modulo;

    res+='1'+currentPoint;

    while(m<modulo)
    {
        const unsigned int neighbourCount=neighbours[currentPoint][0];
        const unsigned int nextPointIndex=(n*neighbourCount)/modulo;

        m*=neighbourCount;
        n=(n*neighbourCount)%modulo;

        currentPoint=neighbours[currentPoint][1+nextPointIndex];
        res+='1'+currentPoint;
    }

    return res;
}

#ifdef TEST_SWYPE

#include <iostream>

int main()
{
    for(int i=0; i<65536; ++i)
        std::cout<<generateSwypeFromInteger(i)<<std::endl;
    return 0;
}

#endif
