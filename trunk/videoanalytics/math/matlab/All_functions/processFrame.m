function [S, call] = processFrame(frame1,frame2, width, height, fps, swype, S, call, count,Swype_KoordX, Swype_KoordY, Swype_Numbers, deltaXX, deltaYY, count_num, count_direction,count_sum)
call=call+1;
usfac = 100;
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% обнаружение кругового движения
if S==0
    buf1ft=dfft(frame1);
    buf2ft=dfft(frame2);
    [output] = Phase_Cor(fft2(buf1ft),fft2(buf2ft),usfac);
    [deltaX, deltaY, deltaXX, deltaYY, K, Mean_Alfa, Direction]=Delta_calculation(output,call, deltaXX, deltaYY);
    
    if ((abs(deltaX))>3)||((abs(deltaY))>3)
        [flag_R, S] = CircleDetection(deltaXX, deltaYY, buf1ft, S);
    end
end
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% проверка ввода swype
if S==1
    if swype==0
        %%%%%%%%%  ввод свайп кода
        [koord_x, koord_y, Numbers] = Koord_Swipe_points(vidHeight,vidWidth);
        
        
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
        
        
        if lenght(Swype_Numbers)==9
            S=3;
        end
        
    else
        %%%%%%%%%  ввод swype кода
        S=2;
        swype_Numbers=importdata('swype_Numbers.txt');
        %%%%%%%%%  координаты для визуализации
        
        
         *  @param x, y [out] только для state==2, координаты траектории для визуализации 
        начинаем распознавать траекторию, при этом с каждым загруженным кадром возвращаем S2 (i) и координаты точки траектории для
        визуализации;
        
        
        если траектория в ходе ввода swype "заехала" в зону другой цифры, не той, которую мы ожидаем, то необходимо вернуть алгоритм в
        состояние S0, при этом если swype-код не был задан при Инициализации алгоритма, его также надо сбросить;
        
        
        если swype-код не введен полностью в течение времени, равному 2*N секунд, где N - количество знаков в swype-коде, то перейти в
        состояние S0, при этом если swype-код не был задан при Инициализации алгоритма, его также надо сбросить. Важно! Время следует
        рассчитывать только исходя из заданного при инициализации fps. а следовательно, известного времени между соседними кадрами;
        если распознавание swype-кода успешно завершено, перейти в состояние S3.
        
    end
end




%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

end