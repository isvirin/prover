function [NewImg, Alfa, ImShow]= shift_detection(original, distorted,frameGray2,frameRGB)

ptsOriginal  = detectSURFFeatures(original);
ptsDistorted = detectSURFFeatures(distorted);

[featuresOriginal,  validPtsOriginal]  = extractFeatures(original,  ptsOriginal);
[featuresDistorted, validPtsDistorted] = extractFeatures(distorted, ptsDistorted);

indexPairs = matchFeatures(featuresOriginal, featuresDistorted);

matchedOriginal  = validPtsOriginal(indexPairs(:,1));
matchedDistorted = validPtsDistorted(indexPairs(:,2));

%     figure;
%     showMatchedFeatures(original,distorted,matchedOriginal,matchedDistorted);
%     title('Putatively matched points (including outliers)');

[tform, inlierDistorted, inlierOriginal] = estimateGeometricTransform(matchedDistorted, matchedOriginal, 'similarity');

%    figure;
%    showMatchedFeatures(original,distorted,inlierOriginal,inlierDistorted);
%    title('Matching points (inliers only)');
%    legend('ptsOriginal','ptsDistorted');


rez_vec_1_x=floor(inlierOriginal.Location(:,2));
rez_vec_1_y=floor(inlierOriginal.Location(:,1));
rez_vec_2_x=floor(inlierDistorted.Location(:,2));
rez_vec_2_y=floor(inlierDistorted.Location(:,1));

K=(rez_vec_2_y-rez_vec_1_y)./((rez_vec_2_x-rez_vec_1_x)+0.000001);

colich=inlierOriginal.Count;
Alfa=[];
for tt=1:colich
    %% 1
    if (rez_vec_2_x(tt)>=rez_vec_1_x(tt))&&(rez_vec_2_y(tt)>=rez_vec_1_y(tt))
        Alfa(tt)=floor((atan((K(tt))).*180./pi));
    end
    
    %% 2
    if (rez_vec_1_x(tt)>rez_vec_2_x(tt))&&(rez_vec_2_y(tt)>=rez_vec_1_y(tt))
        Alfa(tt)=180-abs(floor((atan((K(tt))).*180./pi)));
    end
    
    %% 3
    if (rez_vec_1_x(tt)>=rez_vec_2_x(tt))&&(rez_vec_1_y(tt)>rez_vec_2_y(tt))
        Alfa(tt)=180+abs(floor((atan((K(tt))).*180./pi)));
    end
    
    %% 4
    if (rez_vec_2_x(tt)>rez_vec_1_x(tt))&&(rez_vec_1_y(tt)>rez_vec_2_y(tt))
        Alfa(tt)=360-abs(floor((atan((K(tt))).*180./pi)));
    end
    
end

Mean_Alfa=(median(Alfa));

[ImShow,Imm]= direction(Mean_Alfa,frameGray2,frameRGB);

NewImg=frameRGB;
% [aa bb cc]=size(FrameMov);
% NewImg(1:aa,1:bb,1:3)=FrameMov;
%   figure, imshow(New_img)