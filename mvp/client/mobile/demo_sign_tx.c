// Blashyrkh.maniac.coding

#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <secp256k1_recovery.h>
#include "hex2bin.h"
#include "sign_eth_tx.h"


int main(int argc, char *argv[])
{
    if(argc==6 && strcmp(argv[1], "requestSwypeCode")==0)
    {
        uint8_t nonce[32];
        uint8_t gasPrice[32];
        uint8_t contract[20];
        uint8_t privkey[32];
        uint8_t *result;
        ssize_t size;

        hex2bin_bigendian(argv[2], nonce, 32);
        hex2bin_bigendian(argv[3], gasPrice, 32);
        hex2bin(argv[4], contract, 20);
        hex2bin(argv[5], privkey, 32);

        size=build_requestSwypeCode_tx(nonce, gasPrice, contract, privkey, &result);
        if(size!=-1)
        {
            ssize_t i;
            printf("0x");
            for(i=0; i<size; ++i)
                printf("%02x", result[i]);
            printf("\n");
            free(result);
        }
    }
    else if(argc==8 && strcmp(argv[1], "commitMediaHash")==0)
    {
        uint8_t nonce[32];
        uint8_t gasPrice[32];
        uint8_t contract[20];
        uint8_t mediaHash[32];
        uint8_t swypeCodeTransactionHash[32];
        uint8_t privkey[32];
        uint8_t *result;
        ssize_t size;

        hex2bin_bigendian(argv[2], nonce, 32);
        hex2bin_bigendian(argv[3], gasPrice, 32);
        hex2bin(argv[4], contract, 20);
        hex2bin(argv[5], privkey, 32);
        hex2bin(argv[6], mediaHash, 32);
        hex2bin(argv[7], swypeCodeTransactionHash, 32);

        size=build_submitMediaHash_tx(nonce, gasPrice, contract, mediaHash, swypeCodeTransactionHash, privkey, &result);
        if(size!=-1)
        {
            ssize_t i;
            printf("0x");
            for(i=0; i<size; ++i)
                printf("%02x", result[i]);
            printf("\n");
            free(result);
        }
    }
    else
    {
        fprintf(
            stderr,
            "Usage:\n"
            "  %s requestSwypeCode NONCE GASPRICE CONTRACT-ADDRESS PRIVATE-KEY\n"
            "  %s submitMediaHash NONCE GASPRICE CONTRACT-ADDRESS PRIVATE-KEY MEDIA-HASH SWYPE-CODE-TX-HASH\n",
            argv[0],
            argv[0]);
        return 1;
    }


    return 0;
}
