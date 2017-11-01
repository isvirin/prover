// Blashyrkh.maniac.coding

#ifndef __HEX2BIN_H
#define __HEX2BIN_H

#include <stdint.h>


int hex2bin(const char *hex, uint8_t *bin, unsigned int byteslen);

// A number in hex form (with or without 0x prefix) is decoded into
// binary form and right-justified in the buffer.
int hex2bin_bigendian(const char *hex, uint8_t *bin, unsigned int byteslen);

#endif

