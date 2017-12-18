// Blashyrkh.maniac.coding

#include "sign_eth_tx.h"
#include <string.h>
#include <stdlib.h>
#include <secp256k1.h>
#include <secp256k1_recovery.h>
#include "rlp.h"
#include "keccak.h"


static ssize_t build_tx(
    const uint8_t   nonce[32],
    const uint8_t   gasPrice[32],
    const uint8_t   contractAddress[20],
    const uint8_t  *data,
    size_t          datasize,
    const uint8_t   privkey[32],
    uint8_t       **pbuffer)
{
    struct rlp_item *Ls, *Lt;
    uint8_t *buffer;
    size_t size;
    uint8_t hash[32];
    secp256k1_context *secpctx;
    secp256k1_ecdsa_recoverable_signature signature;
    uint8_t rs[64];
    int recoverId;

    Ls=rlp_create_list_item();
    rlp_list_append_item(Ls, rlp_create_be_int_item(nonce, 32));
    rlp_list_append_item(Ls, rlp_create_be_int_item(gasPrice, 32));
    rlp_list_append_item(Ls, rlp_create_be_int_item((const uint8_t *)"\x0f\x42\x40", 3)); // Fixed 1000000 amount
    rlp_list_append_item(Ls, rlp_create_be_int_item(contractAddress, 20));
    rlp_list_append_item(Ls, rlp_create_be_int_item((const uint8_t *)"", 0)); // Zero value
    rlp_list_append_item(Ls, rlp_create_string_item(data, datasize));

    size=rlp_serialize(Ls, &buffer);
    keccak_simple(hash, 1088, 256, 0x01, buffer, size);
    free(buffer);

    rlp_put_item(Ls);

    secpctx=secp256k1_context_create(SECP256K1_CONTEXT_SIGN);

    while(1)
    {
        uint8_t entropy[32];
        int i;

        for(i=0; i<32; ++i)
            entropy[i]=random()%256;

        secp256k1_ecdsa_sign_recoverable(
            secpctx,
            &signature,
            (const unsigned char *)hash,
            (const unsigned char *)privkey,
            secp256k1_nonce_function_rfc6979,
            entropy);
        secp256k1_ecdsa_recoverable_signature_serialize_compact(
            secpctx,
            rs,
            &recoverId,
            &signature);

        if(recoverId==0 || recoverId==1)
            break;
    }

    secp256k1_context_destroy(secpctx);

    Lt=rlp_create_list_item();
    rlp_list_append_item(Lt, rlp_create_be_int_item(nonce, 32));
    rlp_list_append_item(Lt, rlp_create_be_int_item(gasPrice, 32));
    rlp_list_append_item(Lt, rlp_create_be_int_item((const uint8_t *)"\x0f\x42\x40", 3)); // Fixed 1000000 amount
    rlp_list_append_item(Lt, rlp_create_be_int_item(contractAddress, 20));
    rlp_list_append_item(Lt, rlp_create_be_int_item((const uint8_t *)"", 0)); // Zero value
    rlp_list_append_item(Lt, rlp_create_string_item(data, datasize));
    rlp_list_append_item(Lt, rlp_create_be_int_item((const uint8_t *)(recoverId?"\x1c":"\x1b"), 1));
    rlp_list_append_item(Lt, rlp_create_be_int_item(rs+0, 32));
    rlp_list_append_item(Lt, rlp_create_be_int_item(rs+32, 32));

    return rlp_serialize(Ls, pbuffer);
}

ssize_t build_requestSwypeCode_tx(
    const uint8_t   nonce[32],
    const uint8_t   gasPrice[32],
    const uint8_t   contractAddress[20],
    const uint8_t   privkey[32],
    uint8_t       **pbuffer)
{
    uint8_t data[4];
    memcpy(data, "\x74\x30\x5b\x38", 4); // Keccak("requestSwypeCode()")[0:4]

    return build_tx(
        nonce,
        gasPrice,
        contractAddress,
        data,
        sizeof(data),
        privkey,
        pbuffer);
}

ssize_t build_submitMediaHash_tx(
    const uint8_t   nonce[32],
    const uint8_t   gasPrice[32],
    const uint8_t   contractAddress[20],
    const uint8_t   mediaHash[32],
    const uint8_t   swypeCodeTransactionHash[32],
    const uint8_t   privkey[32],
    uint8_t       **pbuffer)
{
    uint8_t data[4+32+32];
    memcpy(data+0, "\xa0\xee\x3e\xcf", 4); // Keccak("submitMediaHash(bytes32,bytes32)")[0:4]
    memcpy(data+4, mediaHash, 32);
    memcpy(data+36, swypeCodeTransactionHash, 32);

    return build_tx(
        nonce,
        gasPrice,
        contractAddress,
        data,
        sizeof(data),
        privkey,
        pbuffer);
}

ssize_t build_submitMessage_tx(
    const uint8_t   nonce[32],
    const uint8_t   gasPrice[32],
    const uint8_t   contractAddress[20],
    const char     *message,
    const uint8_t   privkey[32],
    uint8_t       **pbuffer)
{
    size_t l=strlen(message);
    size_t datasize=4+32+32+(l+31)/32*32;
    uint8_t *data=(uint8_t *)calloc(datasize, 1);

    memcpy(data+0, "\x70\x8b\x34\xfe", 4); // Keccak("submitMessage(string)")[0:4]
    data[4+31]=0x20;
    data[4+60]=(l>>24)&0xFF;
    data[4+61]=(l>>16)&0xFF;
    data[4+62]=(l>> 8)&0xFF;
    data[4+63]=(l    )&0xFF;
    memcpy(data+(4+64), message, l);

    ssize_t res=build_tx(
        nonce,
        gasPrice,
        contractAddress,
        data,
        datasize,
        privkey,
        pbuffer);

    free(data);
    return res;
}
