// Blashyrkh.maniac.coding

#ifndef __SHA256_H
#define __SHA256_H

#include <stdint.h>
#include <stddef.h>


struct sha256_context
{
    uint32_t  s[8];
    uint8_t   buffer[64];
    uint64_t  size;
};

void sha256_start(struct sha256_context *ctx);
void sha256_step(struct sha256_context *ctx, const uint8_t *in, size_t size);
void sha256_finish(struct sha256_context *ctx, uint8_t hash[32]);
void sha256_simple(uint8_t hash[32], const uint8_t *in, size_t size);

#endif
