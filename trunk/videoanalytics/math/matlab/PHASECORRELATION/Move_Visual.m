%%%%%%%%%%%%%%%%%% визуализация смещения
close all
clear all

vidObj = VideoReader('test.mp4');
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%DirectionMov=1
%        |
%        |
%       ...
%        .
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%DirectionMov=2
%        \
%         \
%         ...
%           .
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%DirectionMov=3
%          .
%  ________..
%          .
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%DirectionMov=4
%      .
%    ...
%    /
%   /
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%DirectionMov=5
%        .
%       ...
%        |
%        |
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%DirectionMov=6
%        .
%        ...
%          \
%           \
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%DirectionMov=7
%   .
%  ..__________
%   .
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%DirectionMov=8
%      /
%     /
%   ...
%   .
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%DirectionMov=8
%        |
%        |
%       ...
%        .
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
arrow1=imread('arrow1.bmp');
arrow2=imread('arrow2.bmp');
arrow3=imread('arrow3.bmp');
arrow4=imread('arrow4.bmp');
arrow5=imread('arrow5.bmp');
arrow6=imread('arrow6.bmp');
arrow7=imread('arrow7.bmp');
arrow8=imread('arrow8.bmp');

vidHeight = vidObj.Height;
vidWidth = vidObj.Width;
frameRate = vidObj.FrameRate;

get(vidObj)
nframes = floor(vidObj.Duration.*frameRate);
I = read(vidObj, 1);
Frame_rez = zeros([size(I,1) size(I,2) 3 nframes], class(I));

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%параметры
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
        
    NewImg=frameRGB;
    
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    FrameRez(:,:,:,k) = NewImg;
    DirectionMov(count)=Direction;
    ImShowMed(count_sum)=Direction;
    count=count+1;
    Degree(count)=Mean_Alfa;
    
    
    if ((abs(deltaX))>3)||((abs(deltaY))>3)
        if Direction==1
            FrameMov=imresize(arrow1, [80, size(arrow1, 2)*80/size(arrow1, 1)]);
        end
        if Direction==2
            FrameMov=imresize(arrow2, [80, size(arrow2, 2)*80/size(arrow2, 1)]);
        end
        if Direction==3
            FrameMov=imresize(arrow3, [80, size(arrow3, 2)*80/size(arrow3, 1)]);
        end
        if Direction==4
            FrameMov=imresize(arrow4, [80, size(arrow4, 2)*80/size(arrow4, 1)]);
        end
        if Direction==5
            FrameMov=imresize(arrow5, [80, size(arrow5, 2)*80/size(arrow5, 1)]);
        end
        if Direction==6
            FrameMov=imresize(arrow6, [80, size(arrow6, 2)*80/size(arrow6, 1)]);
        end
        if Direction==7
            FrameMov=imresize(arrow7, [80, size(arrow7, 2)*80/size(arrow7, 1)]);
        end
        if Direction==8
            FrameMov=imresize(arrow8, [80, size(arrow8, 2)*80/size(arrow8, 1)]);
        end
        
        NewImg(1:size(FrameMov,1),1:size(FrameMov,2),1:3)=FrameMov;
        FrameRez(:,:,:,k) = NewImg;
    end
    
end

implay(FrameRez,frameRate);
% DirectionMov
% ImShowRez
% figure, plot(DirectionMov)
% figure, plot(ImShowRez)
% figure, plot(Degree)
% figure, hist(Degree)
% figure, plot(deltaXX,deltaYY)
% figure, plot(deltaXX)
% figure, plot(deltaYY)
