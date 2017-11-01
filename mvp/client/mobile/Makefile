
CFLAGS := $(shell pkg-config --cflags json-c) -Isecp256k1/include
LIBS   := $(shell pkg-config --libs json-c)
TARGET_DEMOS := demo_eth_keystore demo_sign_tx
TARGET_LIBS  := eth_keystore.a sign_eth_tx.a

.PHONY: all libraries demos clean

all: libraries demos

libraries: $(TARGET_LIBS)

demos: $(TARGET_DEMOS)

secp256k1/.libs/libsecp256k1.a:
	cd secp256k1; ./autogen.sh && ./configure --enable-module-recovery && $(MAKE)

eth_keystore.a: aes.c.o aesctr.c.o hex2bin.c.o hmac_sha256.c.o keccak.c.o pbkdf2_sha256.c.o scrypt.c.o sha256.c.o eth_keystore.c.o
	ar r $@ $^

sign_eth_tx.a: keccak.c.o rlp.c.o sign_eth_tx.c.o
	ar r $@ $^

demo_eth_keystore: demo_eth_keystore.c.o eth_keystore.a
	gcc $(LIBS) -o $@ $^

demo_sign_tx: demo_sign_tx.c.o hex2bin.c.o sign_eth_tx.a secp256k1/.libs/libsecp256k1.a
	gcc $(LIBS) -lgmp -o $@ $^

%.c.o: %.c
	gcc -c $(CFLAGS) -o $@ $<


clean:
	rm -rf $(TARGET_DEMOS) $(TARGET_LIBS) *.c.o
	cd secp256k1; make distclean
