#extension GL_OES_EGL_image_external : require

precision mediump float;
uniform samplerExternalOES camTexture;

varying vec2 v_CamTexCoordinate;
varying vec2 v_TexCoordinate;
uniform vec2 hStepVec;

void main ()
{
    /*gl_FragColor.r = v_CamTexCoordinate.x;
    gl_FragColor.g = v_CamTexCoordinate.y;
    gl_FragColor.b = hStepVec.x;
    gl_FragColor.a = hStepVec.y;*/

    vec2 coord = v_CamTexCoordinate;
    vec4 cameraColor = texture2D(camTexture, coord);
    gl_FragColor.r = 0.299*cameraColor.r + 0.587*cameraColor.g + 0.114*cameraColor.b;

    coord += hStepVec;
    cameraColor = texture2D(camTexture, coord);
    gl_FragColor.g = 0.299*cameraColor.r + 0.587*cameraColor.g + 0.114*cameraColor.b;

    coord += hStepVec;
    cameraColor = texture2D(camTexture, coord);
    gl_FragColor.b = 0.299*cameraColor.r + 0.587*cameraColor.g + 0.114*cameraColor.b;

    coord += hStepVec;
    cameraColor = texture2D(camTexture, coord);
    gl_FragColor.a = 0.299*cameraColor.r + 0.587*cameraColor.g + 0.114*cameraColor.b;//*/
}

