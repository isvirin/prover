// Blashyrkh.maniac.coding

#ifndef __SCRYPT_H
#define __SCRYPT_H

#include <stdint.h>
#include <stddef.h>
#include "pbkdf2_sha256.h"


struct scrypt_context
{
    struct pbkdf2_sha256_context  pbkdf2ctx;
};

void scrypt_start(
    struct scrypt_context *ctx,
    const uint8_t         *password,
    size_t                 passwordlen,
    const uint8_t         *salt,
    size_t                 saltlen,
    unsigned int           n,
    unsigned int           r,
    unsigned int           p);
void scrypt_step(
    struct scrypt_context *ctx,
    uint8_t               *out,
    size_t                 count);
void scrypt_finish(
    struct scrypt_context *ctx);

#endif
