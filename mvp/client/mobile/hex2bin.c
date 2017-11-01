// Blashyrkh.maniac.coding

#include "hex2bin.h"
#include <string.h>


static const int hex2bin_table[256]=
{
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 0-15
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 16-31
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 32-47
     0,  1,  2,  3,  4,  5,  6,  7,  8,  9, -1, -1, -1, -1, -1, -1, // 48-63
    -1, 10, 11, 12, 13, 14, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 64-79
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 80-95
    -1, 10, 11, 12, 13, 14, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 96-111
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 112-127
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 128-143
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 144-159
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 160-175
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 176-191
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 192-207
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 208-223
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 224-239
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1  // 240-255
};

int hex2bin(const char *hex, uint8_t *bin, unsigned int byteslen)
{
    if(hex[0]=='0' && (hex[1]=='x' || hex[1]=='X'))
        hex+=2;

    for(; byteslen>0; hex+=2, ++bin, --byteslen)
    {
        if(hex2bin_table[(unsigned int)(unsigned char)hex[0]]<0 ||
           hex2bin_table[(unsigned int)(unsigned char)hex[1]]<0)
        {
            return -1;
        }

        *bin=hex2bin_table[(unsigned int)(unsigned char)hex[0]]*16+hex2bin_table[(unsigned int)(unsigned char)hex[1]];
    }
    return 0;
}

int hex2bin_bigendian(const char *hex, uint8_t *bin, unsigned int byteslen)
{
    uint8_t *bp;
    const char *hp;

    if(hex[0]=='0' && (hex[1]=='x' || hex[1]=='X'))
        hex+=2;

    bp=bin+byteslen-1;
    hp=hex+strlen(hex)-1;

    while(bp>=bin && hp>=hex)
    {
        unsigned int v;

        if(hex2bin_table[(unsigned int)(unsigned char)*hp]==-1)
            return -1;

        v=hex2bin_table[(unsigned int)(unsigned char)*hp];
        --hp;

        if(hp>=hex)
        {
            if(hex2bin_table[(unsigned int)(unsigned char)*hp]==-1)
                return -1;

            v+=16*hex2bin_table[(unsigned int)(unsigned char)*hp];
            --hp;
        }

        --bp;
    }
    if(bp<bin && hp>=hex)
        return -1; // no space left in the binary buffer

    while(bp>=bin)
    {
        *bp=0;
        --bp;
    }

    return 0;
}
