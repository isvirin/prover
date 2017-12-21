#include <cstdio>
#include <string>
#include <cmath>
#include <getopt.h>
#include <png.h>

extern "C"
{
#include <libavformat/avformat.h>
}

#include "VideoFileReader.h"
#include "ImageProcessor.h"
#include "swype_detect.h"
#include "sha256.h"
#include "common.h"


bool debug_save_image_to_png(const unsigned char *data, unsigned int width, unsigned int height, const std::string &filename)
{
    png_bytep rows[height];
    for(unsigned int i=0; i<height; ++i)
        rows[i]=(png_bytep)(data+i*width);

    png_structp png=png_create_write_struct(
        PNG_LIBPNG_VER_STRING,
        NULL,
        NULL,
        NULL);
    if(!png)
        return false;

    png_infop info=png_create_info_struct(png);
    if(!info)
    {
        png_destroy_write_struct(&png, (png_infopp)NULL);
        return false;
    }

    if(setjmp(png_jmpbuf(png)))
    {
        png_destroy_write_struct(&png, &info);
        return false;
    }

    FILE *f=fopen(filename.c_str(), "wb");
    png_init_io(png, f);

    png_set_IHDR(
        png,
        info,
        width,
        height,
        8,
        PNG_COLOR_TYPE_GRAY,
        PNG_INTERLACE_NONE,
        PNG_COMPRESSION_TYPE_DEFAULT,
        PNG_FILTER_TYPE_DEFAULT);

    png_set_rows(png, info, rows);

    png_write_png(png, info, 0, NULL);

    png_destroy_write_struct(&png, &info);
    fclose(f);

    return true;
}

std::string transformSwypeCode(const std::string &swype, const char *transformation)
{
    std::string res;
    for(auto c: swype)
    {
        if(c>='1' && c<='9')
            c=transformation[c-'1'];
        res.push_back(c);
    }
    return res;
}

std::string rotateSwypeCode(const std::string &swype, int rotation)
{
    //   0:  123456789
    // -90:  741852963
    //  90:  369258147
    // 180:  987654321

    if(rotation==0)
        return swype;
    else if(rotation==-90 || rotation==270)
        return transformSwypeCode(swype, "741852963");
    else if(rotation==90 || rotation==-270)
        return transformSwypeCode(swype, "369258147");
    else if(rotation==180 || rotation==-180)
        return transformSwypeCode(swype, "987654321");
    else
        abort();
}

bool parseHash(std::string hashstr, uint8_t *outbuf)
{
    if(hashstr.compare(0, 2, "0x")==0)
        hashstr=hashstr.substr(2);

    if(hashstr.size()!=64)
        return false;

    for(const char *p=hashstr.c_str(); *p; p+=2)
    {
        char twochar[3]={*p, *(p+1), '\0'};
        char *endp;
        *outbuf++=strtoul(twochar, &endp, 16);
        if(endp!=twochar+2)
            return false;
    }

    return true;
}

std::string calculateSwypeCode(const std::string &txhash, const std::string &blockhash, unsigned int modulo)
{
    uint8_t bytes[32+32];
    if(!parseHash(txhash, bytes+32) || !parseHash(blockhash, bytes+0))
    {
        return std::string();
    }

    uint8_t hash[32];
    sha256_simple(hash, bytes, 64);

    unsigned int swypeid=0;
    for(unsigned int i=0; i<32; ++i)
    {
        swypeid=(swypeid*256+hash[i])%modulo;
    }

    std::string swypecode="5";
    unsigned int m=1;
    while(m<modulo)
    {
        static const int neighbours[9][9]=
        {
            {3, 2, 4, 5},
            {5, 1, 3, 4, 5, 6},
            {3, 2, 5, 6},
            {5, 1, 2, 5, 7, 8},
            {8, 1, 2, 3, 4, 6, 7, 8, 9},
            {5, 2, 3, 5, 8, 9},
            {3, 4, 5, 8},
            {5, 4, 5, 6, 7, 9},
            {3, 5, 6, 8}
        };

        int curpt=swypecode.back()-'0';
        int nc=neighbours[curpt-1][0];
        int nextpt=neighbours[curpt-1][1+swypeid*nc/modulo];

        m*=nc;
        swypeid=(swypeid*nc)%modulo;

        swypecode.push_back('0'+nextpt);
    }

    return swypecode;
}

int main(int argc, char *argv[])
{
    static const struct option long_options[]=
    {
        {"width",     required_argument, 0, 'w'},
        {"height",    required_argument, 0, 'h'},
        {"swype",     required_argument, 0,  0 }, // #2
        {"txhash",    required_argument, 0,  0 }, // #3
        {"blockhash", required_argument, 0,  0 }, // #4
        {"png",       no_argument,       0,  0 }, // #5
        {"verbose",   no_argument,       0, 'v'},
        {0,           0,                 0,  0 }
    };

    int opt;
    int option_index;

    int scaleWidth=320;
    int scaleHeight=240;

    std::string swypecode;
    std::string txhash;
    std::string blockhash;
    bool savePng=false;
    ::logLevel=0;

    while((opt=getopt_long(argc, argv, "w:h:v", long_options, &option_index))!=-1)
    {
        switch(opt)
        {
        case 'w':
            scaleWidth=atoi(optarg);
            break;
        case 'h':
            scaleHeight=atoi(optarg);
            break;
        case 'v':
            ++::logLevel;
            break;
        case 0:
            switch(option_index)
            {
            case 2:
                swypecode=optarg;
                break;
            case 3:
                txhash=optarg;
                break;
            case 4:
                blockhash=optarg;
                break;
            case 5:
                savePng=true;
                break;
            }
            break;
        case '?':
            break;
        }
    }

    if(swypecode.empty() && (txhash.empty() || blockhash.empty()))
        return 1;

    if(optind>=argc)
        return 1;

    std::string filename=argv[optind];

    if(swypecode.empty())
    {
        swypecode=calculateSwypeCode(txhash, blockhash, 27000);
    }

//    av_log_set_level(48);
    av_register_all();
    avcodec_register_all();
    avfilter_register_all();

    std::cerr<<"Opening file "<<filename<<"\n";
    VideoFileReader reader(filename.c_str(), true);
    if(!reader.isValid())
        return 2;

    unsigned int sourceWidth=reader.getCodecParameters()->width;
    unsigned int sourceHeight=reader.getCodecParameters()->height;
    int pixelFormat=reader.getCodecParameters()->format;
    const AVRational *timeBase=reader.getTimeBase();
    unsigned int targetWidth=(sourceWidth>sourceHeight)?scaleWidth:scaleHeight;
    unsigned int targetHeight=scaleWidth*scaleHeight/targetWidth;

    ImageProcessor processor(
        sourceWidth,
        sourceHeight,
        pixelFormat,
        timeBase,
        targetWidth,
        targetHeight);

    AVCodec *codec=avcodec_find_decoder(reader.getCodecParameters()->codec_id);
    if(!codec)
    {
        fprintf(stderr, "no codec found\n");
        return 2;
    }

    AVCodecContext *codecctx=avcodec_alloc_context3(codec);
    if(!codecctx)
    {
        fprintf(stderr, "avcodec_alloc_context3 failed\n");
        return 2;
    }

    avcodec_open2(codecctx, codec, NULL);

    int rc;

    rc=avcodec_parameters_to_context(codecctx, reader.getCodecParameters());
    if(rc<0)
    {
        fprintf(stderr, "avcodec_parameters_to_context failed %d\n", rc);
        return 2;
    }

    fprintf(stderr, "Specified swype-code: %s\n", swypecode.c_str());
    swypecode=rotateSwypeCode(swypecode, (int)floor(reader.getOrientationAngle()));
    fprintf(stderr, "Transformed swype-code: %s\n", swypecode.c_str());

    SwypeDetect detector;
    detector.init(
        (double)sourceWidth/(double)sourceHeight,
        targetWidth,
        targetHeight);
    detector.setSwype(swypecode);

    int64_t swypeBeginTimestamp=-1;
    int64_t swypeEndTimestamp=-1;

    AVFrame *frame=av_frame_alloc();

    AVPacket *packet=av_packet_alloc();
    while(reader.readPacket(packet))
    {
        avcodec_send_packet(codecctx, packet);

        while((rc=avcodec_receive_frame(codecctx, frame))==0)
        {
            int64_t timestamp=frame->pts*1000*reader.getTimeBase()->num/reader.getTimeBase()->den;

            processor.processImage(frame);

            if(savePng)
            {
                char filename[20];
                snprintf(filename, 20, "%04d.%03d.png", timestamp/1000, timestamp%1000);
                debug_save_image_to_png(frame->data[0], frame->width, frame->height, filename);
            }

            int state=-1, index=-1, x=-1, y=-1;
            int debug=-1;

            detector.processFrame_new(frame->data[0], frame->width, frame->height, timestamp, state, index, x, y, debug);

            if(state==3 && swypeBeginTimestamp==-1)
                swypeBeginTimestamp=timestamp;
            else if(state==4 && swypeBeginTimestamp!=-1 && swypeEndTimestamp==-1)
                swypeEndTimestamp=timestamp;
            else if(state!=3 && state!=4)
                swypeBeginTimestamp=swypeEndTimestamp=-1;

//            fprintf(stderr, "TS=%lld S=%d index=%d x=%d y=%d debug=%d %lld %lld\n", (long long)timestamp, state, index, x, y, debug, (long long)swypeBeginTimestamp, (long long)swypeEndTimestamp);

            if(swypeBeginTimestamp!=-1 && swypeEndTimestamp!=-1)
                break;
        }
        if(rc==AVERROR_EOF)
            break;

        av_packet_unref(packet);
    }
    av_packet_free(&packet);

    if(swypeBeginTimestamp!=-1 && swypeEndTimestamp!=-1)
    {
        printf("{\"result\":{\"time-begin\":%.3f, \"time-end\":%.3f}}\n", swypeBeginTimestamp/1000.0, swypeEndTimestamp/1000.0);
    }
    else
    {
        printf("{\"result\":null}\n");
    }

    return 0;
}
