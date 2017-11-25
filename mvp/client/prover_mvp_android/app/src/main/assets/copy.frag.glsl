precision mediump float;

uniform sampler2D s_texture;
varying vec2 v_tex;

void main()
{
    gl_FragColor = texture2D(s_texture, v_tex);
}