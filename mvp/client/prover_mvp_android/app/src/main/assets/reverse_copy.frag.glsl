precision highp float;

uniform sampler2D s_texture;
//varying vec2 v_tex;
varying vec2 v_coordNum;
uniform int revOrderTable[256];
uniform float pxStepX;
uniform float pxStepY;
uniform float width;
uniform float height;
uniform int usedHiBitPart; // =64 for 1024 size
uniform int unusedHiBitPart; // = 4 for 1024 size; usedHiBitPart * unusedHiBitPart = 256

const int i256 = 256;

highp float reverse(float coord){
    int cx = int(coord);
    int hi = cx / i256;
    int lo = cx - (i256 * hi);

    int loRev = revOrderTable[lo];
    int hiRev = revOrderTable[hi] / unusedHiBitPart;// unusedHiBitPart == 64 for 1024 size
    int res = loRev * usedHiBitPart + hiRev; // usedHiBitPart == 4 for 1024 size
    return float(res);
}

int reverseInt(float coord){
    int cx = int(coord);
    int hi = cx / i256;
    int lo = cx - (i256 * hi);

    int loRev = revOrderTable[lo];
    int hiRev = revOrderTable[hi] / unusedHiBitPart;
    int res = loRev * usedHiBitPart + hiRev;
    return res;
}

vec2 reverseVec2(float coord){
    int cx = int(coord);
    int hi = cx / i256;
    int lo = cx - (i256 * hi);

    int loRev = revOrderTable[lo];
    int hiRev = revOrderTable[hi] / unusedHiBitPart;
    return vec2(float(hiRev), float(loRev));
}

bool isOdd(float val){
    return mod(val, 2.0) > 0.5;
}

void main()
{
    float cxX = reverse(v_coordNum.x) / width;
    float cxY = reverse(v_coordNum.y) / height;
    vec2 tex2 = vec2(cxX + pxStepX, cxY + pxStepY);
    vec4 cameraColor2 = texture2D(s_texture, tex2);
    gl_FragColor.r = cameraColor2.r;
}