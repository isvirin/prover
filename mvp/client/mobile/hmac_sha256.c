// Blashyrkh.maniac.coding

#include "hmac_sha256.h"
#include <string.h>


void hmac_sha256_start(struct hmac_sha256_context *ctx, const uint8_t *key, size_t keylen)
{
    uint8_t ikey[64];
    int i;

    if(keylen>64)
    {
        sha256_simple(ctx->okey, key, keylen);
        keylen=32;
    }
    else
    {
        memcpy(ctx->okey, key, keylen);
    }
    memset(ctx->okey+keylen, 0, 64-keylen);
    memcpy(ikey, ctx->okey, 64);

    for(i=0; i<64; ++i)
    {
        ctx->okey[i]^=0x5C;
        ikey[i]^=0x36;
    }

    sha256_start(&ctx->hashctx);
    sha256_step(&ctx->hashctx, ikey, 64);
}

void hmac_sha256_step(struct hmac_sha256_context *ctx, const uint8_t *in, size_t size)
{
    sha256_step(&ctx->hashctx, in, size);
}

void hmac_sha256_finish(struct hmac_sha256_context *ctx, uint8_t hash[32])
{
    uint8_t h1[32];
    sha256_finish(&ctx->hashctx, h1);

    sha256_start(&ctx->hashctx);
    sha256_step(&ctx->hashctx, ctx->okey, 64);
    sha256_step(&ctx->hashctx, h1, 32);
    sha256_finish(&ctx->hashctx, hash); 
}

void hmac_sha256_simple(uint8_t hash[32], const uint8_t *key, size_t keylen, const uint8_t *in, size_t size)
{
    struct hmac_sha256_context ctx;
    hmac_sha256_start(&ctx, key, keylen);
    hmac_sha256_step(&ctx, in, size);
    hmac_sha256_finish(&ctx, hash);
}

#ifdef TEST_HMAC_SHA256
#include <stdio.h>

int main()
{
    uint8_t hash[32];
    int i;

    hmac_sha256_simple(hash, (const uint8_t *)"\x0b\x0b\x0b\x0b\x0b\x0b\x0b\x0b\x0b\x0b\x0b\x0b\x0b\x0b\x0b\x0b\x0b\x0b\x0b\x0b", 20, (const uint8_t *)"Hi There", 8);
    for(i=0; i<32; ++i)
        printf("%02x", (int)hash[i]);
    printf("\n");

    hmac_sha256_simple(hash, (const uint8_t *)"Jefe", 4, (const uint8_t *)"what do ya want for nothing?", 28);
    for(i=0; i<32; ++i)
        printf("%02x", (int)hash[i]);
    printf("\n");

    hmac_sha256_simple(hash, (const uint8_t *)"\xaa\xaa\xaa\xaa\xaa\xaa\xaa\xaa\xaa\xaa\xaa\xaa\xaa\xaa\xaa\xaa\xaa\xaa\xaa\xaa", 20, (const uint8_t *)"\xdd\xdd\xdd\xdd\xdd\xdd\xdd\xdd\xdd\xdd\xdd\xdd\xdd\xdd\xdd\xdd\xdd\xdd\xdd\xdd\xdd\xdd\xdd\xdd\xdd\xdd\xdd\xdd\xdd\xdd\xdd\xdd\xdd\xdd\xdd\xdd\xdd\xdd\xdd\xdd\xdd\xdd\xdd\xdd\xdd\xdd\xdd\xdd\xdd\xdd", 50);
    for(i=0; i<32; ++i)
        printf("%02x", (int)hash[i]);
    printf("\n");

    hmac_sha256_simple(hash, (const uint8_t *)"\x01\x02\x03\x04\x05\x06\x07\x08\x09\x0a\x0b\x0c\x0d\x0e\x0f\x10\x11\x12\x13\x14\x15\x16\x17\x18\x19", 25, (const uint8_t *)"\xcd\xcd\xcd\xcd\xcd\xcd\xcd\xcd\xcd\xcd\xcd\xcd\xcd\xcd\xcd\xcd\xcd\xcd\xcd\xcd\xcd\xcd\xcd\xcd\xcd\xcd\xcd\xcd\xcd\xcd\xcd\xcd\xcd\xcd\xcd\xcd\xcd\xcd\xcd\xcd\xcd\xcd\xcd\xcd\xcd\xcd\xcd\xcd\xcd\xcd", 50);
    for(i=0; i<32; ++i)
        printf("%02x", (int)hash[i]);
    printf("\n");

    return 0;
}

#endif
