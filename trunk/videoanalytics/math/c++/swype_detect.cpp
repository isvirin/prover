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
    double rez_vec_1_x = 0;
    double rez_vec_1_y = 0;
    double pi = 3.1415926535897932384626433832795;

    double radius = cv::sqrt(output.x*output.x + output.y*output.y);


    if (radius > 5) {

        fl_dir = true;
        if (call == 0) {
            D_coord.x = 0;
            D_coord.y = 0;

        }
        else {
            D_coord.x = D_coord.x + output.x;
            D_coord.y = D_coord.y + output.y;

        }
        Delta.push_back(D_coord);


        rez_vec_2_x = rez_vec_1_x;
        rez_vec_2_y = rez_vec_1_y;
        rez_vec_1_x = floor(D_coord.x);
        rez_vec_1_y = floor(D_coord.y);

        K = (rez_vec_2_y - rez_vec_1_y) / ((rez_vec_2_x - rez_vec_1_x) + 0.000001);


        // 1
        if ((rez_vec_2_x >= rez_vec_1_x) && (rez_vec_2_y >= rez_vec_1_y)) Mean_Alfa = floor((atan((K)) * 180 / pi));
        // 2
        if ((rez_vec_1_x > rez_vec_2_x) && (rez_vec_2_y >= rez_vec_1_y)) Mean_Alfa = 180 - fabs(floor((atan((K)) * 180 / pi)));
        //3
        if ((rez_vec_1_x >= rez_vec_2_x) && (rez_vec_1_y > rez_vec_2_y)) Mean_Alfa = 180 + fabs(floor((atan((K)) * 180 / pi)));
        //4
        if ((rez_vec_2_x > rez_vec_1_x) && (rez_vec_1_y > rez_vec_2_y)) Mean_Alfa = 360 - fabs(floor((atan((K)) * 180 / pi)));
        //
        if (((Mean_Alfa >= 337) && (Mean_Alfa <= 360)) || ((Mean_Alfa >= 0) && (Mean_Alfa < 22.5))) Direction = 1;
        if ((Mean_Alfa >= 22.5) && (Mean_Alfa < 67.5)) Direction = 8;
        if ((Mean_Alfa >= 67.5) && (Mean_Alfa < 112.5)) Direction = 7;
        if ((Mean_Alfa >= 112.5) && (Mean_Alfa < 157.5)) Direction = 6;
        if ((Mean_Alfa >= 157.5) && (Mean_Alfa < 202.5)) Direction = 5;
        if ((Mean_Alfa >= 202.5) && (Mean_Alfa < 247.5)) Direction = 4;
        if ((Mean_Alfa >= 247.5) && (Mean_Alfa < 292.5)) Direction = 3;
        if ((Mean_Alfa >= 292.5) && (Mean_Alfa < 337.5)) Direction = 2;
        call++;
    }
}


void SwypeDetect::Swype_Data(int Dir) // logic for entering swype numbers
{
	switch (Dir) {
		case 1:
            switch (Swype_Numbers_Get.back()) {
                case 1:
                    Swype_Numbers_Get.push_back(4);
                    break;
                case 2:
                    Swype_Numbers_Get.push_back(5);
                    break;
                case 3:
                    Swype_Numbers_Get.push_back(6);
                    break;
                case 4:
                    Swype_Numbers_Get.push_back(7);
                    break;
                case 5:
                    Swype_Numbers_Get.push_back(8);
                    break;
                case 6:
                    Swype_Numbers_Get.push_back(9);
                    break;
                default:
                    Swype_Numbers_Get.push_back(0);
            }
            break;
        case 2:
            switch (Swype_Numbers_Get.back()) {
                case 1:
                    Swype_Numbers_Get.push_back(5);
                    break;
                case 2:
                    break;
                case 4:
                    Swype_Numbers_Get.push_back(8);
                    break;
                case 5:
                    Swype_Numbers_Get.push_back(9);
                    break;
                default:
                    Swype_Numbers_Get.push_back(0);
            }
            break;
        case 3:
            switch (Swype_Numbers_Get.back()) {
                case 1:
                    Swype_Numbers_Get.push_back(2);
                    break;
                case 2:
                    Swype_Numbers_Get.push_back(3);
                    break;
                case 4:
                    Swype_Numbers_Get.push_back(5);
                    break;
                case 5:
                    Swype_Numbers_Get.push_back(6);
                    break;
                case 7:
                    Swype_Numbers_Get.push_back(8);
                    break;
                case 8:
                    Swype_Numbers_Get.push_back(9);
                    break;
                default:
                    Swype_Numbers_Get.push_back(0);
            }
            break;
        case 4:
            switch (Swype_Numbers_Get.back()) {
                case 4:
                    Swype_Numbers_Get.push_back(2);
                    break;
                case 5:
                    Swype_Numbers_Get.push_back(3);
                    break;
                case 7:
                    Swype_Numbers_Get.push_back(5);
                    break;
                case 8:
                    Swype_Numbers_Get.push_back(6);
                    break;
                default:
                    Swype_Numbers_Get.push_back(0);
            }
            break;
        case 5:
            switch (Swype_Numbers_Get.back()) {
                case 4:
                    Swype_Numbers_Get.push_back(1);
                    break;
                case 5:
                    Swype_Numbers_Get.push_back(2);
                    break;
                case 6:
                    Swype_Numbers_Get.push_back(3);
                    break;
                case 7:
                    Swype_Numbers_Get.push_back(4);
                    break;
                case 8:
                    Swype_Numbers_Get.push_back(5);
                    break;
                case 9:
                    Swype_Numbers_Get.push_back(6);
                    break;
                default:
                    Swype_Numbers_Get.push_back(0);
            }
            break;
        case 6:
            switch (Swype_Numbers_Get.back()) {
                case 5:
                    Swype_Numbers_Get.push_back(1);
                    break;
                case 6:
                    Swype_Numbers_Get.push_back(2);
                    break;
                case 8:
                    Swype_Numbers_Get.push_back(4);
                    break;
                case 9:
                    Swype_Numbers_Get.push_back(5);
                    break;
                default:
                    Swype_Numbers_Get.push_back(0);
            }
            break;
        case 7:
            switch (Swype_Numbers_Get.back()) {
                case 2:
                    Swype_Numbers_Get.push_back(1);
                    break;
                case 3:
                    Swype_Numbers_Get.push_back(2);
                    break;
                case 5:
                    Swype_Numbers_Get.push_back(4);
                    break;
                case 6:
                    Swype_Numbers_Get.push_back(5);
                    break;
                case 8:
                    Swype_Numbers_Get.push_back(7);
                    break;
                case 9:
                    Swype_Numbers_Get.push_back(8);
                    break;
                default:
                    Swype_Numbers_Get.push_back(0);
            }
            break;
        case 8:
            switch (Swype_Numbers_Get.back()) {
                case 2:
                    Swype_Numbers_Get.push_back(4);
                    break;
                case 3:
                    Swype_Numbers_Get.push_back(5);
                    break;
                case 5:
                    Swype_Numbers_Get.push_back(7);
                    break;
                case 6:
                    Swype_Numbers_Get.push_back(8);
                    break;
                default:
                    Swype_Numbers_Get.push_back(0);
            }
            break;
    }
}

SwypeDetect::SwypeDetect() // initialization
{
    ocl::setUseOpenCL(true);

    call = 0;
    count_num = -1;
    S = 0;
    Swype_Numbers_Get.clear();
    Swype_Numbers_Get.resize(0);
    Delta.clear();
    Delta.resize(0);
    DirectionS.clear();
    DirectionS.resize(0);
    D_coord.x = 0;
    D_coord.y = 0;
    Direction = 0;

    Delta.clear();
    Delta.resize(0);

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
    int r;
	swype_Numbers.clear();
	swype_Numbers.resize(0);
    if (swype != "") {
        for (int i = 0; i < swype.length(); i++) {
            t = swype.at(i);
            j = atoi(&t);
            swype_Numbers.push_back(j);
        }
        r = swype_Numbers.front();
        Swype_Numbers_Get.push_back(r);
        count_num = 0;
    }
}

void SwypeDetect::processFrame(Mat frame, int &state, int &index, int &x, int &y, int &debug) // main logic
{
	Point2d shift;
    int Max_d = 0;

    shift = Frame_processor1(frame);

    Delta_Calculation(shift);

    if (S == 0) {
        if ((fabs(D_coord.x) > 3) || (fabs(D_coord.y) > 3)) S = CircleDetection(); 
        state = S;
    }
    else if (S == 1) {
        S1_processor();
    }
    else if (S == 2) {
        if (fl_dir) {
            DirectionS.push_back(Direction);
            fl_dir = false;
            if (DirectionS.size() >= 20) {
                for(int i = 0; i < DirectionS.size(); i++){
                    Dir_count[DirectionS.at(i)-1]++;
                }
                for(int j = 0; j <8; j++){
                    if(Dir_count[j]>= Max_d){
                        Max_d = Dir_count[j];
                        Dir_m = j + 1;
                    }
                }
                for(int l = 0; l<8; l++) Dir_count[l] = 0;
                Swype_Data(Dir_m);
                count_num++;

                if((Swype_Numbers_Get.back() != 0)&&(Swype_Numbers_Get.back() == swype_Numbers[count_num])){

                    index = count_num +1;
                    x = static_cast<int>(floor(koord_Sw_points[swype_Numbers[count_num]].x));
                    y = static_cast<int>(floor(koord_Sw_points[swype_Numbers[count_num]].y));
                    if(Swype_Numbers_Get.size() == swype_Numbers.size()) S = 3;
                    else if (Swype_Numbers_Get.size() > swype_Numbers.size()) Reset();
                }
                else Reset();
                DirectionS.clear();
                DirectionS.resize(0);
            }

        }
    }
    state = S;
    index = count_num + 1;
    debug = Direction;

}

void SwypeDetect::processFrame(const unsigned char *frame_i, int width_i, int height_i, int &state, int &index, int &x, int &y, int &debug)
{
    Point2d shift;
    int Max_d = 0;

    //NW21 convert

    Mat frame(height_i+height_i/2 , width_i, CV_8UC1, (uchar *)frame_i);

    shift = Frame_processor(frame);

    Delta_Calculation(shift);
	
    if (S == 0) {
        if ((fabs(D_coord.x) > 3) || (fabs(D_coord.y) > 3)) S = CircleDetection(); 
    }
    else if (S == 1) {
        S1_processor();
    }
    else if (S == 2) {
        if (fl_dir) {
            DirectionS.push_back(Direction);
            fl_dir = false;
            if (DirectionS.size() >= 20) {
                for(int i = 0; i < DirectionS.size(); i++){
                    Dir_count[DirectionS.at(i)-1]++;
                }
                for(int j = 0; j <8; j++){
                    if(Dir_count[j]>= Max_d){
                        Max_d = Dir_count[j];
                        Dir_m = j + 1;
                    }
                }
				for(int l = 0; l<8; l++) Dir_count[l] = 0;
                Swype_Data(Dir_m);
                count_num++;

                if((Swype_Numbers_Get.back() != 0)&&(Swype_Numbers_Get.back() == swype_Numbers[count_num])){

                    index = count_num +1;
                    x = static_cast<int>(floor(koord_Sw_points[swype_Numbers[count_num]].x));
                    y = static_cast<int>(floor(koord_Sw_points[swype_Numbers[count_num]].y));
                    if(Swype_Numbers_Get.size() == swype_Numbers.size()) S = 3;
                    else if (Swype_Numbers_Get.size() > swype_Numbers.size()) Reset();
                }
                else Reset();*/
                DirectionS.clear();
                DirectionS.resize(0);
            }

        }
    }
    index = count_num + 1;
    debug = Direction;

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

    //cvtColor(b_frame, frame1, CV_RGB2GRAY);// converting frames to CV_64F type
    b_frame.copyTo(frame1);


    if (buf1ft.empty()) {
        frame1.convertTo(buf2ft, CV_64F);//Преобразование фреймов в тип CV_64F
        buf1ft = buf2ft.clone();
        koord_Sw_points = Koord_Swipe_Points(frame1.cols, frame1.rows); // we get the coordinates of the swipe points
    }
    else {
        buf1ft = buf2ft.clone();
        frame1.convertTo(buf2ft, CV_64F);//converting frames to CV_64F type
    }
    if (hann.empty()) {
        createHanningWindow(hann, buf1ft.size(), CV_64F); //  create Hanning window
    }
    shift = phaseCorrelate(buf1ft, buf2ft, hann); // we calculate a phase offset vector

    return shift;
}

Point2d SwypeDetect::Frame_processor1(cv::Mat &frame_i)
{
    Point2d shift;

    UMat b_frame;

    frame_i.convertTo(b_frame, frame_i.depth());

    cvtColor(b_frame, frame1, CV_RGB2GRAY);
    //b_frame.copyTo(frame1);


    if (buf1ft.empty()) {
        frame1.convertTo(buf2ft, CV_64F);//converting frames to CV_64F type
        buf1ft = buf2ft.clone();
        koord_Sw_points = Koord_Swipe_Points(frame1.cols, frame1.rows); // we get the coordinates of the swipe points
    }
    else {
        buf1ft = buf2ft.clone();
        frame1.convertTo(buf2ft, CV_64F);//converting frames to CV_64F type
    }
    if (hann.empty()) {
        createHanningWindow(hann, buf1ft.size(), CV_64F);  //  create Hanning window
    }
    shift = phaseCorrelate(buf1ft, buf2ft, hann); // we calculate a phase offset vector

    return shift;
}

void SwypeDetect::S1_processor(void)
{

    if (!swype_Numbers.empty()) {
        Delta.clear();
        Delta.resize(0);
        DirectionS.clear();
        DirectionS.resize(0);
        D_coord.x = 0;
        D_coord.y = 0;
        Direction = 0;
        S = 2; // if we have swype then we go to detection swype from video
    }
}
