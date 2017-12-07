precision highp float;

uniform sampler2D s_texture;
uniform float inStep; // = 1.0 / width
varying vec2 v_tex;
varying vec2 v_coordNum;

const float one = 1.0;
const float two = 2.0;

void main()
{
    float oddXneg = - mod(v_coordNum.x, two) ;
    float shift1 = oddXneg * inStep;
    float shift2 = (one + oddXneg) * inStep;
    vec4 x0 = texture2D(s_texture, vec2(v_tex.x + shift1, v_tex.y));
    vec4 x1 = texture2D(s_texture, vec2(v_tex.x + shift2, v_tex.y));
    gl_FragColor.r = x0.r + x1.r * (one - two * oddXneg);
}