
precision highp float;

attribute vec4 texCoordinate;
attribute vec4 position;

varying vec2 v_tex;

void main()
{
    gl_Position = position;
    v_tex = texCoordinate.xy;
}
