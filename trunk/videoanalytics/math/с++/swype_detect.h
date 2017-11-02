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
	void setSwype(std::string swype);
	void processFrame(cv::Mat frame, int &state, int &index, int &x, int &y); 

private:
	
	//External data
	std::vector<int> swype_Numbers; 
	int fps;
	
	//Internal data
	cv::Mat frame1; //previous frame
	cv::Mat frame2; //current frame
	std::vector<double> deltaXX; //динамический массив перемещений камеры по X
	std::vector<double> deltaYY; //динамический массив перемещений камеры по Y

	
	
	int width; //Ширина фрейма
	int height; //Высота фрейма
	int S; //Текущий этап распознования
	int call; //Колличество вызовов функции обработки кадров
	int count_num; //Колличество правильно введенных свайп-цифр
	int count_direction; //считаем три смещения для ввода свайпа
	std::vector<int> Swype_Numbers_Get; //Введеные цифры свайпкода
	std::vector<cv::Point2i> Swype_Koord; //Координаты введенного свайпкода
	std::vector<int> DirectionS; //Массив направлений
	
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

