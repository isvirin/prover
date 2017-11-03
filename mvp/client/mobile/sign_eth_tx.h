// Blashyrkh.maniac.coding

#ifndef __SIGN_ETH_TX_H
#define __SIGN_ETH_TX_H

#include <stdint.h>
#include <stddef.h>
#include <sys/types.h>


ssize_t build_commitSwypeCode_tx(
    const uint8_t   nonce[32],
    const uint8_t   gasPrice[32],
    const uint8_t   contractAddress[20],
    const uint8_t   referenceBlockHash[32],
    const uint8_t   privkey[32],
    uint8_t       **pbuffer);

ssize_t build_commitMediaHash_tx(
    const uint8_t   nonce[32],
    const uint8_t   gasPrice[32],
    const uint8_t   contractAddress[20],
    const uint8_t   mediaHash[32],
    const uint8_t   swypeCodeTransactionHash[32],
    const uint8_t   privkey[32],
    uint8_t       **pbuffer);

#endif
