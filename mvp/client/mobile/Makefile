
CFLAGS += $(shell pkg-config --cflags json-c)
LIBS += $(shell pkg-config --libs json-c)


all: demo_eth_keystore


eth_keystore.a: aes.c.o aesctr.c.o hex2bin.c.o hmac_sha256.c.o keccak.c.o pbkdf2_sha256.c.o scrypt.c.o sha256.c.o eth_keystore.c.o
	ar r $@ $^

demo_eth_keystore: demo_eth_keystore.c.o eth_keystore.a
	gcc $(LIBS) -o $@ $^

#proof_swypecode.a: 


%.c.o: %.c
	gcc -c $(CFLAGS) -o $@ $<

