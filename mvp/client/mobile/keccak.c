// Blashyrkh.maniac.coding
// Based on code by Guido Bertoni, Joan Daemen, Michaël Peeters,
// Gilles Van Assche and Ronny Van Keer

#include "keccak.h"
#include <string.h>


static inline uint64_t ROL64(uint64_t a, unsigned int offset)
{
    return (a<<offset)^(a>>(64-offset));
}

static int LFSR86540(uint8_t *LFSR)
{
    int result=((*LFSR)&0x01)!=0;
    if(((*LFSR)&0x80)!=0)
        (*LFSR)=((*LFSR)<<1)^0x71;
    else
        (*LFSR)<<=1;
    return result;
}

static void keccak_permute(uint64_t *state)
{
    unsigned int round, x, y, j, t;
    uint8_t LFSRstate=0x01;

    for(round=0; round<24; ++round)
    {
        {
            uint64_t C[5], D;

            for(x=0; x<5; ++x)
                C[x]=state[x]^state[x+5]^state[x+10]^state[x+15]^state[x+20];
            for(x=0; x<5; ++x)
            {
                D=C[(x+4)%5]^ROL64(C[(x+1)%5], 1);
                for(y=0; y<5; ++y)
                    state[x+5*y]^=D;
            }
        }

        {
            uint64_t current, temp;
            x=1;
            y=0;
            current=state[x+5*y];
            for(t=0; t<24; ++t)
            {
                unsigned int r=((t+1)*(t+2)/2)%64;
                unsigned int Y=(2*x+3*y)%5;
                x=y;
                y=Y;

                temp=state[x+5*y];
                state[x+5*y]=ROL64(current, r);
                current=temp;
            }
        }

        {
            uint64_t temp[5];
            for(y=0; y<5; ++y)
            {
                for(x=0; x<5; ++x)
                    temp[x]=state[x+5*y];
                for(x=0; x<5; ++x)
                    state[x+5*y]=temp[x]^((~temp[(x+1)%5])&temp[(x+2)%5]);
            }
        }

        {
            for(j=0; j<7; ++j)
            {
                unsigned int bitPosition=(1<<j)-1;
                if(LFSR86540(&LFSRstate))
                    state[0]^=1ULL<<bitPosition;
            }
        }
    }
}

void keccak_start(
    struct keccak_context *ctx,
    unsigned int           rate,
    unsigned int           hashbitlen,
    uint8_t                suffix)
{
    memset(ctx->state, 0, 200);
    ctx->rate=rate/8;
    ctx->hashsize=hashbitlen/8;
    ctx->suffix=suffix;
    ctx->pos=0;
}

void keccak_step(struct keccak_context *ctx, const uint8_t *in, size_t size)
{
    while(size>0)
    {
        ctx->state[ctx->pos]^=*in;
        ++ctx->pos;
        ++in;
        --size;

        if(ctx->pos==ctx->rate)
        {
            keccak_permute((uint64_t *)ctx->state);
            ctx->pos=0;
        }
    }
}

void keccak_finish(struct keccak_context *ctx, uint8_t *hash)
{
    size_t hashsize=ctx->hashsize;

    ctx->state[ctx->pos]^=ctx->suffix;
    if((ctx->suffix&0x80)!=0 && ctx->pos==ctx->rate-1)
        keccak_permute((uint64_t *)ctx->state);
    ctx->state[ctx->rate-1]^=0x80;
    keccak_permute((uint64_t *)ctx->state);

    while(hashsize>0)
    {
        size_t bs=hashsize;
        if(bs>ctx->rate)
            bs=ctx->rate;

        memcpy(hash, ctx->state, bs);
        hash+=bs;
        hashsize-=bs;

        if(hashsize>0)
            keccak_permute((uint64_t *)ctx->state);
    }
}

void keccak_simple(
    uint8_t       *hash,
    unsigned int   rate,
    unsigned int   hashbitlen,
    unsigned char  suffix,
    const uint8_t *in,
    size_t         size)
{
    struct keccak_context ctx;
    keccak_start(&ctx, rate, hashbitlen, suffix);
    keccak_step(&ctx, in, size);
    keccak_finish(&ctx, hash);
}

#ifdef TEST_KECCAK
#include <stdio.h>

int main()
{
    uint8_t hash[32];
    int i, j;

    keccak_simple(hash, 1088, 256, 0x06, (const uint8_t *)"abc", 3);
    for(i=0; i<32; ++i)
        printf("%02x", (int)hash[i]);
    printf("\n");

    keccak_simple(hash, 1088, 256, 0x06, (const uint8_t *)"", 0);
    for(i=0; i<32; ++i)
        printf("%02x", (int)hash[i]);
    printf("\n");

    keccak_simple(hash, 1088, 256, 0x06, (const uint8_t *)"abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq", 56);
    for(i=0; i<32; ++i)
        printf("%02x", (int)hash[i]);
    printf("\n");

    keccak_simple(hash, 1088, 256, 0x06, (const uint8_t *)"abcdefghbcdefghicdefghijdefghijkefghijklfghijklmghijklmnhijklmnoijklmnopjklmnopqklmnopqrlmnopqrsmnopqrstnopqrstu", 112);
    for(i=0; i<32; ++i)
        printf("%02x", (int)hash[i]);
    printf("\n");

    return 0;
}

#endif
