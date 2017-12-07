
precision highp float;

attribute vec4 texCoordinate;
attribute vec4 position;

varying vec2 v_tex;
varying vec2 v_coordNum;
uniform float width;
uniform float height;

void main()
{
    gl_Position = position;
    v_tex = texCoordinate.xy;
    v_coordNum = vec2(texCoordinate.x * width - 0.5, texCoordinate.y * height - 0.5);
}
