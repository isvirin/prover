#include "swype_detect.h"

int SwypeDetect::CircleDetection(void)  // circle detection algorithm
{
	std::vector<double> C;
        int lenth_deltaXX = (int)_deltaXX.size();
	for (int i = 0; i < lenth_deltaXX; i++) {
                if ((_deltaXX[i] > _frame2.cols) && ((_deltaYY[i] > _frame2.rows))) {
                        if ((_deltaXX[i] < (_frame2.cols / 2)) && (_deltaYY[i] < (_frame2.rows / 2))) {
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
        cv::matchTemplate(_deltaXX, _deltaYY, Result, CV_TM_CCOEFF_NORMED);
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

        _deltaX = output.x;
        _deltaY = output.y;

	if (k == 0) {
                _deltaXX.push_back(_deltaX);
                _deltaYY.push_back(_deltaY);
	}
	else {
                _deltaXX.push_back(_deltaXX[k - 1] + _deltaX);
                _deltaYY.push_back(_deltaYY[k - 1] + _deltaY);
	}

	rez_vec_2_x = 0;
	rez_vec_2_y = 0;
        rez_vec_1_x = floor(_deltaX);
        rez_vec_1_y = floor(_deltaY);

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
        if (((Mean_Alfa >= 337) && (Mean_Alfa <= 360)) || ((Mean_Alfa >= 0) && (Mean_Alfa<22.5))) _Direction = 5;
        if ((Mean_Alfa >= 22.5) && (Mean_Alfa<67.5)) _Direction = 6;
        if ((Mean_Alfa >= 67.5) && (Mean_Alfa<112.5)) _Direction = 7;
        if ((Mean_Alfa >= 112.5) && (Mean_Alfa<157.5)) _Direction = 8;
        if ((Mean_Alfa >= 157.5) && (Mean_Alfa<202.5)) _Direction = 1;
        if ((Mean_Alfa >= 202.5) && (Mean_Alfa<247.5)) _Direction = 2;
        if ((Mean_Alfa >= 247.5) && (Mean_Alfa<292.5)) _Direction = 3;
        if ((Mean_Alfa >= 292.5) && (Mean_Alfa<337.5)) _Direction = 4;
}

void SwypeDetect::Swype_Data(std::vector<cv::Point2i>& koord)  // logic for entering swype numbers
{

	
        switch (_DirectionS[_count_direction]) {

	case 1:
                switch (_Swype_Numbers_Get[_count_num - 1]) {
		case 1:
                        _Swype_Numbers_Get[_count_num] = 4;
                        _Swype_Koord[_count_num].x = koord[4].x;
                        _Swype_Koord[_count_num].y = koord[4].y;
			break;
		case 2:
                        _Swype_Numbers_Get[_count_num] = 5;
                        _Swype_Koord[_count_num].x = koord[5].x;
                        _Swype_Koord[_count_num].y = koord[5].y;
			break;
		case 3:
                        _Swype_Numbers_Get[_count_num] = 6;
                        _Swype_Koord[_count_num].x = koord[6].x;
                        _Swype_Koord[_count_num].y = koord[6].y;
			break;
		case 4:
                        _Swype_Numbers_Get[_count_num] = 7;
                        _Swype_Koord[_count_num].x = koord[7].x;
                        _Swype_Koord[_count_num].y = koord[7].y;
			break;
		case 5:
                        _Swype_Numbers_Get[_count_num] = 8;
                        _Swype_Koord[_count_num].x = koord[8].x;
                        _Swype_Koord[_count_num].y = koord[8].y;
			break;
		case 6:
                        _Swype_Numbers_Get[_count_num] = 9;
                        _Swype_Koord[_count_num].x = koord[9].x;
                        _Swype_Koord[_count_num].y = koord[9].y;
			break;
		default:
                        _Swype_Numbers_Get[_count_num] = 0;
                        _Swype_Koord[_count_num].x = 0;
                        _Swype_Koord[_count_num].y = 0;
		}
		break;
	case 2:
                switch (_Swype_Numbers_Get[_count_num - 1]) {
		case 1:
                        _Swype_Numbers_Get[_count_num] = 5;
                        _Swype_Koord[_count_num].x = koord[5].x;
                        _Swype_Koord[_count_num].y = koord[5].y;
			break;
		case 2:
                        _Swype_Numbers_Get[_count_num] = 6;
                        _Swype_Koord[_count_num].x = koord[6].x;
                        _Swype_Koord[_count_num].y = koord[6].y;
			break;
		case 4:
                        _Swype_Numbers_Get[_count_num] = 8;
                        _Swype_Koord[_count_num].x = koord[8].x;
                        _Swype_Koord[_count_num].y = koord[8].y;
			break;
		case 5:
                        _Swype_Numbers_Get[_count_num] = 9;
                        _Swype_Koord[_count_num].x = koord[9].x;
                        _Swype_Koord[_count_num].y = koord[9].y;
			break;
		default:
                        _Swype_Numbers_Get[_count_num] = 0;
                        _Swype_Koord[_count_num].x = 0;
                        _Swype_Koord[_count_num].y = 0;
		}
		break;
	case 3:
                switch (_Swype_Numbers_Get[_count_num - 1]) {
		case 1:
                        _Swype_Numbers_Get[_count_num] = 2;
                        _Swype_Koord[_count_num].x = koord[2].x;
                        _Swype_Koord[_count_num].y = koord[2].y;
			break;
		case 2:
                        _Swype_Numbers_Get[_count_num] = 3;
                        _Swype_Koord[_count_num].x = koord[3].x;
                        _Swype_Koord[_count_num].y = koord[3].y;
			break;
		case 4:
                        _Swype_Numbers_Get[_count_num] = 5;
                        _Swype_Koord[_count_num].x = koord[5].x;
                        _Swype_Koord[_count_num].y = koord[5].y;
			break;
		case 5:
                        _Swype_Numbers_Get[_count_num] = 6;
                        _Swype_Koord[_count_num].x = koord[6].x;
                        _Swype_Koord[_count_num].y = koord[6].y;
			break;
		case 7:
                        _Swype_Numbers_Get[_count_num] = 8;
                        _Swype_Koord[_count_num].x = koord[8].x;
                        _Swype_Koord[_count_num].y = koord[8].y;
			break;
		case 8:
                        _Swype_Numbers_Get[_count_num] = 9;
                        _Swype_Koord[_count_num].x = koord[9].x;
                        _Swype_Koord[_count_num].y = koord[9].y;
			break;
		default:
                        _Swype_Numbers_Get[_count_num] = 0;
                        _Swype_Koord[_count_num].x = 0;
                        _Swype_Koord[_count_num].y = 0;
		}
		break;
	case 4:
                switch (_Swype_Numbers_Get[_count_num - 1]) {
		case 4:
                        _Swype_Numbers_Get[_count_num] = 2;
                        _Swype_Koord[_count_num].x = koord[2].x;
                        _Swype_Koord[_count_num].y = koord[2].y;
			break;
		case 5:
                        _Swype_Numbers_Get[_count_num] = 3;
                        _Swype_Koord[_count_num].x = koord[3].x;
                        _Swype_Koord[_count_num].y = koord[3].y;
			break;
		case 7:
                        _Swype_Numbers_Get[_count_num] = 5;
                        _Swype_Koord[_count_num].x = koord[5].x;
                        _Swype_Koord[_count_num].y = koord[5].y;
			break;
		case 8:
                        _Swype_Numbers_Get[_count_num] = 6;
                        _Swype_Koord[_count_num].x = koord[6].x;
                        _Swype_Koord[_count_num].y = koord[6].y;
			break;
		default:
                        _Swype_Numbers_Get[_count_num] = 0;
                        _Swype_Koord[_count_num].x = 0;
                        _Swype_Koord[_count_num].y = 0;
		}
		break;
	case 5:
                switch (_Swype_Numbers_Get[_count_num - 1]) {
		case 4:
                        _Swype_Numbers_Get[_count_num] = 1;
                        _Swype_Koord[_count_num].x = koord[1].x;
                        _Swype_Koord[_count_num].y = koord[1].y;
			break;
		case 5:
                        _Swype_Numbers_Get[_count_num] = 2;
                        _Swype_Koord[_count_num].x = koord[2].x;
                        _Swype_Koord[_count_num].y = koord[2].y;
			break;
		case 6:
                        _Swype_Numbers_Get[_count_num] = 3;
                        _Swype_Koord[_count_num].x = koord[3].x;
                        _Swype_Koord[_count_num].y = koord[3].y;
			break;
		case 7:
                        _Swype_Numbers_Get[_count_num] = 4;
                        _Swype_Koord[_count_num].x = koord[4].x;
                        _Swype_Koord[_count_num].y = koord[4].y;
			break;
		case 8:
                        _Swype_Numbers_Get[_count_num] = 5;
                        _Swype_Koord[_count_num].x = koord[5].x;
                        _Swype_Koord[_count_num].y = koord[5].y;
			break;
		case 9:
                        _Swype_Numbers_Get[_count_num] = 6;
                        _Swype_Koord[_count_num].x = koord[6].x;
                        _Swype_Koord[_count_num].y = koord[6].y;
			break;
		default:
                        _Swype_Numbers_Get[_count_num] = 0;
                        _Swype_Koord[_count_num].x = 0;
                        _Swype_Koord[_count_num].y = 0;
		}
		break;
	case 6:
                switch (_Swype_Numbers_Get[_count_num - 1]) {
		case 5:
                        _Swype_Numbers_Get[_count_num] = 1;
                        _Swype_Koord[_count_num].x = koord[1].x;
                        _Swype_Koord[_count_num].y = koord[1].y;
			break;
		case 6:
                        _Swype_Numbers_Get[_count_num] = 2;
                        _Swype_Koord[_count_num].x = koord[2].x;
                        _Swype_Koord[_count_num].y = koord[2].y;
			break;
		case 8:
                        _Swype_Numbers_Get[_count_num] = 4;
                        _Swype_Koord[_count_num].x = koord[4].x;
                        _Swype_Koord[_count_num].y = koord[4].y;
			break;
		case 9:
                        _Swype_Numbers_Get[_count_num] = 5;
                        _Swype_Koord[_count_num].x = koord[5].x;
                        _Swype_Koord[_count_num].y = koord[5].y;
			break;
		default:
                        _Swype_Numbers_Get[_count_num] = 0;
                        _Swype_Koord[_count_num].x = 0;
                        _Swype_Koord[_count_num].y = 0;
		}
		break;
	case 7:
                switch (_Swype_Numbers_Get[_count_num - 1]) {
		case 2:
                        _Swype_Numbers_Get[_count_num] = 1;
                        _Swype_Koord[_count_num].x = koord[1].x;
                        _Swype_Koord[_count_num].y = koord[1].y;
			break;
		case 3:
                        _Swype_Numbers_Get[_count_num] = 2;
                        _Swype_Koord[_count_num].x = koord[2].x;
                        _Swype_Koord[_count_num].y = koord[2].y;
			break;
		case 5:
                        _Swype_Numbers_Get[_count_num] = 4;
                        _Swype_Koord[_count_num].x = koord[4].x;
                        _Swype_Koord[_count_num].y = koord[4].y;
			break;
		case 6:
                        _Swype_Numbers_Get[_count_num] = 5;
                        _Swype_Koord[_count_num].x = koord[5].x;
                        _Swype_Koord[_count_num].y = koord[5].y;
			break;
		case 8:
                        _Swype_Numbers_Get[_count_num] = 7;
                        _Swype_Koord[_count_num].x = koord[7].x;
                        _Swype_Koord[_count_num].y = koord[7].y;
			break;
		case 9:
                        _Swype_Numbers_Get[_count_num] = 8;
                        _Swype_Koord[_count_num].x = koord[8].x;
                        _Swype_Koord[_count_num].y = koord[8].y;
			break;
		default:
                        _Swype_Numbers_Get[_count_num] = 0;
                        _Swype_Koord[_count_num].x = 0;
                        _Swype_Koord[_count_num].y = 0;
		}
		break;
	case 8:
                switch (_Swype_Numbers_Get[_count_num - 1]) {
		case 2:
                        _Swype_Numbers_Get[_count_num] = 4;
                        _Swype_Koord[_count_num].x = koord[4].x;
                        _Swype_Koord[_count_num].y = koord[4].y;
			break;
		case 3:
                        _Swype_Numbers_Get[_count_num] = 5;
                        _Swype_Koord[_count_num].x = koord[5].x;
                        _Swype_Koord[_count_num].y = koord[5].y;
			break;
		case 5:
                        _Swype_Numbers_Get[_count_num] = 7;
                        _Swype_Koord[_count_num].x = koord[7].x;
                        _Swype_Koord[_count_num].y = koord[7].y;
			break;
		case 6:
                        _Swype_Numbers_Get[_count_num] = 8;
                        _Swype_Koord[_count_num].x = koord[8].x;
                        _Swype_Koord[_count_num].y = koord[8].y;
			break;
		default:
                        _Swype_Numbers_Get[_count_num] = 0;
                        _Swype_Koord[_count_num].x = 0;
                        _Swype_Koord[_count_num].y = 0;
		}
		break;
	}
}

SwypeDetect::SwypeDetect() // initialization
{
        _call = 0;
        _count_direction = -1;
        _count_num = -1;
        _S = 0;
        _width = 0;
        _height = 0;
        _Swype_Numbers_Get.clear();
        _Swype_Koord.clear();
        _DirectionS.clear();
        _deltaX = 0;
        _deltaY = 0;
        _Direction = 0;

        _seconds_1 = 0;
        _seconds_2 = 0;
        _frm_count = 0;

        _deltaXX.clear();
        _deltaYY.clear();
}

SwypeDetect::~SwypeDetect()
{

}

void SwypeDetect::init(int fps_e, std::string swype = "")
{
        _fps = fps_e;
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
                        _swypeNumbers.push_back(j);
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

        if (_frame1.empty()) {
                cvtColor(frame, _frame1, CV_RGB2GRAY); // RGB to grayscale
                _frame2 = _frame1.clone();
	}
	else {
                _frame1 = _frame2.clone();
                cvtColor(frame, _frame2, CV_RGB2GRAY);
	}
	
        _width = _frame1.cols;
        _height = _frame1.rows;

        koord_Sw_points = Koord_Swipe_Points(_width, _height); // we get the coordinates of the swipe points
	
        _frame1.convertTo(buf1ft, CV_64F); // converting frames to CV_64F type
        _frame2.convertTo(buf2ft, CV_64F);

	if (hann.empty()) {
		createHanningWindow(hann, buf1ft.size(), CV_64F); //  create Hanning window
	}

	shift = phaseCorrelate(buf1ft, buf2ft, hann); // we calculate a phase offset vector

        Delta_Calculation(shift, _call); //offsets calculation for frames
	
        if (_S == 0) {
                if ((fabs(_deltaX) > 3) || (fabs(_deltaY) > 3)) _S = CircleDetection(); //circle detection
                _seconds_1 = time(NULL);
	}
        else if (_S == 1) {
                if (!_swypeNumbers.empty()) _S = 2; // if we have swype then we go to detection swype from video
                state = _S;
                _seconds_2 = time(NULL);
                if ((_seconds_2 - _seconds_1) > 4) Reset();
	}
        else if (_S == 2) {
                //for (int j = _count_num; j < _swypeNumbers.size; j++) {

                        if ((fabs(_deltaX) > 3) || (fabs(_deltaY) > 3)) {
                                _DirectionS.push_back(_Direction);
                                _count_direction++;
			}

                        if ((_count_direction >= 2) && (_DirectionS[_count_direction] == _DirectionS[_count_direction - 1]) && (_DirectionS[_count_direction - 1] == _DirectionS[_count_direction - 2])) {
                                _count_num++;
                                if (_count_num > 1) Swype_Data(koord_Sw_points);
			}
                        if ((_Swype_Koord[_count_num].x == 0) || (_Swype_Koord[_count_num].y == 0)) Reset();
			else {
                                if (_Swype_Numbers_Get[_count_num] == _swypeNumbers[_count_num]) {
                                        index = _Swype_Numbers_Get[_count_num];
                                        x = _Swype_Koord[_count_num].x;
                                        y = _Swype_Koord[_count_num].y;
                                        if (_Swype_Numbers_Get.size() == _swypeNumbers.size()) {
                                                _S = 3;
                                                _call = 0;
					}
				}
				else Reset();
			}
                        if (_count_direction > 2) _count_direction = -1;
                        if (_count_direction > 8) Reset();
                        state = _S;
                        _frm_count++;
                        if ((2 * (int)_swypeNumbers.size()) <= (_frm_count / _fps)) Reset();
		}
        else state = _S;

	//}


        _call++;
	
}

void SwypeDetect::Reset(void)  // reset
{
        _call = 0;
        _count_direction = -1;
        _count_num = -1;
        _S = 0;
        _width = 0;
        _height = 0;
        _Swype_Numbers_Get.clear();
        _Swype_Koord.clear();
        _DirectionS.clear();
        _deltaX = 0;
        _deltaY = 0;
        _Direction = 0;

        _seconds_1 = 0;
        _seconds_2 = 0;
        _frm_count = 0;

        _deltaXX.clear();
        _deltaYY.clear();
}
