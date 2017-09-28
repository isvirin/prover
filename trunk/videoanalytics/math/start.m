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
vidHeight = vidObj.Height;
vidWidth = vidObj.Width;
frameRate = vidObj.FrameRate;

get(vidObj)
% nframes = vidObj.NumberOfFrames;
nframes = floor(vidObj.Duration.*frameRate);
I = read(vidObj, 1);
Frame_rez = zeros([size(I,1) size(I,2) 3 nframes], class(I));

step_frame=5;
plot_angle=0;
step=1;
k=30;
DirectionMov=[];
count=1;
for k = 1:step:nframes-(step_frame+1)
    frameRGB = read(vidObj, k);
    frameGray1 = rgb2gray(frameRGB);
    frameRGB_rot1=frameGray1;
    
    cur_frame = k+step_frame;
    frameRGB = read(vidObj, cur_frame);
    frameGray2 = rgb2gray(frameRGB);
    frameRGB_rot2=frameGray2;
    
    original=frameRGB_rot1;
    distorted=frameRGB_rot2;
    
    [NewImg, Alfa, ImShow] = shift_detection(original, distorted,frameGray2,frameRGB);
    FrameRez(:,:,:,k) = NewImg;
    DirectionMov(count)=ImShow;
    count=count+1;
end
DirectionMov
implay(FrameRez,frameRate);

