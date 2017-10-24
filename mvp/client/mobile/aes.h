// Blashyrkh.maniac.coding

#ifndef __AES_H
#define __AES_H

#include <stdint.h>
#include <stddef.h>


enum
{
    AES_BLOCK_SIZE = 16
};

struct aes_context
{
    uint32_t     key[60];
    unsigned int keylen;
};

void aes_encrypt_start(struct aes_context *ctx, const uint8_t *key, unsigned int keylen);
void aes_encrypt_step(struct aes_context *ctx, uint8_t out[16], const uint8_t in[16]);
void aes_encrypt_finish(struct aes_context *ctx);

void aes_decrypt_start(struct aes_context *ctx, const uint8_t *key, unsigned int keylen);
void aes_decrypt_step(struct aes_context *ctx, uint8_t out[16], const uint8_t in[16]);
void aes_decrypt_finish(struct aes_context *ctx);

#endif
