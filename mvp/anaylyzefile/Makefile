TARGET := analyzefile
CXXFLAGS := -Wall -pipe
LFLAGS := -lavformat -lavcodec -lavutil -lopencv_features2d -lopencv_imgproc -lopencv_core

%.cpp.o: %.cpp
	g++ -c $^ -o $@ $(CXXFLAGS)

$(TARGET): analyzefile.cpp.o swype_detect.cpp.o
	g++ $^ -o $@ $(LFLAGS)
