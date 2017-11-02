function [deltaX, deltaY, deltaXX, deltaYY, K, Mean_Alfa, Direction]=Delta_calculation(output,k, deltaXX, deltaYY)
deltaX=output(1,3);
deltaY=output(1,4);

if k==1
    deltaXX(k)=deltaX;
    deltaYY(k)=deltaY;
else
    deltaXX(k)=deltaXX(k-1)+deltaX;
    deltaYY(k)=deltaYY(k-1)+deltaY;
end

rez_vec_2_x=0;
rez_vec_2_y=0;
rez_vec_1_x=floor(deltaX);
rez_vec_1_y=floor(deltaY);

K=(rez_vec_2_y-rez_vec_1_y)./((rez_vec_2_x-rez_vec_1_x)+0.000001);


%% 1
if (rez_vec_2_x>=rez_vec_1_x)&&(rez_vec_2_y>=rez_vec_1_y)
    Mean_Alfa=floor((atan((K)).*180./pi));
end

%% 2
if (rez_vec_1_x>rez_vec_2_x)&&(rez_vec_2_y>=rez_vec_1_y)
    Mean_Alfa=180-abs(floor((atan((K)).*180./pi)));
end

%% 3
if (rez_vec_1_x>=rez_vec_2_x)&&(rez_vec_1_y>rez_vec_2_y)
    Mean_Alfa=180+abs(floor((atan((K)).*180./pi)));
end

%% 4
if (rez_vec_2_x>rez_vec_1_x)&&(rez_vec_1_y>rez_vec_2_y)
    Mean_Alfa=360-abs(floor((atan((K)).*180./pi)));
end

%%

if ((Mean_Alfa>=337)&&(Mean_Alfa<=360))||((Mean_Alfa>=0)&&(Mean_Alfa<22.5))
    Direction=5;
end
if (Mean_Alfa>=22.5)&&(Mean_Alfa<67.5)
    Direction=6;
end
if (Mean_Alfa>=67.5)&&(Mean_Alfa<112.5)
    Direction=7;
end
if (Mean_Alfa>=112.5)&&(Mean_Alfa<157.5)
    Direction=8;
end
if (Mean_Alfa>=157.5)&&(Mean_Alfa<202.5)
    Direction=1;
end
if (Mean_Alfa>=202.5)&&(Mean_Alfa<247.5)
    Direction=2;
end
if (Mean_Alfa>=247.5)&&(Mean_Alfa<292.5)
    Direction=3;
end
if (Mean_Alfa>=292.5)&&(Mean_Alfa<337.5)
    Direction=4;
end

end