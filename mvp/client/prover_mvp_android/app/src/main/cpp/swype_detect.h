// @author Viacheslav Voronin
//
// I. Swype-code domain definition
//
// Swype-code is defined in the domain of 9 virtual points placed in 3x3 grid.
// Points are numbered from 1 to 9 starting from the top left corner (point 1)
// from left to right, then from top downward.
//
// II. Use cases
//
// 1. Approval of video file being captured. In this case we don't know
//    swype-code to be entered. The algorithm just should detect circular
//    motion "okey, prover" and notify about it. Only after that the swype-code
//    will be provided, and the algorithm should start to detect it.
// 2. Verification of swype-code presence in a given file. In this case a
//    swype-code is known and the goal is to find the code in the video stream.
//    The algorithm should detect circular motion "okey, prover" and then
//    search for the code within some predefined amount of time depending on
//    the code length (for example, 2 seconds for each code digit).
//
// It's suggested to implement both use cases in the single algorithm.
//
// III. General information
//
// Input: sequence of video frames (parameters? minimum possible resolution?
// color space etc...) with FPS no less than N (what's the minimum?)
//
// The algorithm behaves like a state machine, state transitions happen after
// processing a successive video frame. The following states are suggested
// (a state may have an associated parameter or set of parameters - given in
// brackets):
// * S0 (no): awaiting for circular motion "okey, prover";
// * S1 (no): circular motion is detected, the algorithm awaits for setting
//   a swype-code (this state is available for the first use-case only, when
//   the swype-code was not set yet; transition to S2 occurs after setting
//   a swype-code);
// * S2 (index of a last recognized symbol in the code, coordinates of the
//   current point of the image for trajectory visualization): entering
//   a swype-code is in progess.
// * S3 (no): swype-code entering is finished.
//
// IV. Algorithm description
//
// Initialization. The following parameters are provided: frame resolution,
// fps, swype-code (optional).
//
// Algorithm workflow:
// * Start from state S0. Right after recognition of circular motion transit to
//   one of the state:
//   - S1, if the swype-code was not specified at initialization. Immediately
//     after setting a swype-code (separate function that may be called
//     asynchronously) the algorithm transits into state S2 after receiving
//     a successive video frame.
//   - S2 (0), if the swype-code was given at initialization.
// * start to recognize a trajectory, return S2(i) and trajectory point
//   coordinates for visualization with every given video frame.
// * If the trajectory during the input fall into zone of another digit (not
//   the expected one), fall back to state S0. If swype-code was not set at
//   initialization, it should be reset too.
// * If entering a swype-code is not finished within 2*N seconds interval
//   (N denotes number of characters in the swype-code), fall back to state S0
//   and reset the swype-code provided it wasn't set at initialization. Time
//   calculation shall use FPS specified at initialization.
// * If entering a swype-code is finished, transit to state S3.
//
// V. Expected API
//
// /** @brief Initialization
//  *  @param width video frame width (pixels)
//  *  @param height video frame height (pixels)
//  *  @param fps frames per second
//  *  @param swype optional swype-code
//  */
// void init(
//    int         width,
//    int         height,
//    int         fps,
//    std::string swype="");
//
// /** @brief Set swype-code. Only for the case when the swype-code was not set
//  *         via init()
//  *  @param swype swype-code
//  */
// void setSwype(std::string swype);
//
// /** @brief Process single video frame
//  *  @param frame buffer with video frame data
//  *  @param state [out] state S
//  *  @param index [out] only for state==2, index of recognized digit of the
//  *         swype-code
//  *  @param x, y [out] only for state==2, trajectory coordinates for
//  *         visualization
//  */
// void processFrame(
//    const char *frame,
//    int        &state,
//    int        &index,
//    int        &x,
//    int        &y);
#pragma once


#include <opencv2/opencv.hpp>
#include <vector>
#include <cmath>
#include <ctime>
#include <cstdlib>
#include <cstring>
//#include <iostream>
#include "opencv2/core/ocl.hpp"


class SwypeDetect {
public:

    SwypeDetect();
    ~SwypeDetect();

    void init(int fps_e, std::string swype);
    void setSwype(std::string swype);

    void processFrame(cv::Mat frame, int &state, int &index, int &x, int &y, int &debug);

    void
    processFrame(const unsigned char *frame_i, int width_i, int height_i, int &state, int &index,
                 int &x, int &y, int &debug);
    void Reset(void);

private:

    //External data
    std::vector<int> swype_Numbers;
    int fps;

    //Internal data
    cv::UMat frame1; //previous frame
    std::vector<cv::Point2d> Delta; //динамический массив перемещений камеры

    int S; //Текущий этап распознования
    int call; //Колличество вызовов функции обработки кадров
    int count_num; //Колличество правильно введенных свайп-цифр
    std::vector<int> Swype_Numbers_Get; //Введеные цифры свайпкода
    std::vector<cv::Point2d> Swype_Koord; //Координаты введенного свайпкода
    std::vector<int> DirectionS; //Массив направлений

    cv::Point2d D_coord;
    int Direction;
    bool fl_dir;

    std::vector<cv::Point2d> koord_Sw_points;
    cv::UMat buf1ft;
    cv::UMat buf2ft;
    cv::UMat hann;

    int CircleDetection(void);
    std::vector<cv::Point2d> Koord_Swipe_Points(int width, int height);
    void Delta_Calculation(cv::Point2d output);

    void Swype_Data(std::vector<cv::Point2d> &koord);
    cv::Point2d Frame_processor(cv::Mat &frame_i);

    cv::Point2d Frame_processor1(cv::Mat &frame_i);
    void S1_processor(void);
    std::vector<double> S_L_define(cv::Point2d a, cv::Point2d b);
};

