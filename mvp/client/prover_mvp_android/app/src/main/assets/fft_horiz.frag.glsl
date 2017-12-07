precision highp float;

uniform sampler2D s_texture;
uniform sampler2D s_turnCoeff_texture;
uniform float texStep; // = intStep / width
uniform float intStep; // step size 2^stepNum; 1 for first run, 2  for second, 4 for third etc
uniform float turnCoeffRow;
uniform float width;
uniform float argMul;
varying vec2 v_tex;
varying vec2 v_coordNum;

void main()
{
    float oddXneg = - mod(floor(v_coordNum.x / intStep), 2.0) ;
    float shift1 = oddXneg * texStep;
    float shift2 = (1.0 + oddXneg) * texStep;
    vec2 x0 = texture2D(s_texture, vec2(v_tex.x + shift1, v_tex.y)).xy;
    vec2 x1 = texture2D(s_texture, vec2(v_tex.x + shift2, v_tex.y)).xy;

    float arg = argMul * v_coordNum.x;
    vec2 turnCoeff = vec2(cos(arg), sin(arg));
    //vec2 turnCoeff = texture2D(s_turnCoeff_texture, vec2(v_tex.x, turnCoeffRow)).xy;
    float v2_re = x1.x * turnCoeff.x - x1.y * turnCoeff.y;
    float v2_im = x1.x * turnCoeff.y + x1.y * turnCoeff.x;

/*    gl_FragColor.r = argMul;
    gl_FragColor.g = arg;
    gl_FragColor.b = turnCoeff.x;
    gl_FragColor.a = turnCoeff.y;*/

    gl_FragColor.r = x0.x + v2_re;
    gl_FragColor.g = x0.y + v2_im;

    /*gl_FragColor.r = v_tex.x * width;
    gl_FragColor.g = (v_tex.x + shift1) * width - 0.5;
    gl_FragColor.b = (v_tex.x + shift2) * width - 0.5;


    /*
    gl_FragColor.r = x1.x;
    gl_FragColor.g = x1.y;
    gl_FragColor.b = v2_re;
    gl_FragColor.a = v2_im;
    */
}