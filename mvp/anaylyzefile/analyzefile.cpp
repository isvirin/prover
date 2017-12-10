#include <cstdio>
#include <string>
#include <getopt.h>
#include <png.h>

extern "C"
{
#include <libavformat/avformat.h>
}

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

    av_register_all();
    avcodec_register_all();

    AVFormatContext *formatctx=NULL;

    int rc=avformat_open_input(&formatctx, argv[1], NULL, NULL);
    if(rc<0)
    {
        fprintf(stderr, "avformat_open_input failed\n");
        return 2;
    }

    fprintf(stderr, "calling avformat_find_stream_info\n");
    rc=avformat_find_stream_info(formatctx, NULL);
    if(rc<0)
    {
        fprintf(stderr, "failed rc=%d\n", rc);
        return 2;
    }

    int videoStreamIndex=-1;
    for(unsigned int index=0; index<formatctx->nb_streams; ++index)
    {
        if(formatctx->streams[index]->codecpar->codec_type==AVMEDIA_TYPE_VIDEO)
            videoStreamIndex=index;
    }
    if(videoStreamIndex==-1)
    {
        fprintf(stderr, "No video stream found\n");
        return 2;
    }

    AVCodec *codec=avcodec_find_decoder(formatctx->streams[videoStreamIndex]->codecpar->codec_id);
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

    rc=avcodec_parameters_to_context(codecctx, formatctx->streams[videoStreamIndex]->codecpar);
    if(rc<0)
    {
        fprintf(stderr, "avcodec_parameters_to_context failed %d\n", rc);
        return 2;
    }


    const AVBitStreamFilter *bsf=av_bsf_get_by_name("h264_mp4toannexb");
    if(!bsf)
    {
        fprintf(stderr, "failed to create bitstream filter\n");
        return 2;
    }

    AVBSFContext *bsfctx=NULL;
    if(av_bsf_alloc(bsf, &bsfctx)<0)
    {
        fprintf(stderr, "failed to create bitstream filter context\n");
        return 2;
    }

    avcodec_parameters_copy(bsfctx->par_in, formatctx->streams[videoStreamIndex]->codecpar);
//    bsfctx->time_base_in=formatctx->time_base;

    if(av_bsf_init(bsfctx)<0)
    {
        fprintf(stderr, "failed to initialize bitstream filter\n");
        return 2;
    }

    int fps=formatctx->streams[videoStreamIndex]->avg_frame_rate.num/formatctx->streams[videoStreamIndex]->avg_frame_rate.den;

    std::string swype=argv[2];
    SwypeDetect detector;
    detector.init(fps, swype);

    AVFrame *frame=av_frame_alloc();
    while(true)
    {
        AVPacket packet;
        int rc;

        av_init_packet(&packet);

        if((rc=av_read_frame(formatctx, &packet))!=0)
        {
            fprintf(stderr, "Failed to read packed %d\n", rc);
            break;
        }

        if(packet.stream_index==videoStreamIndex)
        {
            AVPacket *newpacket=av_packet_alloc();

            av_bsf_send_packet(bsfctx, &packet);
            while((rc=av_bsf_receive_packet(bsfctx, newpacket))==0)
            {
                avcodec_send_packet(codecctx, newpacket);

                while((rc=avcodec_receive_frame(codecctx, frame))==0)
                {
                    int state=-1, index=-1, x=-1, y=-1;
                    int debug=-1;

                    int64_t timestamp=frame->pts*1000*formatctx->streams[videoStreamIndex]->time_base.num/formatctx->streams[videoStreamIndex]->time_base.den;

                    detector.processFrame_new(frame->data[0], frame->width, frame->height, timestamp, state, index, x, y, debug);
                    fprintf(stderr, "TS=%lld S=%d index=%d x=%d y=%d debug=%d\n", (long long)timestamp, state, index, x, y, debug);

                    debug_save_image_to_png(frame->data[0], frame->width, frame->height, std::to_string(timestamp)+".png");
                }
                if(rc==AVERROR_EOF)
                    break;

                av_packet_unref(newpacket);
            }

            if(rc!=AVERROR(EAGAIN))
            {
                
            }

            av_packet_free(&newpacket);
        }

        av_packet_unref(&packet);
    }

    return 0;
}
