TARGET := analyzefile
CFLAGS := -Wall -pipe
CXXFLAGS := $(CFLAGS) -std=c++14
# -O0 -ggdb
LFLAGS := -lavformat -lavcodec -lavutil -lavfilter -lopencv_features2d -lopencv_imgproc -lopencv_core -lpng
VPATH := . ../client/prover_mvp_android/app/src/main/cpp
CXXFLAGS += -I../client/prover_mvp_android/app/src/main/cpp
OBJECTS := \
    .obj/analyzefile.cpp.o \
    .obj/VideoFileReader.cpp.o \
    .obj/ImageProcessor.cpp.o \
    .obj/swype_detect.cpp.o \
    .obj/SwipeCircleDetector.cpp.o \
	.obj/SwypeCodeDetector.cpp.o \
    .obj/SwypeStepDetector.cpp.o \
    .obj/Vector.cpp.o \
    .obj/VectorExplained.cpp.o \
    .obj/BoundsChecker.cpp.o \
    .obj/sha256.c.o

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
