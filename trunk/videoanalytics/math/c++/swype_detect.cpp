#include "swype_detect.h"

int SwypeDetect::CircleDetection(void)  // circle detection algorithm
{
	std::vector<double> C;
	int lenth_deltaXX = (int)deltaXX.size();
	for (int i = 0; i < lenth_deltaXX; i++) {
		if ((deltaXX[i] > frame2.cols) && ((deltaYY[i] > frame2.rows))) {
			if ((deltaXX[i] < (frame2.cols / 2)) && (deltaYY[i] < (frame2.rows / 2))) {
				C = x_corr();
				for (int j = 0; j <= i; j++) {
					if (fabs(C[j]) > 0.75) return 1;
				}
			}
		}
	}
	return 0;
}

std::vector<double> SwypeDetect::x_corr(void)  // correlation calculation algorithm
{
	std::vector<double> Result;
	cv::matchTemplate(deltaXX, deltaYY, Result, CV_TM_CCOEFF_NORMED);
	return Result;
}


std::vector<cv::Point2i> SwypeDetect::Koord_Swipe_Points(int width, int height)  // coordinates of the swype points
{
	std::vector<cv::Point2i> Result(9);

	Result[0].x = (int)floor(2 * width / 8);
	Result[0].y = (int)floor(2 * height / 8);

	Result[1].x = (int)floor(4 * width / 8);
	Result[1].y = (int)floor(2 * height / 8);

	Result[2].x = (int)floor(6 * width / 8);
	Result[2].y = (int)floor(2 * height / 8);

	Result[3].x = (int)floor(2 * width / 8);
	Result[3].y = (int)floor(4 * height / 8);

	Result[4].x = (int)floor(4 * width / 8);
	Result[4].y = (int)floor(4 * height / 8);

	Result[5].x = (int)floor(6 * width / 8);
	Result[5].y = (int)floor(4 * height / 8);

	Result[6].x = (int)floor(2 * width / 8);
	Result[6].y = (int)floor(6 * height / 8);

	Result[7].x = (int)floor(4 * width / 8);
	Result[7].y = (int)floor(6 * height / 8);

	Result[8].x = (int)floor(6 * width / 8);
	Result[8].y = (int)floor(6 * height / 8);

	return Result;
}

void SwypeDetect::Delta_Calculation(cv::Point2i output, int k) // offsets calculation for frames
{
	double Mean_Alfa;
	double K;
	
	double rez_vec_2_x;
	double rez_vec_2_y;
	double rez_vec_1_x;
	double rez_vec_1_y;
	double pi = 3.1415926535897932384626433832795;

	deltaX = output.x;
	deltaY = output.y;

	if (k == 0) {
		deltaXX.push_back(deltaX);
		deltaYY.push_back(deltaY);
	}
	else {
		deltaXX.push_back(deltaXX[k - 1] + deltaX);
		deltaYY.push_back(deltaYY[k - 1] + deltaY);
	}

	rez_vec_2_x = 0;
	rez_vec_2_y = 0;
	rez_vec_1_x = floor(deltaX);
	rez_vec_1_y = floor(deltaY);

	K = (rez_vec_2_y - rez_vec_1_y) / ((rez_vec_2_x - rez_vec_1_x) + 0.000001);


	// 1
	if ((rez_vec_2_x >= rez_vec_1_x) && (rez_vec_2_y >= rez_vec_1_y)) Mean_Alfa = floor((atan((K)) * 180 / pi));
	// 2
	if ((rez_vec_1_x>rez_vec_2_x) && (rez_vec_2_y >= rez_vec_1_y)) Mean_Alfa = 180 - abs(floor((atan((K)) * 180 / pi)));
	//3
	if ((rez_vec_1_x >= rez_vec_2_x) && (rez_vec_1_y>rez_vec_2_y)) Mean_Alfa = 180 + abs(floor((atan((K)) * 180 / pi)));
	//4
	if ((rez_vec_2_x>rez_vec_1_x) && (rez_vec_1_y>rez_vec_2_y)) Mean_Alfa = 360 - abs(floor((atan((K)) * 180 / pi)));
	//
	if (((Mean_Alfa >= 337) && (Mean_Alfa <= 360)) || ((Mean_Alfa >= 0) && (Mean_Alfa<22.5))) Direction = 5;
	if ((Mean_Alfa >= 22.5) && (Mean_Alfa<67.5)) Direction = 6;
	if ((Mean_Alfa >= 67.5) && (Mean_Alfa<112.5)) Direction = 7;
	if ((Mean_Alfa >= 112.5) && (Mean_Alfa<157.5)) Direction = 8;
	if ((Mean_Alfa >= 157.5) && (Mean_Alfa<202.5)) Direction = 1;
	if ((Mean_Alfa >= 202.5) && (Mean_Alfa<247.5)) Direction = 2;
	if ((Mean_Alfa >= 247.5) && (Mean_Alfa<292.5)) Direction = 3;
	if ((Mean_Alfa >= 292.5) && (Mean_Alfa<337.5)) Direction = 4;
}

void SwypeDetect::Swype_Data(std::vector<cv::Point2i>& koord)  // logic for entering swype numbers
{

	
	switch (DirectionS[count_direction]) {

	case 1:
		switch (Swype_Numbers_Get[count_num - 1]) {
		case 1:
			Swype_Numbers_Get[count_num] = 4;
			Swype_Koord[count_num].x = koord[4].x;
			Swype_Koord[count_num].y = koord[4].y;
			break;
		case 2:
			Swype_Numbers_Get[count_num] = 5;
			Swype_Koord[count_num].x = koord[5].x;
			Swype_Koord[count_num].y = koord[5].y;
			break;
		case 3:
			Swype_Numbers_Get[count_num] = 6;
			Swype_Koord[count_num].x = koord[6].x;
			Swype_Koord[count_num].y = koord[6].y;
			break;
		case 4:
			Swype_Numbers_Get[count_num] = 7;
			Swype_Koord[count_num].x = koord[7].x;
			Swype_Koord[count_num].y = koord[7].y;
			break;
		case 5:
			Swype_Numbers_Get[count_num] = 8;
			Swype_Koord[count_num].x = koord[8].x;
			Swype_Koord[count_num].y = koord[8].y;
			break;
		case 6:
			Swype_Numbers_Get[count_num] = 9;
			Swype_Koord[count_num].x = koord[9].x;
			Swype_Koord[count_num].y = koord[9].y;
			break;
		default:
			Swype_Numbers_Get[count_num] = 0;
			Swype_Koord[count_num].x = 0;
			Swype_Koord[count_num].y = 0;
		}
		break;
	case 2:
		switch (Swype_Numbers_Get[count_num - 1]) {
		case 1:
			Swype_Numbers_Get[count_num] = 5;
			Swype_Koord[count_num].x = koord[5].x;
			Swype_Koord[count_num].y = koord[5].y;
			break;
		case 2:
			Swype_Numbers_Get[count_num] = 6;
			Swype_Koord[count_num].x = koord[6].x;
			Swype_Koord[count_num].y = koord[6].y;
			break;
		case 4:
			Swype_Numbers_Get[count_num] = 8;
			Swype_Koord[count_num].x = koord[8].x;
			Swype_Koord[count_num].y = koord[8].y;
			break;
		case 5:
			Swype_Numbers_Get[count_num] = 9;
			Swype_Koord[count_num].x = koord[9].x;
			Swype_Koord[count_num].y = koord[9].y;
			break;
		default:
			Swype_Numbers_Get[count_num] = 0;
			Swype_Koord[count_num].x = 0;
			Swype_Koord[count_num].y = 0;
		}
		break;
	case 3:
		switch (Swype_Numbers_Get[count_num - 1]) {
		case 1:
			Swype_Numbers_Get[count_num] = 2;
			Swype_Koord[count_num].x = koord[2].x;
			Swype_Koord[count_num].y = koord[2].y;
			break;
		case 2:
			Swype_Numbers_Get[count_num] = 3;
			Swype_Koord[count_num].x = koord[3].x;
			Swype_Koord[count_num].y = koord[3].y;
			break;
		case 4:
			Swype_Numbers_Get[count_num] = 5;
			Swype_Koord[count_num].x = koord[5].x;
			Swype_Koord[count_num].y = koord[5].y;
			break;
		case 5:
			Swype_Numbers_Get[count_num] = 6;
			Swype_Koord[count_num].x = koord[6].x;
			Swype_Koord[count_num].y = koord[6].y;
			break;
		case 7:
			Swype_Numbers_Get[count_num] = 8;
			Swype_Koord[count_num].x = koord[8].x;
			Swype_Koord[count_num].y = koord[8].y;
			break;
		case 8:
			Swype_Numbers_Get[count_num] = 9;
			Swype_Koord[count_num].x = koord[9].x;
			Swype_Koord[count_num].y = koord[9].y;
			break;
		default:
			Swype_Numbers_Get[count_num] = 0;
			Swype_Koord[count_num].x = 0;
			Swype_Koord[count_num].y = 0;
		}
		break;
	case 4:
		switch (Swype_Numbers_Get[count_num - 1]) {
		case 4:
			Swype_Numbers_Get[count_num] = 2;
			Swype_Koord[count_num].x = koord[2].x;
			Swype_Koord[count_num].y = koord[2].y;
			break;
		case 5:
			Swype_Numbers_Get[count_num] = 3;
			Swype_Koord[count_num].x = koord[3].x;
			Swype_Koord[count_num].y = koord[3].y;
			break;
		case 7:
			Swype_Numbers_Get[count_num] = 5;
			Swype_Koord[count_num].x = koord[5].x;
			Swype_Koord[count_num].y = koord[5].y;
			break;
		case 8:
			Swype_Numbers_Get[count_num] = 6;
			Swype_Koord[count_num].x = koord[6].x;
			Swype_Koord[count_num].y = koord[6].y;
			break;
		default:
			Swype_Numbers_Get[count_num] = 0;
			Swype_Koord[count_num].x = 0;
			Swype_Koord[count_num].y = 0;
		}
		break;
	case 5:
		switch (Swype_Numbers_Get[count_num - 1]) {
		case 4:
			Swype_Numbers_Get[count_num] = 1;
			Swype_Koord[count_num].x = koord[1].x;
			Swype_Koord[count_num].y = koord[1].y;
			break;
		case 5:
			Swype_Numbers_Get[count_num] = 2;
			Swype_Koord[count_num].x = koord[2].x;
			Swype_Koord[count_num].y = koord[2].y;
			break;
		case 6:
			Swype_Numbers_Get[count_num] = 3;
			Swype_Koord[count_num].x = koord[3].x;
			Swype_Koord[count_num].y = koord[3].y;
			break;
		case 7:
			Swype_Numbers_Get[count_num] = 4;
			Swype_Koord[count_num].x = koord[4].x;
			Swype_Koord[count_num].y = koord[4].y;
			break;
		case 8:
			Swype_Numbers_Get[count_num] = 5;
			Swype_Koord[count_num].x = koord[5].x;
			Swype_Koord[count_num].y = koord[5].y;
			break;
		case 9:
			Swype_Numbers_Get[count_num] = 6;
			Swype_Koord[count_num].x = koord[6].x;
			Swype_Koord[count_num].y = koord[6].y;
			break;
		default:
			Swype_Numbers_Get[count_num] = 0;
			Swype_Koord[count_num].x = 0;
			Swype_Koord[count_num].y = 0;
		}
		break;
	case 6:
		switch (Swype_Numbers_Get[count_num - 1]) {
		case 5:
			Swype_Numbers_Get[count_num] = 1;
			Swype_Koord[count_num].x = koord[1].x;
			Swype_Koord[count_num].y = koord[1].y;
			break;
		case 6:
			Swype_Numbers_Get[count_num] = 2;
			Swype_Koord[count_num].x = koord[2].x;
			Swype_Koord[count_num].y = koord[2].y;
			break;
		case 8:
			Swype_Numbers_Get[count_num] = 4;
			Swype_Koord[count_num].x = koord[4].x;
			Swype_Koord[count_num].y = koord[4].y;
			break;
		case 9:
			Swype_Numbers_Get[count_num] = 5;
			Swype_Koord[count_num].x = koord[5].x;
			Swype_Koord[count_num].y = koord[5].y;
			break;
		default:
			Swype_Numbers_Get[count_num] = 0;
			Swype_Koord[count_num].x = 0;
			Swype_Koord[count_num].y = 0;
		}
		break;
	case 7:
		switch (Swype_Numbers_Get[count_num - 1]) {
		case 2:
			Swype_Numbers_Get[count_num] = 1;
			Swype_Koord[count_num].x = koord[1].x;
			Swype_Koord[count_num].y = koord[1].y;
			break;
		case 3:
			Swype_Numbers_Get[count_num] = 2;
			Swype_Koord[count_num].x = koord[2].x;
			Swype_Koord[count_num].y = koord[2].y;
			break;
		case 5:
			Swype_Numbers_Get[count_num] = 4;
			Swype_Koord[count_num].x = koord[4].x;
			Swype_Koord[count_num].y = koord[4].y;
			break;
		case 6:
			Swype_Numbers_Get[count_num] = 5;
			Swype_Koord[count_num].x = koord[5].x;
			Swype_Koord[count_num].y = koord[5].y;
			break;
		case 8:
			Swype_Numbers_Get[count_num] = 7;
			Swype_Koord[count_num].x = koord[7].x;
			Swype_Koord[count_num].y = koord[7].y;
			break;
		case 9:
			Swype_Numbers_Get[count_num] = 8;
			Swype_Koord[count_num].x = koord[8].x;
			Swype_Koord[count_num].y = koord[8].y;
			break;
		default:
			Swype_Numbers_Get[count_num] = 0;
			Swype_Koord[count_num].x = 0;
			Swype_Koord[count_num].y = 0;
		}
		break;
	case 8:
		switch (Swype_Numbers_Get[count_num - 1]) {
		case 2:
			Swype_Numbers_Get[count_num] = 4;
			Swype_Koord[count_num].x = koord[4].x;
			Swype_Koord[count_num].y = koord[4].y;
			break;
		case 3:
			Swype_Numbers_Get[count_num] = 5;
			Swype_Koord[count_num].x = koord[5].x;
			Swype_Koord[count_num].y = koord[5].y;
			break;
		case 5:
			Swype_Numbers_Get[count_num] = 7;
			Swype_Koord[count_num].x = koord[7].x;
			Swype_Koord[count_num].y = koord[7].y;
			break;
		case 6:
			Swype_Numbers_Get[count_num] = 8;
			Swype_Koord[count_num].x = koord[8].x;
			Swype_Koord[count_num].y = koord[8].y;
			break;
		default:
			Swype_Numbers_Get[count_num] = 0;
			Swype_Koord[count_num].x = 0;
			Swype_Koord[count_num].y = 0;
		}
		break;
	}
}

SwypeDetect::SwypeDetect() // initialization
{
	call = 0;
	count_direction = -1;
	count_num = -1;
	S = 0;
	width = 0;
	height = 0;
	Swype_Numbers_Get.clear();
	Swype_Koord.clear();
	DirectionS.clear();
	deltaX = 0;
	deltaY = 0;
	Direction = 0;

	seconds_1 = 0;
	seconds_2 = 0;
	frm_count = 0;

	deltaXX.clear();
	deltaYY.clear();
}

SwypeDetect::~SwypeDetect()
{

}

void SwypeDetect::init(int fps_e, std::string swype = "")
{
	fps = fps_e;
	setSwype(swype);
}

void SwypeDetect::setSwype(std::string swype)
{
	char t;
	int j;
	if (swype != "") {
		for (int i = 0; i < swype.length(); i++) {
			t = swype.at(i);
			j = atoi(&t);
			swype_Numbers.push_back(j);
		}
	}
}

void SwypeDetect::processFrame(cv::Mat frame, int &state, int &index, int &x, int &y)  // main logic
{

	std::vector<cv::Point2i> koord_Sw_points(9);
	cv::Mat buf1ft;
	cv::Mat buf2ft;
	cv::Mat hann;
	cv::Point2d shift;

	if (frame1.empty()) {
		cvtColor(frame, frame1, CV_RGB2GRAY); // RGB to grayscale
		frame2 = frame1.clone();
	}
	else {
		frame1 = frame2.clone();
		cvtColor(frame, frame2, CV_RGB2GRAY);
	}
	
	width = frame1.cols; 
	height = frame1.rows; 

	koord_Sw_points = Koord_Swipe_Points(width, height); // we get the coordinates of the swipe points
	
	frame1.convertTo(buf1ft, CV_64F); // converting frames to CV_64F type
	frame2.convertTo(buf2ft, CV_64F);

	if (hann.empty()) {
		createHanningWindow(hann, buf1ft.size(), CV_64F); //  create Hanning window
	}

	shift = phaseCorrelate(buf1ft, buf2ft, hann); // we calculate a phase offset vector

	Delta_Calculation(shift, call); //offsets calculation for frames
	
	if (S == 0) {
		if ((fabs(deltaX) > 3) || (fabs(deltaY) > 3)) S = CircleDetection(); //circle detection
		seconds_1 = time(NULL);
	}
	else if (S == 1) {
		if (!swype_Numbers.empty()) S = 2; // if we have swype then we go to detection swype from video
		state = S;
		seconds_2 = time(NULL);
		if ((seconds_2 - seconds_1) > 4) Reset();
	}
	else if (S == 2) {
		//for (int j = count_num; j < swype_Numbers.size; j++) {

			if ((fabs(deltaX) > 3) || (fabs(deltaY) > 3)) {
				DirectionS.push_back(Direction);
				count_direction++;
			}

			if ((count_direction >= 2) && (DirectionS[count_direction] == DirectionS[count_direction - 1]) && (DirectionS[count_direction - 1] == DirectionS[count_direction - 2])) {
				count_num++;
				if (count_num > 1) Swype_Data(koord_Sw_points);
			}
			if ((Swype_Koord[count_num].x == 0) || (Swype_Koord[count_num].y == 0)) Reset();
			else {
				if (Swype_Numbers_Get[count_num] == swype_Numbers[count_num]) {
					index = Swype_Numbers_Get[count_num];
					x = Swype_Koord[count_num].x;
					y = Swype_Koord[count_num].y;
					if (Swype_Numbers_Get.size() == swype_Numbers.size()) {
						S = 3;
						call = 0;
					}
				}
				else Reset();
			}
			if (count_direction > 2) count_direction = -1;
			if (count_direction > 8) Reset();
			state = S;
			frm_count++;
			if ((2 * (int)swype_Numbers.size()) <= (frm_count / fps)) Reset();
		}
	else state = S;

	//}


	call++;
	
}

void SwypeDetect::Reset(void)  // reset
{
	call = 0;
	count_direction = -1;
	count_num = -1;
	S = 0;
	width = 0;
	height = 0;
	Swype_Numbers_Get.clear();
	Swype_Koord.clear();
	DirectionS.clear();
	deltaX = 0;
	deltaY = 0;
	Direction = 0;

	seconds_1 = 0;
	seconds_2 = 0;
	frm_count = 0;

	deltaXX.clear();
	deltaYY.clear();
}