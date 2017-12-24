TARGET := searchqrcode
CFLAGS := -Wall -pipe
CXXFLAGS := $(CFLAGS) -std=c++11
# -O0 -ggdb
LFLAGS := -lavformat -lavcodec -lavutil -lavfilter -lpng -lzbar
OBJECTS := \
    .obj/searchqrcode.cpp.o \
    .obj/VideoFileReader.cpp.o \
    .obj/ImageProcessor.cpp.o

.PHONY: prepare all clean

all: prepare .bin/$(TARGET)

prepare:
	mkdir -p .obj .bin

.obj/%.cpp.o: %.cpp
	g++ -c $^ -o $@ $(CXXFLAGS)

.obj/%.c.o: %.c
	gcc -c $^ -o $@ $(CFLAGS)

.bin/$(TARGET): $(OBJECTS)
	g++ $^ -o $@ $(LFLAGS)

clean:
	rm -rf .obj .bin/$(TARGET)
	rmdir --ignore-fail-on-non-empty .bin
