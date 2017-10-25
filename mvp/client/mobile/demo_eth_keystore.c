// Blashyrkh.maniac.coding

#include <stdio.h>
#include <unistd.h>
#include "eth_keystore.h"


int main(int argc, char *argv[])
{
    char *password;
    uint8_t address[20];
    uint8_t privkey[32];
    enum DecryptKeystoreResultCode rc;
    unsigned int i;

    if(argc<=1)
        return 1;

    password=getpass("Unlock account: ");

    rc=decrypt_keystore_file(argv[1], password, address, privkey);
    if(rc<0)
    {
        switch(rc)
        {
        case Keystore_WrongFormat:
            fprintf(stderr, "Wrong keystore format\n");
            break;
        case Keystore_UnsupportedVersion:
            fprintf(stderr, "Unsupported format version\n");
            break;
        case Keystore_UnsupportedCipher:
            fprintf(stderr, "Unsupported cipher\n");
            break;
        case Keystore_UnsupportedKdf:
            fprintf(stderr, "Unsupported key derivation function\n");
            break;
        case Keystore_AuthFailed:
            fprintf(stderr, "Invalid password\n");
            break;
        default:
            fprintf(stderr, "Unexpected error %d\n", (int)rc);
            break;
        }
        return 2;
    }

    printf("Address: 0x");
    for(i=0; i<20; ++i)
        printf("%02x", (unsigned int)address[i]);
    printf("\n");

    printf("Private key: 0x");
    for(i=0; i<32; ++i)
        printf("%02x", (unsigned int)privkey[i]);
    printf("\n");

    return 0;
}
