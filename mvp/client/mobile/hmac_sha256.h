// Blashyrkh.maniac.coding

#ifndef __HMAC_SHA256_H
#define __HMAC_SHA256_H

#include "sha256.h"


struct hmac_sha256_context
{
    uint8_t               okey[64];
    struct sha256_context hashctx;
};

void hmac_sha256_start(struct hmac_sha256_context *ctx, const uint8_t *key, size_t keylen);
void hmac_sha256_step(struct hmac_sha256_context *ctx, const uint8_t *in, size_t size);
void hmac_sha256_finish(struct hmac_sha256_context *ctx, uint8_t hash[32]);
void hmac_sha256_simple(uint8_t hash[32], const uint8_t *key, size_t keylen, const uint8_t *in, size_t size);

#endif
