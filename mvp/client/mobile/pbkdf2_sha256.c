// Blashyrkh.maniac.coding

#include "pbkdf2_sha256.h"
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include "hmac_sha256.h"


void pbkdf2_sha256_start(
    struct pbkdf2_sha256_context *ctx,
    const uint8_t                *password,
    size_t                        passwordlen,
    const uint8_t                *salt,
    size_t                        saltlen,
    unsigned int                  c)
{
    ctx->password=(uint8_t *)malloc(passwordlen);
    memcpy(ctx->password, password, passwordlen);
    ctx->passwordlen=passwordlen;

    ctx->salt=(uint8_t *)malloc(saltlen+4);
    memcpy(ctx->salt, salt, saltlen);
    ctx->saltlen=saltlen;

    ctx->bufferlen=0;
    ctx->i=0;
    ctx->c=c;
}

void pbkdf2_sha256_step(
    struct pbkdf2_sha256_context *ctx,
    uint8_t                      *out,
    size_t                        count)
{
    if(count>ctx->bufferlen && ctx->bufferlen>0)
    {
        memcpy(out, ctx->buffer, ctx->bufferlen);
        out+=ctx->bufferlen;
        count-=ctx->bufferlen;
        ctx->bufferlen=0;
    }

    while(count>ctx->bufferlen)
    {
        assert(ctx->bufferlen==0);

        uint8_t t[32], h[32];

        ++ctx->i;
        ctx->salt[ctx->saltlen+0]=(ctx->i>>24)&0xFF;
        ctx->salt[ctx->saltlen+1]=(ctx->i>>16)&0xFF;
        ctx->salt[ctx->saltlen+2]=(ctx->i>>8)&0xFF;
        ctx->salt[ctx->saltlen+3]=ctx->i&0xFF;

        hmac_sha256_simple(t, ctx->password, ctx->passwordlen, ctx->salt, ctx->saltlen+4);

        if(ctx->c>1)
        {
            unsigned int i, j;

            memcpy(h, t, 32);
            for(j=1; j<ctx->c; ++j)
            {
                hmac_sha256_simple(h, ctx->password, ctx->passwordlen, h, 32);
                for(i=0; i<32; ++i)
                    t[i]^=h[i];
            }
        }

        memcpy(ctx->buffer, t, 32);
        ctx->bufferlen=32;

        if(count>=32)
        {
            memcpy(out, ctx->buffer, 32);
            out+=32;
            count-=32;
            ctx->bufferlen=0;
        }
    }

    if(count>0)
    {
        assert(count<=ctx->bufferlen);

        memcpy(out, ctx->buffer, count);
        memmove(ctx->buffer, ctx->buffer+count, ctx->bufferlen-count);
        ctx->bufferlen-=count;
    }
}

void pbkdf2_sha256_finish(
    struct pbkdf2_sha256_context *ctx)
{
    memset(ctx->password, 0, ctx->passwordlen);
    free(ctx->password);

    memset(ctx->salt, 0, ctx->saltlen+4);
    free(ctx->salt);

    memset(ctx->buffer, 0, 32);
}

void pbkdf2_sha256_simple(
    uint8_t                      *out,
    size_t                        count,
    const uint8_t                *password,
    size_t                        passwordlen,
    const uint8_t                *salt,
    size_t                        saltlen,
    unsigned int                  c)
{
    struct pbkdf2_sha256_context ctx;
    pbkdf2_sha256_start(&ctx, password, passwordlen, salt, saltlen, c);
    pbkdf2_sha256_step(&ctx, out, count);
    pbkdf2_sha256_finish(&ctx);
}

#ifdef TEST_PBKDF2_SHA256
#include <stdio.h>
#include <stdlib.h>
#include <time.h>

int main()
{
    struct pbkdf2_sha256_context ctx;
    uint8_t dk[64];
    int i;

    pbkdf2_sha256_simple(dk, 64, (const uint8_t *)"Password", 8, (const uint8_t *)"NaCl", 4, 80000);
    for(i=0; i<64; ++i)
        printf("%02x%c", (int)dk[i], i%16==15?'\n':' ');
    printf("\n");

    pbkdf2_sha256_simple(dk, 64, (const uint8_t *)"passwd", 6, (const uint8_t *)"salt", 4, 1);
    for(i=0; i<64; ++i)
        printf("%02x%c", (int)dk[i], i%16==15?'\n':' ');
    printf("\n");

    srandom((unsigned int)time(NULL));
    for(i=0; i<1000000; ++i)
    {
        size_t a, b, l1, l2, l3;
        uint8_t dk2[64];

        a=random()%65;
        b=random()%65;

        l1=a<b?a:b;
        l2=a+b-2*l1;
        l3=64-l1-l2;

        memset(dk2, 0, 64);

        pbkdf2_sha256_start(&ctx, (const uint8_t *)"passwd", 6, (const uint8_t *)"salt", 4, 1);
        pbkdf2_sha256_step(&ctx, dk2+0, l1);
        pbkdf2_sha256_step(&ctx, dk2+l1, l2);
        pbkdf2_sha256_step(&ctx, dk2+l1+l2, l3);
        pbkdf2_sha256_finish(&ctx);

        assert(memcmp(dk2, dk, 64)==0);
    }

    return 0;
}

#endif
