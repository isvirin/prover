function [ImShow,Imm]= direction(alfa,frameGray2,frameRGB)
PP=frameGray2;
[a b]=size(PP);
a_centr=floor(a/2);
b_centr=floor(b/2);

Imm=frameRGB;

if ((alfa>=337)&&(alfa<=360))||((alfa>=0)&&(alfa<22.5))
    beta=5;
%     S=imread('strelka5.bmp');
end
if (alfa>=22.5)&&(alfa<67.5)
    beta=6;
%     S=imread('strelka6.bmp');
end

if (alfa>=67.5)&&(alfa<112.5)
    beta=7;
%     S=imread('strelka7.bmp');
end

if (alfa>=112.5)&&(alfa<157.5)
    beta=8;
%     S=imread('strelka8.bmp');
end

if (alfa>=157.5)&&(alfa<202.5)
    beta=1;
%     S=imread('strelka1.bmp');
end

if (alfa>=202.5)&&(alfa<247.5)
    beta=2;
%     S=imread('strelka2.bmp');
end

if (alfa>=247.5)&&(alfa<292.5)
    beta=3;
%     S=imread('strelka3.bmp');
end

if (alfa>=292.5)&&(alfa<337.5)
    beta=4;
%     S=imread('strelka4.bmp');
end
% FrameMov=imresize(S, [80, size(S, 2)*80/size(S, 1)]);
ImShow=beta;

