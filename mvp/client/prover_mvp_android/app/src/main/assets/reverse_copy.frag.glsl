precision highp float;

uniform sampler2D s_texture;
varying vec2 v_tex;
uniform int revOrderTable[256];
const float pxStep = .5 / 1024.0;

highp float reverse(float coord){
    int cx = int(coord * 1024.0);
    int hi = cx / 256;
    int lo = cx - (256 * hi);

    int loRev = revOrderTable[lo];
    int hiRev = revOrderTable[hi] / 64;
    int res = loRev * 4 + hiRev;
    return float(res) / 1024.0;
}

int reverseInt(float coord){
    int cx = int(coord * 1024.0);
    int hi = cx / 256;
    int lo = cx - (256 * hi);

    int loRev = revOrderTable[lo];
    int hiRev = revOrderTable[hi] / 64;
    int res = loRev * 4 + hiRev;
    return res;
}

vec2 reverseVec2(float coord){
    int cx = int(coord * 1024.0);
    int hi = cx / 256;
    int lo = cx - (256 * hi);

    int loRev = revOrderTable[lo];
    int hiRev = revOrderTable[hi];
    return vec2(float(hiRev / 64), float(loRev));
}

void main()
{
    float cxX = reverse(v_tex.x);
    float cxY = reverse(v_tex.y);
    vec2 tex2 = vec2(cxX + pxStep, cxY + pxStep);

    vec4 cameraColor2 = texture2D(s_texture, tex2);
    gl_FragColor.r = cameraColor2.r;
}