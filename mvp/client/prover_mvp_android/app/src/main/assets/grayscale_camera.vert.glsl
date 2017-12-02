//position
attribute vec4 position;

//camera transform and texture
uniform mat4 camTextureTransform;
attribute vec4 camTexCoordinate;

//tex coords
varying vec2 v_CamTexCoordinate;
varying vec2 v_TexCoordinate;

void main()
{
    //camera texcoord needs to be manipulated by the transform given back from the system
    v_CamTexCoordinate = (camTextureTransform * camTexCoordinate).xy;
    gl_Position = position;
    v_TexCoordinate = camTexCoordinate.xy;
}