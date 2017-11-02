function [koord_x, koord_y, Numbers] = Koord_Swipe_points(width,height)
koord_x(1)=2*width/8;
koord_y(1)=2*height/8;

koord_x(2)=4*width/8;
koord_y(2)=2*height/8;

koord_x(3)=6*width/8;
koord_y(3)=2*height/8;

koord_x(4)=2*width/8;
koord_y(4)=4*height/8;

koord_x(5)=4*width/8;
koord_y(5)=4*height/8;

koord_x(6)=6*width/8;
koord_y(6)=4*height/8;

koord_x(7)=2*width/8;
koord_y(7)=6*height/8;

koord_x(8)=4*width/8;
koord_y(8)=6*height/8;

koord_x(9)=6*width/8;
koord_y(9)=6*height/8;


Numbers(1)=1;
Numbers(2)=2;
Numbers(3)=3;
Numbers(4)=4;
Numbers(5)=5;
Numbers(6)=6;
Numbers(7)=7;
Numbers(8)=8;
Numbers(9)=9;

end