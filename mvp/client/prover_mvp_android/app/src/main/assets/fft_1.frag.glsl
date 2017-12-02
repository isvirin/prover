precision highp float;

uniform sampler2D s_texture;
varying vec2 v_tex;
const float pxStep = .5 / 1024.0;

void main()
{
    vec4 cameraColor = texture2D(s_texture, v_tex);
    gl_FragColor.r = cameraColor2.r;
}