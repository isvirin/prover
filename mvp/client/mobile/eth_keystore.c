// Blashyrkh.maniac.coding

#include "eth_keystore.h"
#include <string.h>
#include <json/json.h>
#include "scrypt.h"
#include "aesctr.h"
#include "keccak.h"


static int hex2bin(const char *hex, uint8_t *bin, unsigned int byteslen)
{
    static const int table[256]=
    {
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 0-15
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 16-31
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 32-47
         0,  1,  2,  3,  4,  5,  6,  7,  8,  9, -1, -1, -1, -1, -1, -1, // 48-63
        -1, 10, 11, 12, 13, 14, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 64-79
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 80-95
        -1, 10, 11, 12, 13, 14, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 96-111
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 112-127
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 128-143
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 144-159
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 160-175
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 176-191
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 192-207
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 208-223
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 224-239
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1  // 240-255
    };
    for(; byteslen>0; hex+=2, ++bin, --byteslen)
    {
        if(table[hex[0]]<0 || table[hex[1]]<0)
            return -1;

        *bin=table[hex[0]]*16+table[hex[1]];
    }
    return 0;
}

static enum DecryptKeystoreResultCode check_version(struct json_object *obj)
{
    struct json_object *jsonVersion;

    if(!json_object_object_get_ex(obj, "version", &jsonVersion))
        return Keystore_WrongFormat;

    if(!json_object_is_type(jsonVersion, json_type_int))
        return Keystore_WrongFormat;

    if(json_object_get_int(jsonVersion)!=3)
        return Keystore_UnsupportedVersion;

    return Keystore_OK;
}

static enum DecryptKeystoreResultCode get_account_address(
    struct json_object *obj,
    uint8_t             address[20])
{
    struct json_object *jsonAddress;
    int len;

    if(!json_object_object_get_ex(obj, "address", &jsonAddress))
        return Keystore_WrongFormat;

    if(!json_object_is_type(jsonAddress, json_type_string))
        return Keystore_WrongFormat;

    // 40 hex chars = 20 bytes ethereum address
    if((len=json_object_get_string_len(jsonAddress))!=40)
        return Keystore_WrongFormat;

    if(hex2bin(json_object_get_string(jsonAddress), address, 20)<0)
        return Keystore_WrongFormat; // wrong characters in the hex string

    return Keystore_OK;
}

static enum DecryptKeystoreResultCode decrypt_privkey(
    struct json_object *obj,
    const char         *password,
    uint8_t             privkey[32])
{
    // "Crypto" or "crypto" object should contain:
    // - s cipher     - name of cipher, only "aes-128-ctr" is allowed
    // - s kdf        - name of kdf, only "scrypt" is allowed
    // - s ciphertext - 64-chars hex string (32 bytes) of encrypted data
    // - s mac        - 64-chars hex string (32 bytes) of MAC
    // - o cipherparams
    //   - s iv       - 32-chars hex string (16 bytes) of initialization vector
    // - o kdfparams
    //   - s salt     - 64-chars hex string (32 bytes) of kdf salt
    //   - i dklen    - length of produced key sequence, should be 32 (bytes)
    //   - i n,r,p    - scrypt parameters

    enum DecryptKeystoreResultCode rc;
    struct json_object *jsonCrypto,
                       *jsonCipher,
                       *jsonKDF,
                       *jsonCipherText,
                       *jsonMAC,
                       *jsonCipherParams,
                       *jsonIV,
                       *jsonKDFParams,
                       *jsonSalt,
                       *jsonDKLen,
                       *jsonN,
                       *jsonR,
                       *jsonP;
    const char *ciphername, *kdfname;
    unsigned int saltlen;
    uint8_t *salt;
    struct scrypt_context kdf;
    uint8_t cipherkey[16], mackey[16];
    uint8_t iv[16];
    uint8_t filemac[32], realmac[32];
    struct keccak_context keccak;
    struct aesctr_context cipher;

    // Check presence and types of "cipher" and "kdf"
    if(!json_object_object_get_ex(obj, "crypto", &jsonCrypto) ||
       !json_object_is_type(jsonCrypto, json_type_object) ||
       !json_object_object_get_ex(jsonCrypto, "cipher", &jsonCipher) ||
       !json_object_is_type(jsonCipher, json_type_string) ||
       !json_object_object_get_ex(jsonCrypto, "kdf", &jsonKDF) ||
       !json_object_is_type(jsonKDF, json_type_string))
    {
        return Keystore_WrongFormat;
    }

    // Check algorithm names. We support AES-128-CTR and SCRYPT only
    ciphername=json_object_get_string(jsonCipher);
    if(!ciphername)
        return Keystore_WrongFormat;
    if(strcmp(ciphername, "aes-128-ctr")!=0)
        return Keystore_UnsupportedCipher;

    kdfname=json_object_get_string(jsonKDF);
    if(!kdfname)
        return Keystore_WrongFormat;
    if(strcmp(kdfname, "scrypt")!=0)
        return Keystore_UnsupportedKdf;

    // Check presence and types of others
    if(!json_object_object_get_ex(jsonCrypto, "ciphertext", &jsonCipherText) ||
       !json_object_is_type(jsonCipherText, json_type_string) ||
       !json_object_object_get_ex(jsonCrypto, "mac", &jsonMAC) ||
       !json_object_is_type(jsonMAC, json_type_string) ||
       !json_object_object_get_ex(jsonCrypto, "cipherparams", &jsonCipherParams) ||
       !json_object_is_type(jsonCipherParams, json_type_object) ||
       !json_object_object_get_ex(jsonCipherParams, "iv", &jsonIV) ||
       !json_object_is_type(jsonIV, json_type_string) ||
       !json_object_object_get_ex(jsonCrypto, "kdfparams", &jsonKDFParams) ||
       !json_object_is_type(jsonKDFParams, json_type_object) ||
       !json_object_object_get_ex(jsonKDFParams, "salt", &jsonSalt) ||
       !json_object_is_type(jsonSalt, json_type_string) ||
       !json_object_object_get_ex(jsonKDFParams, "dklen", &jsonDKLen) ||
       !json_object_is_type(jsonDKLen, json_type_int) ||
       !json_object_object_get_ex(jsonKDFParams, "n", &jsonN) ||
       !json_object_is_type(jsonN, json_type_int) ||
       !json_object_object_get_ex(jsonKDFParams, "r", &jsonR) ||
       !json_object_is_type(jsonR, json_type_int) ||
       !json_object_object_get_ex(jsonKDFParams, "p", &jsonP) ||
       !json_object_is_type(jsonP, json_type_int))
    {
        return Keystore_WrongFormat;
    }

    // Generate key sequence
    saltlen=json_object_get_string_len(jsonSalt)/2;
    salt=(uint8_t *)alloca(saltlen);
    hex2bin(json_object_get_string(jsonSalt), salt, saltlen);

    scrypt_start(
        &kdf,
        password,
        strlen(password),
        salt,
        saltlen,
        json_object_get_int(jsonN),
        json_object_get_int(jsonR),
        json_object_get_int(jsonP));
    scrypt_step(&kdf, cipherkey, 16);
    scrypt_step(&kdf, mackey, 16);
    scrypt_finish(&kdf);

    // Read cipher text
    hex2bin(json_object_get_string(jsonCipherText), privkey, 32);

    // Check MAC
    keccak_start(&keccak, 1088, 256, 0x01);
    keccak_step(&keccak, mackey, 16);
    keccak_step(&keccak, privkey, 32);
    keccak_finish(&keccak, realmac);

    hex2bin(json_object_get_string(jsonMAC), filemac, 32);

    if(memcmp(filemac, realmac, 32)!=0)
        return Keystore_AuthFailed;

    // Decrypt
    hex2bin(json_object_get_string(jsonIV), iv, 16);
    aesctr_start(&cipher, cipherkey, 16, iv);
    aesctr_step(&cipher, privkey, 32);
    aesctr_finish(&cipher);

    return Keystore_OK;
}

static enum DecryptKeystoreResultCode decrypt_keystore_json(
    struct json_object *obj,
    const char         *password,
    uint8_t             address[20],
    uint8_t             privkey[32])
{
    enum DecryptKeystoreResultCode rc;

    if((rc=check_version(obj))<0)
        return rc;

    if((rc=get_account_address(obj, address))<0)
        return rc;

    if((rc=decrypt_privkey(obj, password, privkey))<0)
        return rc;

    return Keystore_OK;
}

enum DecryptKeystoreResultCode decrypt_keystore_file(
    const char *filename,
    const char *password,
    uint8_t     address[20],
    uint8_t     privkey[32])
{
    struct json_object *obj;
    enum DecryptKeystoreResultCode rc;

    obj=json_object_from_file(filename);
    if(!obj)
        return Keystore_WrongFormat;

    rc=decrypt_keystore_json(obj, password, address, privkey);

    json_object_put(obj);

    return rc;
}
