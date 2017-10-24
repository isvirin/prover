// Blashyrkh.maniac.coding

#include "scrypt.h"
#include <stdlib.h>
#include <string.h>


static inline void memxor(uint8_t *out, const uint8_t *in1, const uint8_t *in2, size_t size)
{
    size_t i;
    for(i=0; i<size; ++i)
        out[i]=in1[i]^in2[i];
}

static inline uint32_t R(uint32_t a, unsigned int b)
{
    return (a<<b)|(a>>(32-b));
}

static void salsa20_8_core(uint8_t out[64], const uint8_t in[64])
{
    uint32_t x[16];
    int i;

    for(i=0; i<16; ++i)
        x[i]=in[4*i+0]|(in[4*i+1]<<8)|(in[4*i+2]<<16)|(in[4*i+3]<<24);
    for(i=8; i>0; i-=2)
    {
        x[ 4] ^= R(x[ 0]+x[12], 7);  x[ 8] ^= R(x[ 4]+x[ 0], 9);
        x[12] ^= R(x[ 8]+x[ 4],13);  x[ 0] ^= R(x[12]+x[ 8],18);
        x[ 9] ^= R(x[ 5]+x[ 1], 7);  x[13] ^= R(x[ 9]+x[ 5], 9);
        x[ 1] ^= R(x[13]+x[ 9],13);  x[ 5] ^= R(x[ 1]+x[13],18);
        x[14] ^= R(x[10]+x[ 6], 7);  x[ 2] ^= R(x[14]+x[10], 9);
        x[ 6] ^= R(x[ 2]+x[14],13);  x[10] ^= R(x[ 6]+x[ 2],18);
        x[ 3] ^= R(x[15]+x[11], 7);  x[ 7] ^= R(x[ 3]+x[15], 9);
        x[11] ^= R(x[ 7]+x[ 3],13);  x[15] ^= R(x[11]+x[ 7],18);
        x[ 1] ^= R(x[ 0]+x[ 3], 7);  x[ 2] ^= R(x[ 1]+x[ 0], 9);
        x[ 3] ^= R(x[ 2]+x[ 1],13);  x[ 0] ^= R(x[ 3]+x[ 2],18);
        x[ 6] ^= R(x[ 5]+x[ 4], 7);  x[ 7] ^= R(x[ 6]+x[ 5], 9);
        x[ 4] ^= R(x[ 7]+x[ 6],13);  x[ 5] ^= R(x[ 4]+x[ 7],18);
        x[11] ^= R(x[10]+x[ 9], 7);  x[ 8] ^= R(x[11]+x[10], 9);
        x[ 9] ^= R(x[ 8]+x[11],13);  x[10] ^= R(x[ 9]+x[ 8],18);
        x[12] ^= R(x[15]+x[14], 7);  x[13] ^= R(x[12]+x[15], 9);
        x[14] ^= R(x[13]+x[12],13);  x[15] ^= R(x[14]+x[13],18);
    }
    for(i=0; i<16; ++i)
    {
        uint32_t t=x[i]+(in[4*i+0]|(in[4*i+1]<<8)|(in[4*i+2]<<16)|(in[4*i+3]<<24));
        out[4*i+0]=t&0xFF;
        out[4*i+1]=(t>>8)&0xFF;
        out[4*i+2]=(t>>16)&0xFF;
        out[4*i+3]=(t>>24)&0xFF;
    }
}

static void scryptBlockMix(uint8_t *out, const uint8_t *in, unsigned int r)
{
    uint8_t x[64], t[64];
    unsigned int i;

    memcpy(x, in+128*r-64, 64);
    for(i=0; i<r; ++i)
    {
        memxor(t, x, in+i*128, 64);
        salsa20_8_core(x, t);
        memcpy(out+i*64, x, 64);

        memxor(t, x, in+i*128+64, 64);
        salsa20_8_core(x, t);
        memcpy(out+(i+r)*64, x, 64);
    }
}

static void scryptROMix(uint8_t *out, const uint8_t *in, unsigned int r, unsigned int N, uint8_t *buffer)
{
    uint8_t *x, *t, *v;
    unsigned int i;

    if(buffer)
    {
        x=buffer;
        t=buffer+128*r;
        v=buffer+2*128*r;
    }
    else
    {
        x=(uint8_t *)malloc(128*r);
        t=(uint8_t *)malloc(128*r);
        v=(uint8_t *)malloc(128*r*N);
    }

    memcpy(x, in, 128*r);
    for(i=0; i<N; ++i)
    {
        memcpy(v+i*128*r, x, 128*r);
        scryptBlockMix(t, x, r);
        memcpy(x, t, 128*r);
    }

    for(i=0; i<N; ++i)
    {
        uint32_t j=(x[(2*r-1)*64+0]|(x[(2*r-1)*64+1]<<8)|(x[(2*r-1)*64+2]<<16)|(x[(2*r-1)*64+3]<<24))%N;
        memxor(t, x, v+j*128*r, 128*r);
        scryptBlockMix(x, t, r);
    }

    memcpy(out, x, 128*r);

    if(!buffer)
    {
        memset(x, 0, 128*r);
        memset(t, 0, 128*r);
        memset(v, 0, 128*r*N);
        free(x);
        free(t);
        free(v);
    }
}

void scrypt_start(
    struct scrypt_context *ctx,
    const uint8_t         *password,
    size_t                 passwordlen,
    const uint8_t         *salt,
    size_t                 saltlen,
    unsigned int           n,
    unsigned int           r,
    unsigned int           p)
{
    unsigned int i;
    size_t blen;
    uint8_t *b, *buffer;

    blen=p*128*r;
    b=(uint8_t *)malloc(blen);
    buffer=(uint8_t *)malloc(128*r*(n+2));

    pbkdf2_sha256_simple(b, blen, password, passwordlen, salt, saltlen, 1);

    for(i=0; i<p; ++i)
        scryptROMix(b+i*128*r, b+i*128*r, r, n, buffer);

    pbkdf2_sha256_start(&ctx->pbkdf2ctx, password, passwordlen, b, blen, 1);

    memset(b, 0, blen);
    free(b);

    memset(buffer, 0, 128*r*(n+2));
    free(buffer);
}

void scrypt_step(
    struct scrypt_context *ctx,
    uint8_t               *out,
    size_t                 count)
{
    pbkdf2_sha256_step(&ctx->pbkdf2ctx, out, count);
}

void scrypt_finish(
    struct scrypt_context *ctx)
{
    pbkdf2_sha256_finish(&ctx->pbkdf2ctx);
}

#ifdef TEST_SCRYPT
#include <stdio.h>

static inline void scrypt(
    uint8_t               *out,
    size_t                 count,
    const uint8_t         *password,
    size_t                 passwordlen,
    const uint8_t         *salt,
    size_t                 saltlen,
    unsigned int           n,
    unsigned int           r,
    unsigned int           p)
{
    struct scrypt_context ctx;
    scrypt_start(&ctx, password, passwordlen, salt, saltlen, n, r, p);
    scrypt_step(&ctx, out, count);
    scrypt_finish(&ctx);
}

int main()
{
    {
        uint8_t in[64]=
        {
            0x7e, 0x87, 0x9a, 0x21, 0x4f, 0x3e, 0xc9, 0x86, 0x7c, 0xa9, 0x40, 0xe6, 0x41, 0x71, 0x8f, 0x26,
            0xba, 0xee, 0x55, 0x5b, 0x8c, 0x61, 0xc1, 0xb5, 0x0d, 0xf8, 0x46, 0x11, 0x6d, 0xcd, 0x3b, 0x1d,
            0xee, 0x24, 0xf3, 0x19, 0xdf, 0x9b, 0x3d, 0x85, 0x14, 0x12, 0x1e, 0x4b, 0x5a, 0xc5, 0xaa, 0x32,
            0x76, 0x02, 0x1d, 0x29, 0x09, 0xc7, 0x48, 0x29, 0xed, 0xeb, 0xc6, 0x8d, 0xb8, 0xb8, 0xc2, 0x5e
        };
        uint8_t out[64];
        int i;

        salsa20_8_core(out, in);

        for(i=0; i<64; ++i)
        {
            printf("%02x%c", (int)out[i], i%16==15?'\n':' ');
        }
        printf("\n");
    }
    {
        uint8_t in[128]=
        {
            0xf7, 0xce, 0x0b, 0x65, 0x3d, 0x2d, 0x72, 0xa4, 0x10, 0x8c, 0xf5, 0xab, 0xe9, 0x12, 0xff, 0xdd,
            0x77, 0x76, 0x16, 0xdb, 0xbb, 0x27, 0xa7, 0x0e, 0x82, 0x04, 0xf3, 0xae, 0x2d, 0x0f, 0x6f, 0xad,
            0x89, 0xf6, 0x8f, 0x48, 0x11, 0xd1, 0xe8, 0x7b, 0xcc, 0x3b, 0xd7, 0x40, 0x0a, 0x9f, 0xfd, 0x29,
            0x09, 0x4f, 0x01, 0x84, 0x63, 0x95, 0x74, 0xf3, 0x9a, 0xe5, 0xa1, 0x31, 0x52, 0x17, 0xbc, 0xd7,
            0x89, 0x49, 0x91, 0x44, 0x72, 0x13, 0xbb, 0x22, 0x6c, 0x25, 0xb5, 0x4d, 0xa8, 0x63, 0x70, 0xfb,
            0xcd, 0x98, 0x43, 0x80, 0x37, 0x46, 0x66, 0xbb, 0x8f, 0xfc, 0xb5, 0xbf, 0x40, 0xc2, 0x54, 0xb0,
            0x67, 0xd2, 0x7c, 0x51, 0xce, 0x4a, 0xd5, 0xfe, 0xd8, 0x29, 0xc9, 0x0b, 0x50, 0x5a, 0x57, 0x1b,
            0x7f, 0x4d, 0x1c, 0xad, 0x6a, 0x52, 0x3c, 0xda, 0x77, 0x0e, 0x67, 0xbc, 0xea, 0xaf, 0x7e, 0x89
        };
        uint8_t out[128];
        int i;

        scryptBlockMix(out, in, 1);

        for(i=0; i<128; ++i)
        {
            printf("%02x%c", (int)out[i], i%16==15?'\n':' ');
        }
        printf("\n");
    }
    {
        uint8_t in[128]=
        {
            0xf7, 0xce, 0x0b, 0x65, 0x3d, 0x2d, 0x72, 0xa4, 0x10, 0x8c, 0xf5, 0xab, 0xe9, 0x12, 0xff, 0xdd,
            0x77, 0x76, 0x16, 0xdb, 0xbb, 0x27, 0xa7, 0x0e, 0x82, 0x04, 0xf3, 0xae, 0x2d, 0x0f, 0x6f, 0xad,
            0x89, 0xf6, 0x8f, 0x48, 0x11, 0xd1, 0xe8, 0x7b, 0xcc, 0x3b, 0xd7, 0x40, 0x0a, 0x9f, 0xfd, 0x29,
            0x09, 0x4f, 0x01, 0x84, 0x63, 0x95, 0x74, 0xf3, 0x9a, 0xe5, 0xa1, 0x31, 0x52, 0x17, 0xbc, 0xd7,
            0x89, 0x49, 0x91, 0x44, 0x72, 0x13, 0xbb, 0x22, 0x6c, 0x25, 0xb5, 0x4d, 0xa8, 0x63, 0x70, 0xfb,
            0xcd, 0x98, 0x43, 0x80, 0x37, 0x46, 0x66, 0xbb, 0x8f, 0xfc, 0xb5, 0xbf, 0x40, 0xc2, 0x54, 0xb0,
            0x67, 0xd2, 0x7c, 0x51, 0xce, 0x4a, 0xd5, 0xfe, 0xd8, 0x29, 0xc9, 0x0b, 0x50, 0x5a, 0x57, 0x1b,
            0x7f, 0x4d, 0x1c, 0xad, 0x6a, 0x52, 0x3c, 0xda, 0x77, 0x0e, 0x67, 0xbc, 0xea, 0xaf, 0x7e, 0x89
        };
        uint8_t out[128];
        int i;

        scryptROMix(out, in, 1, 16, NULL);

        for(i=0; i<128; ++i)
        {
            printf("%02x%c", (int)out[i], i%16==15?'\n':' ');
        }
        printf("\n");
    }
    {
        uint8_t out[64];
        int i;

        scrypt(out, 64, (const uint8_t *)"", 0, (const uint8_t *)"", 0, 16, 1, 1);

        for(i=0; i<64; ++i)
        {
            printf("%02x%c", (int)out[i], i%16==15?'\n':' ');
        }
        printf("\n");
    }
    {
        uint8_t out[64];
        int i;

        scrypt(out, 64, (const uint8_t *)"password", 8, (const uint8_t *)"NaCl", 4, 1024, 8, 16);

        for(i=0; i<64; ++i)
        {
            printf("%02x%c", (int)out[i], i%16==15?'\n':' ');
        }
        printf("\n");
    }
    {
        uint8_t out[64];
        int i;

        scrypt(out, 64, (const uint8_t *)"pleaseletmein", 13, (const uint8_t *)"SodiumChloride", 14, 16384, 8, 1);

        for(i=0; i<64; ++i)
        {
            printf("%02x%c", (int)out[i], i%16==15?'\n':' ');
        }
        printf("\n");
    }
    {
        uint8_t out[64];
        int i;

        scrypt(out, 64, (const uint8_t *)"pleaseletmein", 13, (const uint8_t *)"SodiumChloride", 14, 1048576, 8, 1);

        for(i=0; i<64; ++i)
        {
            printf("%02x%c", (int)out[i], i%16==15?'\n':' ');
        }
        printf("\n");
    }

    return 0;
}

#endif
