precision mediump float;

uniform sampler2D s_texture;
varying vec2 v_tex;

void main()
{
    vec4 color = texture2D(s_texture, v_tex);
    gl_FragColor.r = color.a;
    gl_FragColor.g = color.b;
    gl_FragColor.b = color.g;
    gl_FragColor.a = color.r;
}