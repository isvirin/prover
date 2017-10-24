function [deltaXX, deltaYY, Direction] = frameMov(frame1,frame2,call)
frameRGB = frame1;
frameGray1 = rgb2gray(frameRGB);

frameRGB = frame2;
frameGray2 = rgb2gray(frameRGB);

original=frameGray1;
distorted=frameGray2;

buf1ft=original;
buf2ft=distorted;
usfac = 100;
[output] = Phase_Cor(fft2(buf1ft),fft2(buf2ft),usfac);
[deltaX, deltaY, deltaXX, deltaYY, K, Mean_Alfa, Direction]=Delta_calculation(output,k, deltaXX, deltaYY);

end


