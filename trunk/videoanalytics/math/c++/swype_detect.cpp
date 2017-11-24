#include "swype_detect.h"

using namespace cv;
using namespace std;




int SwypeDetect::CircleDetection(void)
{
	vector<double> S_L(2);
	double S = 0;
	double L = 0;
	double C;
	int lenth_Delta = (int)Delta.size();

	if (call > 2) {
		for (int i = 1; i < (lenth_Delta - 1); i++) {
			S_L = S_L_define(Delta[i], Delta[i + 1]);
			L = L + S_L[1];
			S = S + S_L[0];
		}
		L = L + sqrt(pow(Delta[1].x, 2) + pow(Delta[1].y, 2)) + sqrt(pow(Delta[Delta.size() - 1].x, 2) + pow(Delta[Delta.size() - 1].y, 2));
		C = L / sqrt(S);
		if ((C<5)&&(C>3)) return 1;
	}
	return 0;
}



vector<Point2d> SwypeDetect::Koord_Swipe_Points(int width, int height)
{
	vector<Point2d> Result(10);

	Result[0].x = 0;
	Result[0].y = 0;
	
	Result[1].x = floor(2 * width / 8);
	Result[1].y = floor(2 * height / 8);

	Result[2].x = floor(4 * width / 8);
	Result[2].y = floor(2 * height / 8);

	Result[3].x = floor(6 * width / 8);
	Result[3].y = floor(2 * height / 8);

	Result[4].x = floor(2 * width / 8);
	Result[4].y = floor(4 * height / 8);

	Result[5].x = floor(4 * width / 8);
	Result[5].y = floor(4 * height / 8);

	Result[6].x = floor(6 * width / 8);
	Result[6].y = floor(4 * height / 8);

	Result[7].x = floor(2 * width / 8);
	Result[7].y = floor(6 * height / 8);

	Result[8].x = floor(4 * width / 8);
	Result[8].y = floor(6 * height / 8);

	Result[9].x = floor(6 * width / 8);
	Result[9].y = floor(6 * height / 8);

	return Result;
}

void SwypeDetect::Delta_Calculation(Point2d output)
{
	double Mean_Alfa;
	double K;

	double rez_vec_2_x;
	double rez_vec_2_y;
	double rez_vec_1_x;
	double rez_vec_1_y;
	double pi = 3.1415926535897932384626433832795;

	double radius = cv::sqrt(output.x*output.x + output.y*output.y);
	

	if (radius > 5) {

		fl_dir = true;
		if (call == 0) {
			D_coord.x = 0;
			D_coord.y = 0;
			
		}
		else {
			D_coord.x = D_coord.x + output.y;
			D_coord.y = D_coord.y + output.x;
			
		}
		Delta.push_back(D_coord);

		
		rez_vec_2_x = 0;
		rez_vec_2_y = 0;
		rez_vec_1_x = floor(D_coord.x);
		rez_vec_1_y = floor(D_coord.y);

		K = (rez_vec_2_y - rez_vec_1_y) / ((rez_vec_2_x - rez_vec_1_x) + 0.000001);


		// 1
		if ((rez_vec_2_x >= rez_vec_1_x) && (rez_vec_2_y >= rez_vec_1_y)) Mean_Alfa = floor((atan((K)) * 180 / pi));
		// 2
		if ((rez_vec_1_x > rez_vec_2_x) && (rez_vec_2_y >= rez_vec_1_y)) Mean_Alfa = 180 - abs(floor((atan((K)) * 180 / pi)));
		//3
		if ((rez_vec_1_x >= rez_vec_2_x) && (rez_vec_1_y > rez_vec_2_y)) Mean_Alfa = 180 + abs(floor((atan((K)) * 180 / pi)));
		//4
		if ((rez_vec_2_x > rez_vec_1_x) && (rez_vec_1_y > rez_vec_2_y)) Mean_Alfa = 360 - abs(floor((atan((K)) * 180 / pi)));
		//
		if (((Mean_Alfa >= 337) && (Mean_Alfa <= 360)) || ((Mean_Alfa >= 0) && (Mean_Alfa < 22.5))) Direction = 5;
		if ((Mean_Alfa >= 22.5) && (Mean_Alfa < 67.5)) Direction = 6;
		if ((Mean_Alfa >= 67.5) && (Mean_Alfa < 112.5)) Direction = 7;
		if ((Mean_Alfa >= 112.5) && (Mean_Alfa < 157.5)) Direction = 8;
		if ((Mean_Alfa >= 157.5) && (Mean_Alfa < 202.5)) Direction = 1;
		if ((Mean_Alfa >= 202.5) && (Mean_Alfa < 247.5)) Direction = 2;
		if ((Mean_Alfa >= 247.5) && (Mean_Alfa < 292.5)) Direction = 3;
		if ((Mean_Alfa >= 292.5) && (Mean_Alfa < 337.5)) Direction = 4;
		cout << Direction << endl;
		call++;
	}
}


void SwypeDetect::Swype_Data(vector<Point2d>& koord)
{

	
	switch (DirectionS[DirectionS.size() - 1]) {

	case 1:
		switch (Swype_Numbers_Get[count_num - 1]) {
		case 1:
			Swype_Numbers_Get.push_back(4);
			Swype_Koord.push_back(koord[4]);
			break;
		case 2:
			Swype_Numbers_Get.push_back(5);
			Swype_Koord.push_back(koord[5]);
			break;
		case 3:
			Swype_Numbers_Get.push_back(6);
			Swype_Koord.push_back(koord[6]);
			break;
		case 4:
			Swype_Numbers_Get.push_back(7);
			Swype_Koord.push_back(koord[7]);
			break;
		case 5:
			Swype_Numbers_Get.push_back(8);
			Swype_Koord.push_back(koord[8]);
			break;
		case 6:
			Swype_Numbers_Get.push_back(9);
			Swype_Koord.push_back(koord[9]);
			break;
		default:
			Swype_Numbers_Get[count_num] = 0;
			Swype_Koord.push_back(koord[0]);
		}
		break;
	case 2:
		switch (Swype_Numbers_Get[count_num - 1]) {
		case 1:
			Swype_Numbers_Get.push_back(5);
			Swype_Koord.push_back(koord[5]);
			break;
		case 2:
			Swype_Numbers_Get.push_back(6);
			Swype_Koord.push_back(koord[6]);
			break;
		case 4:
			Swype_Numbers_Get.push_back(8);
			Swype_Koord.push_back(koord[8]);
			break;
		case 5:
			Swype_Numbers_Get.push_back(9);
			Swype_Koord.push_back(koord[9]);
			break;
		default:
			Swype_Numbers_Get.push_back(0);
			Swype_Koord.push_back(koord[0]);
		}
		break;
	case 3:
		switch (Swype_Numbers_Get[count_num - 1]) {
		case 1:
			Swype_Numbers_Get.push_back(2);
			Swype_Koord.push_back(koord[2]);
			break;
		case 2:
			Swype_Numbers_Get.push_back(3);
			Swype_Koord.push_back(koord[3]);
			break;
		case 4:
			Swype_Numbers_Get.push_back(5);
			Swype_Koord.push_back(koord[5]);
			break;
		case 5:
			Swype_Numbers_Get.push_back(6);
			Swype_Koord.push_back(koord[6]);
			break;
		case 7:
			Swype_Numbers_Get.push_back(8);
			Swype_Koord.push_back(koord[8]);
			break;
		case 8:
			Swype_Numbers_Get.push_back(9);
			Swype_Koord.push_back(koord[9]);
			break;
		default:
			Swype_Numbers_Get.push_back(0);
			Swype_Koord.push_back(koord[0]);
		}
		break;
	case 4:
		switch (Swype_Numbers_Get[count_num - 1]) {
		case 4:
			Swype_Numbers_Get.push_back(2);
			Swype_Koord.push_back(koord[2]);
			break;
		case 5:
			Swype_Numbers_Get.push_back(3);
			Swype_Koord.push_back(koord[3]);
			break;
		case 7:
			Swype_Numbers_Get.push_back(5);
			Swype_Koord.push_back(koord[5]);
			break;
		case 8:
			Swype_Numbers_Get.push_back(6);
			Swype_Koord.push_back(koord[6]);
			break;
		default:
			Swype_Numbers_Get.push_back(0);
			Swype_Koord.push_back(koord[0]);
		}
		break;
	case 5:
		switch (Swype_Numbers_Get[count_num - 1]) {
		case 4:
			Swype_Numbers_Get.push_back(1);
			Swype_Koord.push_back(koord[1]);
			break;
		case 5:
			Swype_Numbers_Get.push_back(2);
			Swype_Koord.push_back(koord[2]);
			break;
		case 6:
			Swype_Numbers_Get.push_back(3);
			Swype_Koord.push_back(koord[3]);
			break;
		case 7:
			Swype_Numbers_Get.push_back(4);
			Swype_Koord.push_back(koord[4]);
			break;
		case 8:
			Swype_Numbers_Get.push_back(5);
			Swype_Koord.push_back(koord[5]);
			break;
		case 9:
			Swype_Numbers_Get.push_back(6);
			Swype_Koord.push_back(koord[6]);
			break;
		default:
			Swype_Numbers_Get.push_back(0);
			Swype_Koord.push_back(koord[0]);
		}
		break;
	case 6:
		switch (Swype_Numbers_Get[count_num - 1]) {
		case 5:
			Swype_Numbers_Get.push_back(1);
			Swype_Koord.push_back(koord[1]);
			break;
		case 6:
			Swype_Numbers_Get.push_back(2);
			Swype_Koord.push_back(koord[2]);
			break;
		case 8:
			Swype_Numbers_Get.push_back(4);
			Swype_Koord.push_back(koord[4]);
			break;
		case 9:
			Swype_Numbers_Get.push_back(5);
			Swype_Koord.push_back(koord[5]);
			break;
		default:
			Swype_Numbers_Get.push_back(0);
			Swype_Koord.push_back(koord[0]);
		}
		break;
	case 7:
		switch (Swype_Numbers_Get[count_num - 1]) {
		case 2:
			Swype_Numbers_Get.push_back(1);
			Swype_Koord.push_back(koord[1]);
			break;
		case 3:
			Swype_Numbers_Get.push_back(2);
			Swype_Koord.push_back(koord[2]);
			break;
		case 5:
			Swype_Numbers_Get.push_back(4);
			Swype_Koord.push_back(koord[4]);
			break;
		case 6:
			Swype_Numbers_Get.push_back(5);
			Swype_Koord.push_back(koord[5]);
			break;
		case 8:
			Swype_Numbers_Get.push_back(7);
			Swype_Koord.push_back(koord[7]);
			break;
		case 9:
			Swype_Numbers_Get.push_back(8);
			Swype_Koord.push_back(koord[8]);
			break;
		default:
			Swype_Numbers_Get.push_back(0);
			Swype_Koord.push_back(koord[0]);
		}
		break;
	case 8:
		switch (Swype_Numbers_Get[count_num - 1]) {
		case 2:
			Swype_Numbers_Get.push_back(4);
			Swype_Koord.push_back(koord[4]);
			break;
		case 3:
			Swype_Numbers_Get.push_back(5);
			Swype_Koord.push_back(koord[5]);
			break;
		case 5:
			Swype_Numbers_Get.push_back(7);
			Swype_Koord.push_back(koord[7]);
			break;
		case 6:
			Swype_Numbers_Get.push_back(8);
			Swype_Koord.push_back(koord[8]);
			break;
		default:
			Swype_Numbers_Get.push_back(0);
			Swype_Koord.push_back(koord[0]);
		}
		break;
	}
}

SwypeDetect::SwypeDetect()
{
	ocl::setUseOpenCL(true);
	
	call = 0;
	count_num = -1;
	S = 0;
	Swype_Numbers_Get.clear();
	Swype_Koord.clear();
	DirectionS.clear();
	D_coord.x = 0;
	D_coord.y = 0;
	Direction = 0;

	seconds_1 = 0;
	seconds_2 = 0;

	Delta.clear();

	koord_Sw_points.reserve(10);
}

SwypeDetect::~SwypeDetect()
{
	ocl::setUseOpenCL(false);
}

void SwypeDetect::init(int fps_e, string swype = "")
{
	fps = fps_e;
	setSwype(swype);
}

void SwypeDetect::setSwype(string swype)
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

void SwypeDetect::processFrame(Mat frame, int &state, int &index, int &x, int &y, int &debug)
{

	Point2d shift;
	
	shift = Frame_processor(frame);
	
	Delta_Calculation(shift); //Вычисляем перемещение

	if (S == 0) {
		if ((fabs(D_coord.x) > 3) || (fabs(D_coord.x) > 3)) S = CircleDetection(); //Определяем круговое движение
		state = S;
	}
	else if (S == 1) {
		S1_processor();
	}
	else if (S == 2) {
		
		if (((fabs(D_coord.x) > 3) || (fabs(D_coord.y) > 3)) && fl_dir) { //Если приращение по какой-либо из координат больше 3, то это уже направление и его сохраняем в массив
			DirectionS.push_back(Direction);
			fl_dir = false;
			if (DirectionS.size()>=3) {
				if ((DirectionS[DirectionS.size()-1] == DirectionS[DirectionS.size() - 2]) && (DirectionS[DirectionS.size() - 2] == DirectionS[DirectionS.size() - 2])) { //если направление сохраняется, то засчитываем определение цифры свайпкода (верное или не верное)
					count_num++;
					Swype_Data(koord_Sw_points);
					if ((Swype_Koord[count_num].x == 0) || (Swype_Koord[count_num].y == 0)) Reset();
					else {
						if (Swype_Numbers_Get[count_num] == swype_Numbers[count_num]) {
							index = count_num;
							x = static_cast<int>(floor((Swype_Koord[count_num].x)));
							y = static_cast<int>(floor((Swype_Koord[count_num].y)));
							if (Swype_Numbers_Get.size() == swype_Numbers.size()) {
								S = 3;
								call = 0;
							}
						}
						else Reset();
					}
				}
			}

		}
	}
	state = S;
}


void SwypeDetect::processFrame(const unsigned char *frame_i, int width_i, int height_i, int &state, int &index, int &x, int &y, int &debug)
{
	Point2d shift;

    //NW21 convert

	Mat frame(height_i + height_i/2, width_i, CV_8UC1, (uchar *)frame_i);
	
	cvtColor(frame, frame, CV_YUV2RGBA_NV21);

	shift = Frame_processor(frame);
	
	Delta_Calculation(shift); //Вычисляем перемещение

	if (S == 0) {
		if ((fabs(D_coord.x) > 3) || (fabs(D_coord.x) > 3)) S = CircleDetection(); //Определяем круговое движение
		//seconds_1 = time(NULL);
	}
	else if (S == 1) {
		S1_processor();
		//seconds_2 = time(NULL);
		//if ((seconds_2 - seconds_1) > 4) Reset();
	}
	else if (S == 2) {

		if (((fabs(D_coord.x) > 3) || (fabs(D_coord.y) > 3)) && fl_dir) { //Если приращение по какой-либо из координат больше 3, то это уже направление и его сохраняем в массив
			DirectionS.push_back(Direction);
			fl_dir = false;
			if (DirectionS.size() >= 3) {
				if ((DirectionS[DirectionS.size() - 1] == DirectionS[DirectionS.size() - 2]) && (DirectionS[DirectionS.size() - 2] == DirectionS[DirectionS.size() - 2])) { //если направление сохраняется, то засчитываем определение цифры свайпкода (верное или не верное)
					count_num++;
					Swype_Data(koord_Sw_points);
					if ((Swype_Koord[count_num].x == 0) || (Swype_Koord[count_num].y == 0)) Reset();
					else {
						if (Swype_Numbers_Get[count_num] == swype_Numbers[count_num]) {
							index = count_num;
							x = static_cast<int>(floor((Swype_Koord[count_num].x)));
							y = static_cast<int>(floor((Swype_Koord[count_num].y)));
							if (Swype_Numbers_Get.size() == swype_Numbers.size()) {
								S = 3;
								call = 0;
							}
						}
						else Reset();
					}
				}
			}

		}
	}
	state = S;
}



void SwypeDetect::Reset(void)
{
	call = 0;
	count_num = -1;
	S = 0;
	fps = 0;
	fl_dir = false;

	Swype_Numbers_Get.clear();
	Swype_Numbers_Get.resize(0);

	Swype_Koord.clear();
	Swype_Koord.resize(0);

	DirectionS.clear();
	DirectionS.resize(0);

	swype_Numbers.clear();
	swype_Numbers.resize(0);
	
	Delta.clear();
	Delta.resize(0);

	koord_Sw_points.clear();
	koord_Sw_points.resize(0);

	D_coord.x = 0;
	D_coord.y = 0;
	
	Direction = 0;

	seconds_1 = 0;
	seconds_2 = 0;

	frame1.release();
	buf1ft.release();
	buf2ft.release();
	hann.release();

}


vector<double> SwypeDetect::S_L_define(Point2d a, Point2d b)
{
	vector<double> S_L_result(2);
	double L;
	double S;
	double p;
	double pp;

	L = sqrt(pow((a.x - b.x), 2) + pow((a.y - b.y), 2));
	p = L + sqrt(pow(a.x, 2) + pow(a.y, 2)) + sqrt(pow(b.x, 2) + pow(b.y, 2));
	pp = p / 2;
	S = sqrt(pp*(pp - sqrt(pow(a.x, 2) + pow(a.y, 2)))*(pp - sqrt(pow(b.x, 2) + pow(b.y, 2)))*(pp - L));
	S_L_result[0] = S;
	S_L_result[1] = L;

	return S_L_result;
}

Point2d SwypeDetect::Frame_processor(cv::Mat &frame_i)
{
	Point2d shift;

	UMat b_frame;

	frame_i.convertTo(b_frame, frame_i.depth());
	
	cvtColor(b_frame, frame1, CV_RGB2GRAY);// Перевод в градации серого
	
	
	if (buf1ft.empty()) {
		frame1.convertTo(buf2ft, CV_64F);//Преобразование фреймов в тип CV_64F
		buf1ft = buf2ft.clone();
		koord_Sw_points = Koord_Swipe_Points(frame1.cols, frame1.rows); //Получаем координаты swipe-точек
	}
	else {
		buf1ft = buf2ft.clone();
		frame1.convertTo(buf2ft, CV_64F);//Преобразование фреймов в тип CV_64F
	}
	if (hann.empty()) {
		createHanningWindow(hann, buf1ft.size(), CV_64F); //Если окно Ханна не было создано - создаем
	}
	shift = phaseCorrelate(buf1ft, buf2ft, hann); //Получаем вектор смещения фазы
										  
	return shift;
}

void SwypeDetect::S1_processor(void)
{

	if (!swype_Numbers.empty()) {
		Delta.clear();
		Delta.resize(0);
		Swype_Numbers_Get.clear();
		Swype_Numbers_Get.resize(0);
		DirectionS.clear();
		DirectionS.resize(0);
		D_coord.x = 0;
		D_coord.y = 0;
		Direction = 0;
		count_num++;
		Swype_Numbers_Get.push_back(swype_Numbers[0]);
		Swype_Koord.push_back(koord_Sw_points[swype_Numbers[0]]);
		S = 2; //Если получили свайпкод, то переходим к детектированию
	}
}
