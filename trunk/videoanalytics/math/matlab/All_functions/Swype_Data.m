function [Swype_Numbers, Swype_KoordX, Swype_KoordY] = Swype_Data(Swype_Numbers, Swype_KoordX, Swype_KoordY, count_direction,Direction_S,count_num, Numbers,koord_x, koord_y)
 %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        if Direction_S(count_direction)==1
  
            if Swype_Numbers(count_num-1)==1
                Swype_Numbers(count_num)=Numbers(4);
                Swype_KoordX(count_num)=koord_x(4);
                Swype_KoordY(count_num)=koord_y(4);
            end
            
            if Swype_Numbers(count_num-1)==2
                Swype_Numbers(count_num)=Numbers(5);
                Swype_KoordX(count_num)=koord_x(5);
                Swype_KoordY(count_num)=koord_y(5);
            end
            
            if Swype_Numbers(count_num-1)==3
                Swype_Numbers(count_num)=Numbers(6);
                Swype_KoordX(count_num)=koord_x(6);
                Swype_KoordY(count_num)=koord_y(6);
            end
            
            if Swype_Numbers(count_num-1)==4
                Swype_Numbers(count_num)=Numbers(7);
                Swype_KoordX(count_num)=koord_x(7);
                Swype_KoordY(count_num)=koord_y(7);
            end
            
            if Swype_Numbers(count_num-1)==5
                Swype_Numbers(count_num)=Numbers(8);
                Swype_KoordX(count_num)=koord_x(8);
                Swype_KoordY(count_num)=koord_y(8);
            end
            
            if Swype_Numbers(count_num-1)==6
                Swype_Numbers(count_num)=Numbers(9);
                Swype_KoordX(count_num)=koord_x(9);
                Swype_KoordY(count_num)=koord_y(9);
            end
            
            if Swype_Numbers(count_num-1)==7
                Swype_Numbers(count_num)=0;
                Swype_KoordX(count_num)=0;
                Swype_KoordY(count_num)=0;
            end
            
            if Swype_Numbers(count_num-1)==8
                Swype_Numbers(count_num)=0;
                Swype_KoordX(count_num)=0;
                Swype_KoordY(count_num)=0;
            end
            
            if Swype_Numbers(count_num-1)==9
                Swype_Numbers(count_num)=0;
                Swype_KoordX(count_num)=0;
                Swype_KoordY(count_num)=0;
            end
        end
        %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        if Direction_S(count_direction)==2
            
            if Swype_Numbers(count_num-1)==1
                Swype_Numbers(count_num)=Numbers(5);
                Swype_KoordX(count_num)=koord_x(5);
                Swype_KoordY(count_num)=koord_y(5);
            end
            
            if Swype_Numbers(count_num-1)==2
                Swype_Numbers(count_num)=Numbers(6);
                Swype_KoordX(count_num)=koord_x(6);
                Swype_KoordY(count_num)=koord_y(6);
            end
            
            if Swype_Numbers(count_num-1)==3
                Swype_Numbers(count_num)=0;
                Swype_KoordX(count_num)=0;
                Swype_KoordY(count_num)=0;
            end
            
            if Swype_Numbers(count_num-1)==4
                Swype_Numbers(count_num)=Numbers(8);
                Swype_KoordX(count_num)=koord_x(8);
                Swype_KoordY(count_num)=koord_y(8);
            end
            
            if Swype_Numbers(count_num-1)==5
                Swype_Numbers(count_num)=Numbers(9);
                Swype_KoordX(count_num)=koord_x(9);
                Swype_KoordY(count_num)=koord_y(9);
            end
            
            if Swype_Numbers(count_num-1)==6
                Swype_Numbers(count_num)=0;
                Swype_KoordX(count_num)=0;
                Swype_KoordY(count_num)=0;
            end
            
            if Swype_Numbers(count_num-1)==7
                Swype_Numbers(count_num)=0;
                Swype_KoordX(count_num)=0;
                Swype_KoordY(count_num)=0;
            end
            
            if Swype_Numbers(count_num-1)==8
                Swype_Numbers(count_num)=0;
                Swype_KoordX(count_num)=0;
                Swype_KoordY(count_num)=0;
            end
            
            if Swype_Numbers(count_num-1)==9
                Swype_Numbers(count_num)=0;
                Swype_KoordX(count_num)=0;
                Swype_KoordY(count_num)=0;
            end
        end
        %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        if Direction_S(count_direction)==3
            
            if Swype_Numbers(count_num-1)==1
                Swype_Numbers(count_num)=Numbers(2);
                Swype_KoordX(count_num)=koord_x(2);
                Swype_KoordY(count_num)=koord_y(2);
            end
            
            if Swype_Numbers(count_num-1)==2
                Swype_Numbers(count_num)=Numbers(3);
                Swype_KoordX(count_num)=koord_x(3);
                Swype_KoordY(count_num)=koord_y(3);
            end
            
            if Swype_Numbers(count_num-1)==3
                Swype_Numbers(count_num)=0;
                Swype_KoordX(count_num)=0;
                Swype_KoordY(count_num)=0;
            end
            
            if Swype_Numbers(count_num-1)==4
                Swype_Numbers(count_num)=Numbers(5);
                Swype_KoordX(count_num)=koord_x(5);
                Swype_KoordY(count_num)=koord_y(5);
            end
            
            if Swype_Numbers(count_num-1)==5
                Swype_Numbers(count_num)=Numbers(6);
                Swype_KoordX(count_num)=koord_x(6);
                Swype_KoordY(count_num)=koord_y(6);
            end
            
            if Swype_Numbers(count_num-1)==6
                Swype_Numbers(count_num)=0;
                Swype_KoordX(count_num)=0;
                Swype_KoordY(count_num)=0;
            end
            
            if Swype_Numbers(count_num-1)==7
                Swype_Numbers(count_num)=Numbers(8);
                Swype_KoordX(count_num)=koord_x(8);
                Swype_KoordY(count_num)=koord_y(8);
            end
            
            if Swype_Numbers(count_num-1)==8
                Swype_Numbers(count_num)=Numbers(9);
                Swype_KoordX(count_num)=koord_x(9);
                Swype_KoordY(count_num)=koord_y(9);
            end
            
            if Swype_Numbers(count_num-1)==9
                Swype_Numbers(count_num)=0;
                Swype_KoordX(count_num)=0;
                Swype_KoordY(count_num)=0;
            end
        end
        %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        if Direction_S(count_direction)==4
            
            if Swype_Numbers(count_num-1)==1
                Swype_Numbers(count_num)=0;
                Swype_KoordX(count_num)=0;
                Swype_KoordY(count_num)=0;
            end
            
            if Swype_Numbers(count_num-1)==2
                Swype_Numbers(count_num)=0;
                Swype_KoordX(count_num)=0;
                Swype_KoordY(count_num)=0;
            end
            
            if Swype_Numbers(count_num-1)==3
                Swype_Numbers(count_num)=0;
                Swype_KoordX(count_num)=0;
                Swype_KoordY(count_num)=0;
            end
            
            if Swype_Numbers(count_num-1)==4
                Swype_Numbers(count_num)=Numbers(2);
                Swype_KoordX(count_num)=koord_x(2);
                Swype_KoordY(count_num)=koord_y(2);
            end
            
            if Swype_Numbers(count_num-1)==5
                Swype_Numbers(count_num)=Numbers(3);
                Swype_KoordX(count_num)=koord_x(3);
                Swype_KoordY(count_num)=koord_y(3);
            end
            
            if Swype_Numbers(count_num-1)==6
                Swype_Numbers(count_num)=0;
                Swype_KoordX(count_num)=0;
                Swype_KoordY(count_num)=0;
            end
            
            if Swype_Numbers(count_num-1)==7
                Swype_Numbers(count_num)=Numbers(5);
                Swype_KoordX(count_num)=koord_x(5);
                Swype_KoordY(count_num)=koord_y(5);
            end
            
            if Swype_Numbers(count_num-1)==8
                Swype_Numbers(count_num)=Numbers(6);
                Swype_KoordX(count_num)=koord_x(6);
                Swype_KoordY(count_num)=koord_y(6);
            end
            
            if Swype_Numbers(count_num-1)==9
                Swype_Numbers(count_num)=0;
                Swype_KoordX(count_num)=0;
                Swype_KoordY(count_num)=0;
            end
        end
        %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        if Direction_S(count_direction)==5
            
            if Swype_Numbers(count_num-1)==1
                Swype_Numbers(count_num)=0;
                Swype_KoordX(count_num)=0;
                Swype_KoordY(count_num)=0;
            end
            
            if Swype_Numbers(count_num-1)==2
                Swype_Numbers(count_num)=0;
                Swype_KoordX(count_num)=0;
                Swype_KoordY(count_num)=0;
            end
            
            if Swype_Numbers(count_num-1)==3
                Swype_Numbers(count_num)=0;
                Swype_KoordX(count_num)=0;
                Swype_KoordY(count_num)=0;
            end
            
            if Swype_Numbers(count_num-1)==4
                Swype_Numbers(count_num)=Numbers(1);
                Swype_KoordX(count_num)=koord_x(1);
                Swype_KoordY(count_num)=koord_y(1);
            end
            
            if Swype_Numbers(count_num-1)==5
                Swype_Numbers(count_num)=Numbers(2);
                Swype_KoordX(count_num)=koord_x(2);
                Swype_KoordY(count_num)=koord_y(2);
            end
            
            if Swype_Numbers(count_num-1)==6
                Swype_Numbers(count_num)=Numbers(3);
                Swype_KoordX(count_num)=koord_x(3);
                Swype_KoordY(count_num)=koord_y(3);
            end
            
            if Swype_Numbers(count_num-1)==7
                Swype_Numbers(count_num)=Numbers(4);
                Swype_KoordX(count_num)=koord_x(4);
                Swype_KoordY(count_num)=koord_y(4);
            end
            
            if Swype_Numbers(count_num-1)==8
                Swype_Numbers(count_num)=Numbers(5);
                Swype_KoordX(count_num)=koord_x(5);
                Swype_KoordY(count_num)=koord_y(5);
            end
            
            if Swype_Numbers(count_num-1)==9
                Swype_Numbers(count_num)=Numbers(6);
                Swype_KoordX(count_num)=koord_x(6);
                Swype_KoordY(count_num)=koord_y(6);
            end
        end
        %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        if Direction_S(count_direction)==6
            
            if Swype_Numbers(count_num-1)==1
                Swype_Numbers(count_num)=0;
                Swype_KoordX(count_num)=0;
                Swype_KoordY(count_num)=0;
            end
            
            if Swype_Numbers(count_num-1)==2
                Swype_Numbers(count_num)=0;
                Swype_KoordX(count_num)=0;
                Swype_KoordY(count_num)=0;
            end
            
            if Swype_Numbers(count_num-1)==3
                Swype_Numbers(count_num)=0;
                Swype_KoordX(count_num)=0;
                Swype_KoordY(count_num)=0;
            end
            
            if Swype_Numbers(count_num-1)==4
                Swype_Numbers(count_num)=0;
                Swype_KoordX(count_num)=0;
                Swype_KoordY(count_num)=0;
            end
            
            if Swype_Numbers(count_num-1)==5
                Swype_Numbers(count_num)=Numbers(1);
                Swype_KoordX(count_num)=koord_x(1);
                Swype_KoordY(count_num)=koord_y(1);
            end
            
            if Swype_Numbers(count_num-1)==6
                Swype_Numbers(count_num)=Numbers(2);
                Swype_KoordX(count_num)=koord_x(2);
                Swype_KoordY(count_num)=koord_y(2);
            end
            
            if Swype_Numbers(count_num-1)==7
                Swype_Numbers(count_num)=0;
                Swype_KoordX(count_num)=0;
                Swype_KoordY(count_num)=0;
            end
            
            if Swype_Numbers(count_num-1)==8
                Swype_Numbers(count_num)=Numbers(4);
                Swype_KoordX(count_num)=koord_x(4);
                Swype_KoordY(count_num)=koord_y(4);
            end
            
            if Swype_Numbers(count_num-1)==9
                Swype_Numbers(count_num)=Numbers(5);
                Swype_KoordX(count_num)=koord_x(5);
                Swype_KoordY(count_num)=koord_y(5);
            end
        end
        %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        if Direction_S(count_direction)==7
            
            if Swype_Numbers(count_num-1)==1
                Swype_Numbers(count_num)=0;
                Swype_KoordX(count_num)=0;
                Swype_KoordY(count_num)=0;
            end
            
            if Swype_Numbers(count_num-1)==2
                Swype_Numbers(count_num)=Numbers(1);
                Swype_KoordX(count_num)=koord_x(1);
                Swype_KoordY(count_num)=koord_y(1);
            end
            
            if Swype_Numbers(count_num-1)==3
                Swype_Numbers(count_num)=Numbers(2);
                Swype_KoordX(count_num)=koord_x(2);
                Swype_KoordY(count_num)=koord_y(2);
            end
            
            if Swype_Numbers(count_num-1)==4
                Swype_Numbers(count_num)=0;
                Swype_KoordX(count_num)=0;
                Swype_KoordY(count_num)=0;
            end
            
            if Swype_Numbers(count_num-1)==5
                Swype_Numbers(count_num)=Numbers(4);
                Swype_KoordX(count_num)=koord_x(4);
                Swype_KoordY(count_num)=koord_y(4);
            end
            
            if Swype_Numbers(count_num-1)==6
                Swype_Numbers(count_num)=Numbers(5);
                Swype_KoordX(count_num)=koord_x(5);
                Swype_KoordY(count_num)=koord_y(5);
            end
            
            if Swype_Numbers(count_num-1)==7
                Swype_Numbers(count_num)=0;
                Swype_KoordX(count_num)=0;
                Swype_KoordY(count_num)=0;
            end
            
            if Swype_Numbers(count_num-1)==8
                Swype_Numbers(count_num)=Numbers(7);
                Swype_KoordX(count_num)=koord_x(7);
                Swype_KoordY(count_num)=koord_y(7);
            end
            
            if Swype_Numbers(count_num-1)==9
                Swype_Numbers(count_num)=Numbers(8);
                Swype_KoordX(count_num)=koord_x(8);
                Swype_KoordY(count_num)=koord_y(8);
            end
        end
        %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        if Direction_S(count_direction)==8
            
            if Swype_Numbers(count_num-1)==1
                Swype_Numbers(count_num)=0;
                Swype_KoordX(count_num)=0;
                Swype_KoordY(count_num)=0;
            end
            
            if Swype_Numbers(count_num-1)==2
                Swype_Numbers(count_num)=Numbers(4);
                Swype_KoordX(count_num)=koord_x(4);
                Swype_KoordY(count_num)=koord_y(4);
            end
            
            if Swype_Numbers(count_num-1)==3
                Swype_Numbers(count_num)=Numbers(5);
                Swype_KoordX(count_num)=koord_x(5);
                Swype_KoordY(count_num)=koord_y(5);
            end
            
            if Swype_Numbers(count_num-1)==4
                Swype_Numbers(count_num)=0;
                Swype_KoordX(count_num)=0;
                Swype_KoordY(count_num)=0;
            end
            
            if Swype_Numbers(count_num-1)==5
                Swype_Numbers(count_num)=Numbers(7);
                Swype_KoordX(count_num)=koord_x(7);
                Swype_KoordY(count_num)=koord_y(7);
            end
            
            if Swype_Numbers(count_num-1)==6
                Swype_Numbers(count_num)=Numbers(8);
                Swype_KoordX(count_num)=koord_x(8);
                Swype_KoordY(count_num)=koord_y(8);
            end
            
            if Swype_Numbers(count_num-1)==7
                Swype_Numbers(count_num)=0;
                Swype_KoordX(count_num)=0;
                Swype_KoordY(count_num)=0;
            end
            
            if Swype_Numbers(count_num-1)==8
                Swype_Numbers(count_num)=0;
                Swype_KoordX(count_num)=0;
                Swype_KoordY(count_num)=0;
            end
            
            if Swype_Numbers(count_num-1)==9
                Swype_Numbers(count_num)=0;
                Swype_KoordX(count_num)=0;
                Swype_KoordY(count_num)=0;
            end
        end
        %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
end