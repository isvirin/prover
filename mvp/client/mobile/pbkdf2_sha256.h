// Blashyrkh.maniac.coding

#ifndef __PBKDF2_SHA256_H
#define __PBKDF2_SHA256_H

#include <stdint.h>
#include <stddef.h>


struct pbkdf2_sha256_context
{
    uint8_t     *password;
    size_t       passwordlen;
    uint8_t     *salt;
    size_t       saltlen;
    uint8_t      buffer[32];
    size_t       bufferlen;
    unsigned int i;
    unsigned int c;
};

void pbkdf2_sha256_start(
    struct pbkdf2_sha256_context *ctx,
    const uint8_t                *password,
    size_t                        passwordlen,
    const uint8_t                *salt,
    size_t                        saltlen,
    unsigned int                  c);
void pbkdf2_sha256_step(
    struct pbkdf2_sha256_context *ctx,
    uint8_t                      *out,
    size_t                        count);
void pbkdf2_sha256_finish(
    struct pbkdf2_sha256_context *ctx);
void pbkdf2_sha256_simple(
    uint8_t                      *out,
    size_t                        count,
    const uint8_t                *password,
    size_t                        passwordlen,
    const uint8_t                *salt,
    size_t                        saltlen,
    unsigned int                  c);

#endif
