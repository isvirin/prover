S=0;
call=0;
count=0;
count_sum=0;
count_direction=0;
count_num=1;
ImShowMed=[];
deltaXX=[];
deltaYY=[];
Swype_Numbers=[];
Swype_KoordX=[];
Swype_KoordY=[];
Swype_Numbers(1)=Numbers(1);
Swype_KoordX(1)=koord_x(1);
Swype_KoordY(1)=koord_y(1);
%frame1
%frame2
%width
%height
%fps
%swype

[S, call] = processFrame(frame1,frame2, width, height, fps, swype, S, call, count,Swype_KoordX, Swype_KoordY, Swype_Numbers, deltaXX, deltaYY, count_num, count_direction,count_sum);