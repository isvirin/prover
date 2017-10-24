
function [flag_R, S] = CircleDetection(deltaXX, deltaYY,frame1, S)

length_deltaXX=length(deltaXX);
Time=[];
flag=0;
flag_R='No_Circle';
S=0;
for i=1:length_deltaXX
    
    if (deltaXX(i)>(size(frame1,1)))&&(deltaYY(i)>(size(frame1,2)))
        flag=1;
    end
    
    if (deltaXX(i)<(size(frame1,1))/2)&&(deltaYY(i)<(size(frame1,2))/2)&&(flag==1)
        C = xcorr(deltaXX,deltaYY,'coeff');
        for j=1:i
            if (abs(C(j))>0.75)
                flag_R='Circle';
                S=1;
            end
        end
    end
end


end