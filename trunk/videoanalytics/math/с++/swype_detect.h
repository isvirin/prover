#pragma once


#include <opencv2\opencv.hpp>
#include <vector>
#include <cmath>
#include <ctime>
#include <cstdlib>
#include <cstring>


class SwypeDetect
{
public:
	
	SwypeDetect();
	~SwypeDetect();
	
	void init(int fps_e, std::string swype);
	void setSwype(std::string swype);  // setting the swype code
	void processFrame(cv::Mat frame, int &state, int &index, int &x, int &y);   // a frame processing
	// frame - pointer to a buffer with a frame
	// state - state S
	// index - if state==2, the index  of the last entered swype number
	// x - if state==2, the X coordinate for visualisation
	// y - if state==2, the Y coordinate for visualisation
private:
	
	//External data
	std::vector<int> swype_Numbers; //we have swype code or we will wait swype code
	int fps; //fps frame
	
	//Internal data
	cv::Mat frame1; //previous frame
	cv::Mat frame2; //current frame
	std::vector<double> deltaXX; 
	std::vector<double> deltaYY; 

	
	
	int width; //frame width
	int height; //frame height
	int S; //state S
	int call; //Number of the frame processing function calls 
	int count_num; //Number of the correctly entered swype-numbers
	int count_direction; // we count 3 same direction for enter swype-number
	std::vector<int> Swype_Numbers_Get; //the entered numbers of the swype code
	std::vector<cv::Point2i> Swype_Koord; //the coordinates of the entered swype code
	std::vector<int> DirectionS; //directions array
	
	double deltaX;
	double deltaY;
	int Direction;

	time_t seconds_1;
	time_t seconds_2;
	long frm_count;
	
	std::vector<double> x_corr(void);
	int CircleDetection(void);
	std::vector<cv::Point2i> Koord_Swipe_Points(int width, int height);
	void Delta_Calculation(cv::Point2i output, int k);
	void Swype_Data(std::vector<cv::Point2i>& koord);
	void Reset(void);
};

