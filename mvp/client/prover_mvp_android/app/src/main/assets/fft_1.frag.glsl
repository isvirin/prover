precision highp float;

uniform sampler2D s_texture;
varying vec2 v_tex;
const float pxStep = .5 / 1024.0;

void main()
{
    vec4 x0 = texture2D(s_texture, v_tex);
    vec4 x1 = texture2D(s_texture, vec2(v_tex.x + 0.5, v_tex.y));
    gl_FragColor.r = x0.r + x1.r;
    gl_FragColor.b = x0.r - x1.r;
}