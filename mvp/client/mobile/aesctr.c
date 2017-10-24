// Blashyrkh.maniac.coding

#include "aesctr.h"
#include <assert.h>
#include <string.h>


static inline void memxor(uint8_t *out, const uint8_t *in, size_t size)
{
    size_t i;
    for(i=0; i<size; ++i)
        out[i]^=in[i];
}

static inline void increment_ctr(uint8_t *ctr)
{
    unsigned int c=1;
    int i;
    for(i=AES_BLOCK_SIZE-1; i>=0 && c>0; --i)
    {
        c+=ctr[i];
        ctr[i]=c&0xFF;
        c>>=8;
    }
}

void aesctr_start(
    struct aesctr_context *ctx,
    const uint8_t         *key,
    size_t                 keylen,
    const uint8_t          iv[AES_BLOCK_SIZE])
{
    aes_encrypt_start(&ctx->aesctx, key, keylen);
    memcpy(ctx->ctr, iv, AES_BLOCK_SIZE);
    ctx->bufoffset=AES_BLOCK_SIZE;
}

void aesctr_step(
    struct aesctr_context *ctx,
    uint8_t               *data,
    size_t                 size)
{
    if(ctx->bufoffset<AES_BLOCK_SIZE && ctx->bufoffset+size>AES_BLOCK_SIZE)
    {
        size_t s=AES_BLOCK_SIZE-ctx->bufoffset;
        memxor(data, ctx->buf+ctx->bufoffset, s);
        data+=s;
        size-=s;
        ctx->bufoffset=AES_BLOCK_SIZE;
    }

    while(ctx->bufoffset+size>AES_BLOCK_SIZE)
    {
        assert(ctx->bufoffset==AES_BLOCK_SIZE);

        aes_encrypt_step(&ctx->aesctx, ctx->buf, ctx->ctr);
        increment_ctr(ctx->ctr);

        ctx->bufoffset=0;

        if(size>=AES_BLOCK_SIZE)
        {
            memxor(data, ctx->buf, AES_BLOCK_SIZE);
            ctx->bufoffset+=AES_BLOCK_SIZE;
            data+=AES_BLOCK_SIZE;
            size-=AES_BLOCK_SIZE;
        }
        else
        {
            memxor(data, ctx->buf, size);
            ctx->bufoffset+=size;
            data+=size;
            size=0;
        }
    }

    if(size>0)
    {
        assert(ctx->bufoffset+size<=AES_BLOCK_SIZE);

        memxor(data, ctx->buf+ctx->bufoffset, size);
        ctx->bufoffset+=size;
    }
}

void aesctr_finish(
    struct aesctr_context *ctx)
{
    aes_encrypt_finish(&ctx->aesctx);
    memset(ctx->ctr, 0, AES_BLOCK_SIZE);
    memset(ctx->buf, 0, AES_BLOCK_SIZE);
}

#ifdef TEST_AESCTR
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

static uint8_t *hex2bin(const char *str, size_t *bytelen)
{
    unsigned int l, i;
    uint8_t *p;

    l=strlen(str);
    *bytelen=l/2;
    p=(uint8_t *)malloc(*bytelen);

    for(i=0; i<l; i+=2)
    {
        char c[3];
        c[0]=str[i];
        c[1]=str[i+1];
        c[2]='\0';

        unsigned int t;
        sscanf(c, "%02X", &t);

        p[i/2]=t;
    }

    return p;
}

static void test(const char *keystr, const char *ivstr, const char *instr, const char *outstr)
{
    uint8_t *key, *iv, *in, *out, *tmp;
    size_t keylen, ivlen, inlen, outlen;
    struct aesctr_context ctx;
    size_t a, b, l1, l2, l3;

    key=hex2bin(keystr, &keylen);
    iv=hex2bin(ivstr, &ivlen);
    in=hex2bin(instr, &inlen);
    out=hex2bin(outstr, &outlen);

    assert(ivlen==AES_BLOCK_SIZE);
    assert(inlen==outlen);

    tmp=(uint8_t *)malloc(inlen);
    memcpy(tmp, in, inlen);

    a=random()%(inlen+1);
    b=random()%(inlen+1);

    l1=a<b?a:b;
    l2=a+b-2*l1;
    l3=inlen-l1-l2;

    assert(l1<=inlen && l2<=inlen && l3<=inlen && l1+l2+l3==inlen);

    aesctr_start(&ctx, key, keylen, iv);
    aesctr_step(&ctx, tmp, l1);
    aesctr_step(&ctx, tmp+l1, l2);
    aesctr_step(&ctx, tmp+l1+l2, l3);
    aesctr_finish(&ctx);

    assert(memcmp(tmp, out, inlen)==0);

    free(key);
    free(iv);
    free(in);
    free(out);
    free(tmp);
}

int main()
{
    int i;
    srandom((unsigned int)time(NULL));
    for(i=0; i<1000000; ++i)
    {
        test("2b7e151628aed2a6abf7158809cf4f3c", "f0f1f2f3f4f5f6f7f8f9fafbfcfdfeff", "6bc1bee22e409f96e93d7e117393172aae2d8a571e03ac9c9eb76fac45af8e5130c81c46a35ce411e5fbc1191a0a52eff69f2445df4f9b17ad2b417be66c3710", "874d6191b620e3261bef6864990db6ce9806f66b7970fdff8617187bb9fffdff5ae4df3edbd5d35e5b4f09020db03eab1e031dda2fbe03d1792170a0f3009cee");
        test("8e73b0f7da0e6452c810f32b809079e562f8ead2522c6b7b", "f0f1f2f3f4f5f6f7f8f9fafbfcfdfeff", "6bc1bee22e409f96e93d7e117393172aae2d8a571e03ac9c9eb76fac45af8e5130c81c46a35ce411e5fbc1191a0a52eff69f2445df4f9b17ad2b417be66c3710", "1abc932417521ca24f2b0459fe7e6e0b090339ec0aa6faefd5ccc2c6f4ce8e941e36b26bd1ebc670d1bd1d665620abf74f78a7f6d29809585a97daec58c6b050");
        test("603deb1015ca71be2b73aef0857d77811f352c073b6108d72d9810a30914dff4", "f0f1f2f3f4f5f6f7f8f9fafbfcfdfeff", "6bc1bee22e409f96e93d7e117393172aae2d8a571e03ac9c9eb76fac45af8e5130c81c46a35ce411e5fbc1191a0a52eff69f2445df4f9b17ad2b417be66c3710", "601ec313775789a5b7a7f504bbf3d228f443e3ca4d62b59aca84e990cacaf5c52b0930daa23de94ce87017ba2d84988ddfc9c58db67aada613c2dd08457941a6");
    }

    return 0;
}

#endif
