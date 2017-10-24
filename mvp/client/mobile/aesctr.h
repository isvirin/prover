// Blashyrkh.maniac.coding

#ifndef __AESCTR_H
#define __AESCTR_H

#include "aes.h"


struct aesctr_context
{
    struct aes_context aesctx;
    uint8_t            ctr[AES_BLOCK_SIZE];
    uint8_t            buf[AES_BLOCK_SIZE];
    unsigned int       bufoffset;
};

void aesctr_start(
    struct aesctr_context *ctx,
    const uint8_t         *key,
    size_t                 keylen,
    const uint8_t          iv[AES_BLOCK_SIZE]);
void aesctr_step(
    struct aesctr_context *ctx,
    uint8_t               *data,
    size_t                 size);
void aesctr_finish(
    struct aesctr_context *ctx);

#endif
