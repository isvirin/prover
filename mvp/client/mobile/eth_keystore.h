// Blashyrkh.maniac.coding

#ifndef __ETH_KEYSTORE_H
#define __ETH_KEYSTORE_H

#include <stdint.h>


enum DecryptKeystoreResultCode
{
    Keystore_OK                 =  0,
    Keystore_WrongFormat        = -1,
    Keystore_UnsupportedVersion = -2,
    Keystore_UnsupportedCipher  = -3,
    Keystore_UnsupportedKdf     = -4
};

enum DecryptKeystoreResultCode decrypt_keystore_file(
    const char *filename,
    const char *password,
    uint8_t     address[20],
    uint8_t     privkey[32]);

#endif
