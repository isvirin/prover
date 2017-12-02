#extension GL_OES_EGL_image_external : require

precision mediump float;
uniform samplerExternalOES camTexture;
uniform int revOrderTable[256];

varying vec2 v_CamTexCoordinate;
varying vec2 v_TexCoordinate;

void main ()
{
    vec4 cameraColor = texture2D(camTexture, v_CamTexCoordinate);
    float luminance = 0.299*cameraColor.r + 0.587*cameraColor.g + 0.114*cameraColor.b;

    gl_FragColor.r = luminance;
    //gl_FragColor.g = luminance;
    //gl_FragColor.b = luminance;
    //gl_FragColor.a = luminance;
}

