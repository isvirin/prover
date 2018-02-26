TARGET := searchqrcode
CFLAGS := -Wall -pipe
CXXFLAGS := $(CFLAGS) -std=c++11 $(shell pkg-config --cflags libjpeg) $(shell pkg-config --cflags libpng) $(shell pkg-config --cflags zbar) -O0 -ggdb
LFLAGS := -lavformat -lavcodec -lavutil -lavfilter $(shell pkg-config --libs libjpeg) $(shell pkg-config --libs libpng) $(shell pkg-config --libs zbar)
OBJECTS := \
    .obj/searchqrcode.cpp.o \
    .obj/VideoFileReader.cpp.o \
    .obj/ImageProcessor.cpp.o \
    .obj/Analyzer.cpp.o \
    .obj/VideoAnalyzer.cpp.o \
    .obj/JpegAnalyzer.cpp.o

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
