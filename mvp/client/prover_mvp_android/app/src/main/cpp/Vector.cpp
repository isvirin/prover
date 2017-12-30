//
// Created by babay on 08.12.2017.
//


#include "Vector.h"

Vector Vector::EllipticalShift(double rx, double ry, double targetX, double targetY) {
    if (rx == 0) {
        if (targetY > _y + ry) {
            return Vector(_x, _y + ry);
        } else if (targetY < _y - ry) {
            return Vector(_x, _y - ry);
        }
        return Vector(_x, targetY);
    } else if (ry == 0) {
        if (targetX > _y + rx) {
            return Vector(_x + rx, _y);
        } else if (targetX < _x - rx) {
            return Vector(_x - rx, _y);
        }
        return Vector(targetX, _y);
    }

    // move to coordinates where our ellipse is radius-1 circle
    Vector res = Vector((targetX - _x) / rx, (targetY - _y) / ry);
    double len = res.Length();
    //normalize -- point is on the circle
    res._x /= len;
    res._y /= len;
    //restore coordinates
    res._x = res._x * rx + _x;
    res._y = res._y * ry + _y;
    return res;
}

Vector Vector::EllipticalShiftMagnet(float rx, float ry, float targetX, float targetY) {
    if (rx == 0) {
        if (targetY > _y + ry) {
            return Vector(_x, _y + ry);
        } else if (targetY < _y - ry) {
            return Vector(_x, _y - ry);
        }
        return Vector(_x, targetY);
    } else if (ry == 0) {
        if (targetX > _y + rx) {
            return Vector(_x + rx, _y);
        } else if (targetX < _x - rx) {
            return Vector(_x - rx, _y);
        }
        return Vector(targetX, _y);
    }

    // move to coordinates where our ellipse is radius-1 circle
    Vector res = Vector((targetX - _x) / rx, (targetY - _y) / ry);
    double len = res.Length();
    if (len <= 1)
        return Vector(targetX, targetY);
    //normalize -- point is on the circle
    res._x /= len;
    res._y /= len;
    //restore coordinates
    res._x = res._x * rx + _x;
    res._y = res._y * ry + _y;
    return res;
}

Vector Vector::ShiftEllipseToTouchLineMagnet(float a, float b) {
    if (a == 0) {
        Vector result(_x, _y + b);
        if (result._y > _x)
            result._y = _x;
        return result;
    } else if (b == 0) {
        Vector result(_x - a, _y);
        if (result._x < _y)
            result._x = _y;
        return result;
    }

    float a2 = a * a;
    float x = -a2 / sqrtf(b * b + a2);
    float y = b / a * sqrtf(a2 - x * x) + (float) _y;
    x += (float) _x;
    if (x <= y) // (x,y) is on the other side of y=x line
        return Vector(0, 0);

    return Vector(x, y);
}
