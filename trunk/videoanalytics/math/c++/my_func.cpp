#include <opencv2\opencv.hpp>
#include <vector>
#include <cmath>

using namespace cv;
using namespace std;

vector<double> x_corr(vector<double>& deltaXX, vector<double>& deltaYY);


//Circle detection function (use OpenCV Mat, but may be released without it) 
int CircleDetection(vector<double>& deltaXX, vector<double>& deltaYY, Mat frame)
{
	vector<double> C;
	int lenth_deltaXX = (int)deltaXX.size();
	for (int i = 0; i < lenth_deltaXX; i++) {
		if ((deltaXX[i] > frame.cols) && ((deltaYY[i] > frame.rows))) {
			if ((deltaXX[i] < (frame.cols / 2)) && (deltaYY[i] < (frame.rows / 2))) {
				C = x_corr(deltaXX, deltaYY);
				for (int j = 0; j <= i; j++) {
					if (fabs(C[j]) > 0.75) return 1;
				}
				 
			}
		}
	}
	return 0;
}

vector<double> x_corr(vector<double>& deltaXX, vector<double>& deltaYY)
{
	vector<double> Result;
	matchTemplate(deltaXX, deltaYY, Result, CV_TM_CCOEFF_NORMED);
	return Result;
}





vector<Point2d> Koord_Swipe_Points(int width, int height)
{
	vector<Point2d> Result(9);

	Result[0].x = 2 * width / 8;
	Result[0].y = 2 * height / 8;

	Result[1].x = 4 * width / 8;
	Result[1].y = 2 * height / 8;

	Result[2].x = 6 * width / 8;
	Result[2].y = 2 * height / 8;

	Result[3].x = 2 * width / 8;
	Result[3].y = 4 * height / 8;

	Result[4].x = 4 * width / 8;
	Result[4].y = 4 * height / 8;

	Result[5].x = 6 * width / 8;
	Result[5].y = 4 * height / 8;

	Result[6].x = 2 * width / 8;
	Result[6].y = 6 * height / 8;

	Result[7].x = 4 * width / 8;
	Result[7].y = 6 * height / 8;

	Result[8].x = 6 * width / 8;
	Result[8].y = 6 * height / 8;
	
	return Result;
}

struct Delta_Calculation_Out
{
	double deltaX;
	double deltaY;
	int Direction;
	double Mean_Alfa;
	double K;
};


Delta_Calculation_Out Delta_Calculation(Point2d output, int k, vector<double>& deltaXX, vector<double>& deltaYY)
{
	Delta_Calculation_Out Result;
	double rez_vec_2_x;
	double rez_vec_2_y;
	double rez_vec_1_x;
	double rez_vec_1_y;
	double pi = 3.14;

	Result.deltaX = output.x;
	Result.deltaY = output.y;

	if (k == 0) {
		deltaXX[k] = Result.deltaX;
		deltaYY[k] = Result.deltaY;
	}
	else {
		deltaXX[k] = deltaXX[k - 1] + Result.deltaX;
		deltaYY[k] = deltaYY[k - 1] + Result.deltaY;
	}
	
	rez_vec_2_x = 0;
	rez_vec_2_y = 0;
	rez_vec_1_x = floor(Result.deltaX);
	rez_vec_1_y = floor(Result.deltaY);

	Result.K = (rez_vec_2_y - rez_vec_1_y)/((rez_vec_2_x - rez_vec_1_x) + 0.000001);


	// 1
	if ((rez_vec_2_x >= rez_vec_1_x) && (rez_vec_2_y >= rez_vec_1_y)) Result.Mean_Alfa = floor((atan((Result.K))*180/ pi));
	// 2
	if ((rez_vec_1_x>rez_vec_2_x) && (rez_vec_2_y >= rez_vec_1_y)) Result.Mean_Alfa = 180 - abs(floor((atan((Result.K))*180 / pi)));
	//3
	if ((rez_vec_1_x >= rez_vec_2_x) && (rez_vec_1_y>rez_vec_2_y)) Result.Mean_Alfa = 180 + abs(floor((atan((Result.K))*180/ pi)));
	//4
	if ((rez_vec_2_x>rez_vec_1_x) && (rez_vec_1_y>rez_vec_2_y)) Result.Mean_Alfa = 360 - abs(floor((atan((Result.K))*180 / pi)));
	//
	if (((Result.Mean_Alfa >= 337) && (Result.Mean_Alfa <= 360)) || ((Result.Mean_Alfa >= 0) && (Result.Mean_Alfa<22.5))) Result.Direction = 5;
	if ((Result.Mean_Alfa >= 22.5) && (Result.Mean_Alfa<67.5)) Result.Direction = 6;
	if ((Result.Mean_Alfa >= 67.5) && (Result.Mean_Alfa<112.5)) Result.Direction = 7;
	if ((Result.Mean_Alfa >= 112.5) && (Result.Mean_Alfa<157.5)) Result.Direction = 8;
	if ((Result.Mean_Alfa >= 157.5) && (Result.Mean_Alfa<202.5)) Result.Direction = 1;
	if ((Result.Mean_Alfa >= 202.5) && (Result.Mean_Alfa<247.5)) Result.Direction = 2;
	if ((Result.Mean_Alfa >= 247.5) && (Result.Mean_Alfa<292.5)) Result.Direction = 3;
	if ((Result.Mean_Alfa >= 292.5) && (Result.Mean_Alfa<337.5)) Result.Direction = 4;

	return Result;
}


Point2d Phase_Cor(Mat& buf_curr, Mat& buf_prev, Mat& buf_hann)
{
	Mat curr64f, prev64f;
	Point2d Result;

	buf_prev.convertTo(prev64f, CV_64F);
	buf_curr.convertTo(curr64f, CV_64F);
	Result = phaseCorrelate(prev64f, curr64f, buf_hann);

	return Result;
}



void Swype_Data(vector<int>& Swype_Numbers, vector<Point2d>& Swype_Koord, int count_direction, vector<int>& Direction_S, int count_num, vector<Point2d>& koord)
{

	switch (Direction_S[count_direction]) {

	case 1:
		switch (Swype_Numbers[count_num - 1]) {
		case 1:
			Swype_Numbers[count_num] = 4;
			Swype_Koord[count_num].x = koord[4].x;
			Swype_Koord[count_num].y = koord[4].y;
			break;
		case 2:
			Swype_Numbers[count_num] = 5;
			Swype_Koord[count_num].x = koord[5].x;
			Swype_Koord[count_num].y = koord[5].y;
			break;
		case 3:
			Swype_Numbers[count_num] = 6;
			Swype_Koord[count_num].x = koord[6].x;
			Swype_Koord[count_num].y = koord[6].y;
			break;
		case 4:
			Swype_Numbers[count_num] = 7;
			Swype_Koord[count_num].x = koord[7].x;
			Swype_Koord[count_num].y = koord[7].y;
			break;
		case 5:
			Swype_Numbers[count_num] = 8;
			Swype_Koord[count_num].x = koord[8].x;
			Swype_Koord[count_num].y = koord[8].y;
			break;
		case 6:
			Swype_Numbers[count_num] = 9;
			Swype_Koord[count_num].x = koord[9].x;
			Swype_Koord[count_num].y = koord[9].y;
			break;
		default:
			Swype_Numbers[count_num] = 0;
			Swype_Koord[count_num].x = 0;
			Swype_Koord[count_num].y = 0;
		}
		break;
	case 2:
		switch (Swype_Numbers[count_num - 1]) {
		case 1:
			Swype_Numbers[count_num] = 5;
			Swype_Koord[count_num].x = koord[5].x;
			Swype_Koord[count_num].y = koord[5].y;
			break;
		case 2:
			Swype_Numbers[count_num] = 6;
			Swype_Koord[count_num].x = koord[6].x;
			Swype_Koord[count_num].y = koord[6].y;
			break;
		case 4:
			Swype_Numbers[count_num] = 8;
			Swype_Koord[count_num].x = koord[8].x;
			Swype_Koord[count_num].y = koord[8].y;
			break;
		case 5:
			Swype_Numbers[count_num] = 9;
			Swype_Koord[count_num].x = koord[9].x;
			Swype_Koord[count_num].y = koord[9].y;
			break;
		default:
			Swype_Numbers[count_num] = 0;
			Swype_Koord[count_num].x = 0;
			Swype_Koord[count_num].y = 0;
		}
		break;
	case 3:
		switch (Swype_Numbers[count_num - 1]) {
		case 1:
			Swype_Numbers[count_num] = 2;
			Swype_Koord[count_num].x = koord[2].x;
			Swype_Koord[count_num].y = koord[2].y;
			break;
		case 2:
			Swype_Numbers[count_num] = 3;
			Swype_Koord[count_num].x = koord[3].x;
			Swype_Koord[count_num].y = koord[3].y;
			break;
		case 4:
			Swype_Numbers[count_num] = 5;
			Swype_Koord[count_num].x = koord[5].x;
			Swype_Koord[count_num].y = koord[5].y;
			break;
		case 5:
			Swype_Numbers[count_num] = 6;
			Swype_Koord[count_num].x = koord[6].x;
			Swype_Koord[count_num].y = koord[6].y;
			break;
		case 7:
			Swype_Numbers[count_num] = 8;
			Swype_Koord[count_num].x = koord[8].x;
			Swype_Koord[count_num].y = koord[8].y;
			break;
		case 8:
			Swype_Numbers[count_num] = 9;
			Swype_Koord[count_num].x = koord[9].x;
			Swype_Koord[count_num].y = koord[9].y;
			break;
		default:
			Swype_Numbers[count_num] = 0;
			Swype_Koord[count_num].x = 0;
			Swype_Koord[count_num].y = 0;
		}
		break;
	case 4:
		switch (Swype_Numbers[count_num - 1]) {
		case 4:
			Swype_Numbers[count_num] = 2;
			Swype_Koord[count_num].x = koord[2].x;
			Swype_Koord[count_num].y = koord[2].y;
			break;
		case 5:
			Swype_Numbers[count_num] = 3;
			Swype_Koord[count_num].x = koord[3].x;
			Swype_Koord[count_num].y = koord[3].y;
			break;
		case 7:
			Swype_Numbers[count_num] = 5;
			Swype_Koord[count_num].x = koord[5].x;
			Swype_Koord[count_num].y = koord[5].y;
			break;
		case 8:
			Swype_Numbers[count_num] = 6;
			Swype_Koord[count_num].x = koord[6].x;
			Swype_Koord[count_num].y = koord[6].y;
			break;
		default:
			Swype_Numbers[count_num] = 0;
			Swype_Koord[count_num].x = 0;
			Swype_Koord[count_num].y = 0;
		}
		break;
	case 5:
		switch (Swype_Numbers[count_num - 1]) {
		case 4:
			Swype_Numbers[count_num] = 1;
			Swype_Koord[count_num].x = koord[1].x;
			Swype_Koord[count_num].y = koord[1].y;
			break;
		case 5:
			Swype_Numbers[count_num] = 2;
			Swype_Koord[count_num].x = koord[2].x;
			Swype_Koord[count_num].y = koord[2].y;
			break;
		case 6:
			Swype_Numbers[count_num] = 3;
			Swype_Koord[count_num].x = koord[3].x;
			Swype_Koord[count_num].y = koord[3].y;
			break;
		case 7:
			Swype_Numbers[count_num] = 4;
			Swype_Koord[count_num].x = koord[4].x;
			Swype_Koord[count_num].y = koord[4].y;
			break;
		case 8:
			Swype_Numbers[count_num] = 5;
			Swype_Koord[count_num].x = koord[5].x;
			Swype_Koord[count_num].y = koord[5].y;
			break;
		case 9:
			Swype_Numbers[count_num] = 6;
			Swype_Koord[count_num].x = koord[6].x;
			Swype_Koord[count_num].y = koord[6].y;
			break;
		default:
			Swype_Numbers[count_num] = 0;
			Swype_Koord[count_num].x = 0;
			Swype_Koord[count_num].y = 0;
		}
		break;
	case 6:
		switch (Swype_Numbers[count_num - 1]) {
		case 5:
			Swype_Numbers[count_num] = 1;
			Swype_Koord[count_num].x = koord[1].x;
			Swype_Koord[count_num].y = koord[1].y;
			break;
		case 6:
			Swype_Numbers[count_num] = 2;
			Swype_Koord[count_num].x = koord[2].x;
			Swype_Koord[count_num].y = koord[2].y;
			break;
		case 8:
			Swype_Numbers[count_num] = 4;
			Swype_Koord[count_num].x = koord[4].x;
			Swype_Koord[count_num].y = koord[4].y;
			break;
		case 9:
			Swype_Numbers[count_num] = 5;
			Swype_Koord[count_num].x = koord[5].x;
			Swype_Koord[count_num].y = koord[5].y;
			break;
		default:
			Swype_Numbers[count_num] = 0;
			Swype_Koord[count_num].x = 0;
			Swype_Koord[count_num].y = 0;
		}
		break;
	case 7:
		switch (Swype_Numbers[count_num - 1]) {
		case 2:
			Swype_Numbers[count_num] = 1;
			Swype_Koord[count_num].x = koord[1].x;
			Swype_Koord[count_num].y = koord[1].y;
			break;
		case 3:
			Swype_Numbers[count_num] = 2;
			Swype_Koord[count_num].x = koord[2].x;
			Swype_Koord[count_num].y = koord[2].y;
			break;
		case 5:
			Swype_Numbers[count_num] = 4;
			Swype_Koord[count_num].x = koord[4].x;
			Swype_Koord[count_num].y = koord[4].y;
			break;
		case 6:
			Swype_Numbers[count_num] = 5;
			Swype_Koord[count_num].x = koord[5].x;
			Swype_Koord[count_num].y = koord[5].y;
			break;
		case 8:
			Swype_Numbers[count_num] = 7;
			Swype_Koord[count_num].x = koord[7].x;
			Swype_Koord[count_num].y = koord[7].y;
			break;
		case 9:
			Swype_Numbers[count_num] = 8;
			Swype_Koord[count_num].x = koord[8].x;
			Swype_Koord[count_num].y = koord[8].y;
			break;
		default:
			Swype_Numbers[count_num] = 0;
			Swype_Koord[count_num].x = 0;
			Swype_Koord[count_num].y = 0;
		}
		break;
	case 8:
		switch (Swype_Numbers[count_num - 1]) {
		case 2:
			Swype_Numbers[count_num] = 4;
			Swype_Koord[count_num].x = koord[4].x;
			Swype_Koord[count_num].y = koord[4].y;
			break;
		case 3:
			Swype_Numbers[count_num] = 5;
			Swype_Koord[count_num].x = koord[5].x;
			Swype_Koord[count_num].y = koord[5].y;
			break;
		case 5:
			Swype_Numbers[count_num] =7;
			Swype_Koord[count_num].x = koord[7].x;
			Swype_Koord[count_num].y = koord[7].y;
			break;
		case 6:
			Swype_Numbers[count_num] = 8;
			Swype_Koord[count_num].x = koord[8].x;
			Swype_Koord[count_num].y = koord[8].y;
			break;
		default:
			Swype_Numbers[count_num] = 0;
			Swype_Koord[count_num].x = 0;
			Swype_Koord[count_num].y = 0;
		}
		break;
	}
}

		

