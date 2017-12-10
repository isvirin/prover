TARGET := analyzefile
CXXFLAGS := -Wall -pipe -std=c++14
LFLAGS := -lavformat -lavcodec -lavutil -lopencv_features2d -lopencv_imgproc -lopencv_core -lpng
VPATH := . ../client/prover_mvp_android/app/src/main/cpp
CXXFLAGS += -I../client/prover_mvp_android/app/src/main/cpp

%.cpp.o: %.cpp
	g++ -c $^ -o $@ $(CXXFLAGS)

$(TARGET): analyzefile.cpp.o VideoFileReader.cpp.o swype_detect.cpp.o SwipeCircleDetector.cpp.o SwypeStepDetector.cpp.o Vector.cpp.o VectorExplained.cpp.o BoundsChecker.cpp.o
	g++ $^ -o $@ $(LFLAGS)
