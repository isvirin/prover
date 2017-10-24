%%%%%%%%%%%%%%%%%% визуализация обнаружения кругового движения
close all
clear all

vidObj = VideoReader('test.mp4');
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
vidHeight = vidObj.Height;
vidWidth = vidObj.Width;
frameRate = vidObj.FrameRate;

get(vidObj)
nframes = floor(vidObj.Duration.*frameRate);
I = read(vidObj, 1);
Frame_rez = zeros([size(I,1) size(I,2) 3 nframes], class(I));

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% параметры
step_frame=5;
step=1;
DirectionMov=[];
count=1;
count_sum=0;
ImShowMed=[];
Degree=[];
deltaXX=[];
deltaYY=[];
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

for k = 1:step:nframes-(step_frame+1)
    count_sum=count_sum+1;
    frameRGB = read(vidObj, k);
    frameGray1 = rgb2gray(frameRGB);
    frameRGB_rot1=frameGray1;
    
    cur_frame = k+step_frame;
    frameRGB = read(vidObj, cur_frame);
    frameGray2 = rgb2gray(frameRGB);
    frameRGB_rot2=frameGray2;
    
    original=frameRGB_rot1;
    distorted=frameRGB_rot2;
    
    buf1ft=original;
    buf2ft=distorted;
    usfac = 100;
    
    [output] = Phase_Cor(fft2(buf1ft),fft2(buf2ft),usfac);

    [deltaX, deltaY, deltaXX, deltaYY, K, Mean_Alfa, Direction]=Delta_calculation(output,k, deltaXX, deltaYY);
    S=0;
   
    if ((abs(deltaX))>3)||((abs(deltaY))>3)
        [flag_R, S] = CircleDetection(deltaXX, deltaYY, buf1ft, S);        
    end
end

flag_R