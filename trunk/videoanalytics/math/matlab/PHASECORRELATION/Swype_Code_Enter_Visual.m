%%% визуализация ввода свайп кода
close all
clear all

vidObj = VideoReader('test.mp4');
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
vidHeight = vidObj.Height;
vidWidth = vidObj.Width;
frameRate = vidObj.FrameRate;

get(vidObj)
nframes = floor(vidObj.Duration.*frameRate);
I = read(vidObj, 1);
Frame_rez = zeros([size(I,1) size(I,2) 3 nframes], class(I));

deltaXX=[];
deltaYY=[];

[koord_x, koord_y, Numbers] = Koord_Swipe_points(vidHeight,vidWidth);
%%%%%%%%%  координаты для визуализации
% 1 2 3
% 4 5 6
% 7 8 9

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%параметры
step_frame=5;
step=1;
DirectionMov=[];
count=1;
count_sum=0;
count_direction=0;
count_num=1;
ImShowMed=[];
Degree=[];
deltaXX=[];
deltaYY=[];
Swype_Numbers=[];
Swype_KoordX=[];
Swype_KoordY=[];

Swype_Numbers(1)=Numbers(1);
Swype_KoordX(1)=koord_x(1);
Swype_KoordY(1)=koord_y(1);
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


for k = 1:step:nframes-(step_frame+1)
    count=count+1;
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
    
    if ((abs(deltaX))>3)||((abs(deltaY))>3)
        count_direction=count_direction+1;
        Direction_S(count_direction)=Direction;
        count_sum=count_sum+1;
    end
    
    
    if (count_sum>3)&&(Direction_S(count_direction)==Direction_S(count_direction-1))&&(Direction_S(count_direction-1)==Direction_S(count_direction-2))
        % символ получен
        count_num=count_num+1;
        
        % Swype_Numbers==0 выход за границу экрана
        if Swype_Numbers(count_num-1)==0
            Swype_Numbers(count_num)=Numbers(1);
            Swype_KoordX(count_num)=koord_x(1);
            Swype_KoordY(count_num)=koord_y(1);   
        else
        [Swype_Numbers, Swype_KoordX, Swype_KoordY] = Swype_Data(Swype_Numbers, Swype_KoordX, Swype_KoordY, count_direction,Direction_S,count_num, Numbers,koord_x, koord_y);
        end
        count_sum=0;
    end
    
    if count_sum>3
        count_sum=0;
    end
    Direction_S
    Swype_Numbers
end