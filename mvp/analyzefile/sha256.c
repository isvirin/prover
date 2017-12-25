// Blashyrkh.maniac.coding
// BTC:1Maniaccv5vSQVuwrmRtfazhf2WsUJ1KyD DOGE:DManiac9Gk31A4vLw9fLN9jVDFAQZc2zPj

#include "sha256.h"
#include <string.h>


static inline uint32_t ReadBE32(const uint8_t *ptr)
{
    return (ptr[0]<<24)|(ptr[1]<<16)|(ptr[2]<<8)|(ptr[3]);
}

static inline void WriteBE32(uint8_t *ptr, uint32_t x)
{
    ptr[0]=(x>>24)&0xFF;
    ptr[1]=(x>>16)&0xFF;
    ptr[2]=(x>>8)&0xFF;
    ptr[3]=x&0xFF;
}

static inline void WriteBE64(uint8_t *ptr, uint64_t x)
{
    ptr[0]=(x>>56)&0xFF;
    ptr[1]=(x>>48)&0xFF;
    ptr[2]=(x>>40)&0xFF;
    ptr[3]=(x>>32)&0xFF;
    ptr[4]=(x>>24)&0xFF;
    ptr[5]=(x>>16)&0xFF;
    ptr[6]=(x>>8)&0xFF;
    ptr[7]=x&0xFF;
}

static inline uint32_t Ch(uint32_t x, uint32_t y, uint32_t z)
{
    return z^(x&(y^z));
}

static inline uint32_t Maj(uint32_t x, uint32_t y, uint32_t z)
{
    return (x&y)|(z&(x|y));
}

static inline uint32_t Sigma0(uint32_t x)
{
    return (x>>2|x<<30)^(x>>13|x<<19)^(x>>22|x<<10);
}

static inline uint32_t Sigma1(uint32_t x)
{
    return (x>>6|x<<26)^(x>>11|x<<21)^(x>>25|x<<7);
}

static inline uint32_t sigma0(uint32_t x)
{
    return (x>>7|x<<25)^(x>>18|x<<14)^(x>>3);
}

static inline uint32_t sigma1(uint32_t x)
{
    return (x>>17|x<<15)^(x>>19|x<<13)^(x>>10);
}

static inline void Round(uint32_t a, uint32_t b, uint32_t c, uint32_t *d, uint32_t e, uint32_t f, uint32_t g, uint32_t *h, uint32_t k, uint32_t w)
{
    uint32_t t1=*h+Sigma1(e)+Ch(e, f, g)+k+w;
    uint32_t t2=Sigma0(a)+Maj(a, b, c);
    *d+=t1;
    *h=t1+t2;
}

static void Transform(uint32_t s[8], const uint8_t chunk[64])
{
    uint32_t a=s[0], b=s[1], c=s[2], d=s[3], e=s[4], f=s[5], g=s[6], h=s[7];
    uint32_t w0, w1, w2, w3, w4, w5, w6, w7, w8, w9, w10, w11, w12, w13, w14, w15;

    Round(a, b, c, &d, e, f, g, &h, 0x428a2f98, w0 = ReadBE32(chunk + 0));
    Round(h, a, b, &c, d, e, f, &g, 0x71374491, w1 = ReadBE32(chunk + 4));
    Round(g, h, a, &b, c, d, e, &f, 0xb5c0fbcf, w2 = ReadBE32(chunk + 8));
    Round(f, g, h, &a, b, c, d, &e, 0xe9b5dba5, w3 = ReadBE32(chunk + 12));
    Round(e, f, g, &h, a, b, c, &d, 0x3956c25b, w4 = ReadBE32(chunk + 16));
    Round(d, e, f, &g, h, a, b, &c, 0x59f111f1, w5 = ReadBE32(chunk + 20));
    Round(c, d, e, &f, g, h, a, &b, 0x923f82a4, w6 = ReadBE32(chunk + 24));
    Round(b, c, d, &e, f, g, h, &a, 0xab1c5ed5, w7 = ReadBE32(chunk + 28));
    Round(a, b, c, &d, e, f, g, &h, 0xd807aa98, w8 = ReadBE32(chunk + 32));
    Round(h, a, b, &c, d, e, f, &g, 0x12835b01, w9 = ReadBE32(chunk + 36));
    Round(g, h, a, &b, c, d, e, &f, 0x243185be, w10 = ReadBE32(chunk + 40));
    Round(f, g, h, &a, b, c, d, &e, 0x550c7dc3, w11 = ReadBE32(chunk + 44));
    Round(e, f, g, &h, a, b, c, &d, 0x72be5d74, w12 = ReadBE32(chunk + 48));
    Round(d, e, f, &g, h, a, b, &c, 0x80deb1fe, w13 = ReadBE32(chunk + 52));
    Round(c, d, e, &f, g, h, a, &b, 0x9bdc06a7, w14 = ReadBE32(chunk + 56));
    Round(b, c, d, &e, f, g, h, &a, 0xc19bf174, w15 = ReadBE32(chunk + 60));

    Round(a, b, c, &d, e, f, g, &h, 0xe49b69c1, w0 += sigma1(w14) + w9 + sigma0(w1));
    Round(h, a, b, &c, d, e, f, &g, 0xefbe4786, w1 += sigma1(w15) + w10 + sigma0(w2));
    Round(g, h, a, &b, c, d, e, &f, 0x0fc19dc6, w2 += sigma1(w0) + w11 + sigma0(w3));
    Round(f, g, h, &a, b, c, d, &e, 0x240ca1cc, w3 += sigma1(w1) + w12 + sigma0(w4));
    Round(e, f, g, &h, a, b, c, &d, 0x2de92c6f, w4 += sigma1(w2) + w13 + sigma0(w5));
    Round(d, e, f, &g, h, a, b, &c, 0x4a7484aa, w5 += sigma1(w3) + w14 + sigma0(w6));
    Round(c, d, e, &f, g, h, a, &b, 0x5cb0a9dc, w6 += sigma1(w4) + w15 + sigma0(w7));
    Round(b, c, d, &e, f, g, h, &a, 0x76f988da, w7 += sigma1(w5) + w0 + sigma0(w8));
    Round(a, b, c, &d, e, f, g, &h, 0x983e5152, w8 += sigma1(w6) + w1 + sigma0(w9));
    Round(h, a, b, &c, d, e, f, &g, 0xa831c66d, w9 += sigma1(w7) + w2 + sigma0(w10));
    Round(g, h, a, &b, c, d, e, &f, 0xb00327c8, w10 += sigma1(w8) + w3 + sigma0(w11));
    Round(f, g, h, &a, b, c, d, &e, 0xbf597fc7, w11 += sigma1(w9) + w4 + sigma0(w12));
    Round(e, f, g, &h, a, b, c, &d, 0xc6e00bf3, w12 += sigma1(w10) + w5 + sigma0(w13));
    Round(d, e, f, &g, h, a, b, &c, 0xd5a79147, w13 += sigma1(w11) + w6 + sigma0(w14));
    Round(c, d, e, &f, g, h, a, &b, 0x06ca6351, w14 += sigma1(w12) + w7 + sigma0(w15));
    Round(b, c, d, &e, f, g, h, &a, 0x14292967, w15 += sigma1(w13) + w8 + sigma0(w0));

    Round(a, b, c, &d, e, f, g, &h, 0x27b70a85, w0 += sigma1(w14) + w9 + sigma0(w1));
    Round(h, a, b, &c, d, e, f, &g, 0x2e1b2138, w1 += sigma1(w15) + w10 + sigma0(w2));
    Round(g, h, a, &b, c, d, e, &f, 0x4d2c6dfc, w2 += sigma1(w0) + w11 + sigma0(w3));
    Round(f, g, h, &a, b, c, d, &e, 0x53380d13, w3 += sigma1(w1) + w12 + sigma0(w4));
    Round(e, f, g, &h, a, b, c, &d, 0x650a7354, w4 += sigma1(w2) + w13 + sigma0(w5));
    Round(d, e, f, &g, h, a, b, &c, 0x766a0abb, w5 += sigma1(w3) + w14 + sigma0(w6));
    Round(c, d, e, &f, g, h, a, &b, 0x81c2c92e, w6 += sigma1(w4) + w15 + sigma0(w7));
    Round(b, c, d, &e, f, g, h, &a, 0x92722c85, w7 += sigma1(w5) + w0 + sigma0(w8));
    Round(a, b, c, &d, e, f, g, &h, 0xa2bfe8a1, w8 += sigma1(w6) + w1 + sigma0(w9));
    Round(h, a, b, &c, d, e, f, &g, 0xa81a664b, w9 += sigma1(w7) + w2 + sigma0(w10));
    Round(g, h, a, &b, c, d, e, &f, 0xc24b8b70, w10 += sigma1(w8) + w3 + sigma0(w11));
    Round(f, g, h, &a, b, c, d, &e, 0xc76c51a3, w11 += sigma1(w9) + w4 + sigma0(w12));
    Round(e, f, g, &h, a, b, c, &d, 0xd192e819, w12 += sigma1(w10) + w5 + sigma0(w13));
    Round(d, e, f, &g, h, a, b, &c, 0xd6990624, w13 += sigma1(w11) + w6 + sigma0(w14));
    Round(c, d, e, &f, g, h, a, &b, 0xf40e3585, w14 += sigma1(w12) + w7 + sigma0(w15));
    Round(b, c, d, &e, f, g, h, &a, 0x106aa070, w15 += sigma1(w13) + w8 + sigma0(w0));

    Round(a, b, c, &d, e, f, g, &h, 0x19a4c116, w0 += sigma1(w14) + w9 + sigma0(w1));
    Round(h, a, b, &c, d, e, f, &g, 0x1e376c08, w1 += sigma1(w15) + w10 + sigma0(w2));
    Round(g, h, a, &b, c, d, e, &f, 0x2748774c, w2 += sigma1(w0) + w11 + sigma0(w3));
    Round(f, g, h, &a, b, c, d, &e, 0x34b0bcb5, w3 += sigma1(w1) + w12 + sigma0(w4));
    Round(e, f, g, &h, a, b, c, &d, 0x391c0cb3, w4 += sigma1(w2) + w13 + sigma0(w5));
    Round(d, e, f, &g, h, a, b, &c, 0x4ed8aa4a, w5 += sigma1(w3) + w14 + sigma0(w6));
    Round(c, d, e, &f, g, h, a, &b, 0x5b9cca4f, w6 += sigma1(w4) + w15 + sigma0(w7));
    Round(b, c, d, &e, f, g, h, &a, 0x682e6ff3, w7 += sigma1(w5) + w0 + sigma0(w8));
    Round(a, b, c, &d, e, f, g, &h, 0x748f82ee, w8 += sigma1(w6) + w1 + sigma0(w9));
    Round(h, a, b, &c, d, e, f, &g, 0x78a5636f, w9 += sigma1(w7) + w2 + sigma0(w10));
    Round(g, h, a, &b, c, d, e, &f, 0x84c87814, w10 += sigma1(w8) + w3 + sigma0(w11));
    Round(f, g, h, &a, b, c, d, &e, 0x8cc70208, w11 += sigma1(w9) + w4 + sigma0(w12));
    Round(e, f, g, &h, a, b, c, &d, 0x90befffa, w12 += sigma1(w10) + w5 + sigma0(w13));
    Round(d, e, f, &g, h, a, b, &c, 0xa4506ceb, w13 += sigma1(w11) + w6 + sigma0(w14));
    Round(c, d, e, &f, g, h, a, &b, 0xbef9a3f7, w14 + sigma1(w12) + w7 + sigma0(w15));
    Round(b, c, d, &e, f, g, h, &a, 0xc67178f2, w15 + sigma1(w13) + w8 + sigma0(w0));

    s[0]+=a;
    s[1]+=b;
    s[2]+=c;
    s[3]+=d;
    s[4]+=e;
    s[5]+=f;
    s[6]+=g;
    s[7]+=h;
}

void sha256_start(struct sha256_context *ctx)
{
    ctx->s[0]=0x6a09e667;
    ctx->s[1]=0xbb67ae85;
    ctx->s[2]=0x3c6ef372;
    ctx->s[3]=0xa54ff53a;
    ctx->s[4]=0x510e527f;
    ctx->s[5]=0x9b05688c;
    ctx->s[6]=0x1f83d9ab;
    ctx->s[7]=0x5be0cd19;
    ctx->size=0;
}

void sha256_step(struct sha256_context *ctx, const uint8_t *in, size_t size)
{
    unsigned int bufsize=ctx->size%64;
    if(bufsize+size>=64)
    {
        unsigned int n=64-bufsize;
        memcpy(ctx->buffer+bufsize, in, n);
        in+=n;
        size-=n;
        ctx->size+=n;
        Transform(ctx->s, ctx->buffer);
        bufsize=0;
    }

    while(size>=64)
    {
        Transform(ctx->s, in);
        in+=64;
        size-=64;
        ctx->size+=64;
    }

    if(size>0)
    {
        memcpy(ctx->buffer+bufsize, in, size);
        ctx->size+=size;
    }
}

void sha256_finish(struct sha256_context *ctx, uint8_t hash[32])
{
    unsigned int bufsize=ctx->size%64;
    ctx->buffer[bufsize]=0x80;

    if(bufsize<=55)
    {
        memset(ctx->buffer+bufsize+1, 0, 64-8-1-bufsize);
        WriteBE64(ctx->buffer+64-8, ctx->size<<3);
        Transform(ctx->s, ctx->buffer);
    }
    else
    {
        memset(ctx->buffer+bufsize+1, 0, 64-1-bufsize);
        Transform(ctx->s, ctx->buffer);

        memset(ctx->buffer, 0, 64-8);
        WriteBE64(ctx->buffer+64-8, ctx->size<<3);
        Transform(ctx->s, ctx->buffer);
    }

    WriteBE32(hash+0, ctx->s[0]);
    WriteBE32(hash+4, ctx->s[1]);
    WriteBE32(hash+8, ctx->s[2]);
    WriteBE32(hash+12, ctx->s[3]);
    WriteBE32(hash+16, ctx->s[4]);
    WriteBE32(hash+20, ctx->s[5]);
    WriteBE32(hash+24, ctx->s[6]);
    WriteBE32(hash+28, ctx->s[7]);
}

void sha256_simple(uint8_t hash[32], const uint8_t *data, size_t size)
{
    struct sha256_context ctx;
    sha256_start(&ctx);
    sha256_step(&ctx, data, size);
    sha256_finish(&ctx, hash);
}

#ifdef TEST_SHA256
#include <stdio.h>

int main()
{
    uint8_t hash[32];
    int i, j;

    sha256_simple(hash, (const uint8_t *)"abc", 3);
    for(i=0; i<32; ++i)
        printf("%02x", (int)hash[i]);
    printf("\n");

    sha256_simple(hash, (const uint8_t *)"", 0);
    for(i=0; i<32; ++i)
        printf("%02x", (int)hash[i]);
    printf("\n");

    sha256_simple(hash, (const uint8_t *)"abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq", 56);
    for(i=0; i<32; ++i)
        printf("%02x", (int)hash[i]);
    printf("\n");

    sha256_simple(hash, (const uint8_t *)"abcdefghbcdefghicdefghijdefghijkefghijklfghijklmghijklmnhijklmnoijklmnopjklmnopqklmnopqrlmnopqrsmnopqrstnopqrstu", 112);
    for(i=0; i<32; ++i)
        printf("%02x", (int)hash[i]);
    printf("\n");

    for(j=0; j<=112; ++j)
    {
        const char *str="abcdefghbcdefghicdefghijdefghijkefghijklfghijklmghijklmnhijklmnoijklmnopjklmnopqklmnopqrlmnopqrsmnopqrstnopqrstu";
        struct sha256_context ctx1, ctx2;

        sha256_start(&ctx1);
        sha256_step(&ctx1, str, j);
        memcpy(&ctx2, &ctx1, sizeof(struct sha256_context));
        sha256_step(&ctx2, str+j, 112-j);
        sha256_finish(&ctx2, hash);
        for(i=0; i<32; ++i)
            printf("%02x", (int)hash[i]);
        printf("  %u %u\n", j, 112-j);
    }

    return 0;
}

#endif
