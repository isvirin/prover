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


int main(int argc, char *argv[])
{
    if(argc<=2)
        return 1;

//    av_log_set_level(48);
    av_register_all();
    avcodec_register_all();
    avfilter_register_all();

    VideoFileReader reader(argv[1], true);
    if(!reader.isValid())
        return 2;

    unsigned int sourceWidth=reader.getCodecParameters()->width;
    unsigned int sourceHeight=reader.getCodecParameters()->height;
    int pixelFormat=reader.getCodecParameters()->format;
    const AVRational *timeBase=reader.getTimeBase();
    unsigned int targetWidth=(sourceWidth>sourceHeight)?320:240;
    unsigned int targetHeight=320*240/targetWidth;

    ImageProcessor processor(
        sourceWidth,
        sourceHeight,
        pixelFormat,
        timeBase,
        targetWidth,
        targetHeight,
        (int)floor(-reader.getOrientationAngle()));

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

    int fps=30;//formatctx->streams[videoStreamIndex]->avg_frame_rate.num/formatctx->streams[videoStreamIndex]->avg_frame_rate.den;

    std::string swype=argv[2];
    SwypeDetect detector;
    detector.init(fps, swype);

    AVFrame *frame=av_frame_alloc();

    AVPacket packet;
    av_init_packet(&packet);
    while(reader.readPacket(&packet))
    {
        avcodec_send_packet(codecctx, &packet);

        while((rc=avcodec_receive_frame(codecctx, frame))==0)
        {
            int64_t timestamp=frame->pts*1000*reader.getTimeBase()->num/reader.getTimeBase()->den;

            processor.processImage(frame);
            char filename[20];
            snprintf(filename, 20, "%04d.%03d.png", timestamp/1000, timestamp%1000);
            debug_save_image_to_png(frame->data[0], frame->width, frame->height, filename);
        
            int state=-1, index=-1, x=-1, y=-1;
            int debug=-1;


            detector.processFrame_new(frame->data[0], frame->width, frame->height, timestamp, state, index, x, y, debug);
            fprintf(stderr, "TS=%lld S=%d index=%d x=%d y=%d debug=%d\n", (long long)timestamp, state, index, x, y, debug);

        }
        if(rc==AVERROR_EOF)
            break;

        av_packet_unref(&packet);
    }

    return 0;
}
