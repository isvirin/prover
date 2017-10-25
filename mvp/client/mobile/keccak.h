// Blashyrkh.maniac.coding
// Based on code by Guido Bertoni, Joan Daemen, Michaël Peeters,
// Gilles Van Assche and Ronny Van Keer

#ifndef __KECCAK_H
#define __KECCAK_H

#include <stdint.h>
#include <stddef.h>


struct keccak_context
{
    uint8_t       state[200];
    unsigned int  rate;
    unsigned int  hashsize;
    uint8_t       suffix;
    unsigned int  pos;
};

void keccak_start(
    struct keccak_context *ctx,
    unsigned int           rate,
    unsigned int           hashbitlen,
    uint8_t                suffix);

void keccak_step(
    struct keccak_context *ctx,
    const uint8_t         *in,
    size_t                 size);

void keccak_finish(
    struct keccak_context *ctx,
    uint8_t               *hash);

void keccak_simple(
    uint8_t       *hash,
    unsigned int   rate,
    unsigned int   hashsize,
    unsigned char  suffix,
    const uint8_t *in,
    size_t         size);

#endif
