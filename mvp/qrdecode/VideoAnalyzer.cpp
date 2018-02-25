// Blashyrkh.maniac.coding
// BTC:1Maniaccv5vSQVuwrmRtfazhf2WsUJ1KyD DOGE:DManiac9Gk31A4vLw9fLN9jVDFAQZc2zPj

#include "VideoAnalyzer.h"
#include <iostream>
#include <cstring>
#include "VideoFileReader.h"
#include "ImageProcessor.h"


VideoAnalyzer::VideoAnalyzer(
    const std::string &filename,
    const Config      &config) :

    _filename(filename),
    _config(config)
{
}

Analyzer::Result VideoAnalyzer::analyzeFile()
{
    Result result;

//    av_log_set_level(48);
    av_register_all();
    avcodec_register_all();
    avfilter_register_all();

    std::cerr<<"Opening file "<<_filename<<"\n";
    VideoFileReader reader(_filename.c_str(), true);
    if(!reader.isValid())
        return result.setError();

    unsigned int sourceWidth=reader.getCodecParameters()->width;
    unsigned int sourceHeight=reader.getCodecParameters()->height;
    int pixelFormat=reader.getCodecParameters()->format;
    const AVRational *timeBase=reader.getTimeBase();
    unsigned int targetWidth=(sourceWidth>sourceHeight)?_config.getScaleWidth():_config.getScaleHeight();
    unsigned int targetHeight=_config.getScaleWidth()*_config.getScaleHeight()/targetWidth;

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
        return result.setError();
    }

    AVCodecContext *codecctx=avcodec_alloc_context3(codec);
    if(!codecctx)
    {
        fprintf(stderr, "avcodec_alloc_context3 failed\n");
        return result.setError();
    }

    avcodec_open2(codecctx, codec, NULL);

    int rc;

    rc=avcodec_parameters_to_context(codecctx, reader.getCodecParameters());
    if(rc<0)
    {
        fprintf(stderr, "avcodec_parameters_to_context failed %d\n", rc);
        return result.setError();
    }

    AVFrame *frame=av_frame_alloc();

    AVPacket *packet=av_packet_alloc();
    unsigned long frameno=0;
    while(reader.readPacket(packet))
    {
        avcodec_send_packet(codecctx, packet);

        while((rc=avcodec_receive_frame(codecctx, frame))==0)
        {
            int64_t timestamp=frame->pts*1000*reader.getTimeBase()->num/reader.getTimeBase()->den;

            processor.processImage(frame);

            if(frame->width!=frame->linesize[0])
            {
                for(int y=1; y<frame->height; ++y)
                {
                    std::memmove(frame->data[0]+y*frame->width, frame->data[0]+y*frame->linesize[0], frame->width);
                }
            }

            if(_config.getSaveToPngFlag())
            {
                char filename[20];
                snprintf(filename, 20, "%04d.%03d.png", (int)(timestamp/1000), (int)(timestamp%1000));
                debug_save_image_to_png(frame->data[0], frame->width, frame->height, filename);
            }

            analyzeGrayscaleImage(
                frame->width,
                frame->height,
                (const char *)frame->data[0],
                result);

            ++frameno;
        }
        if(rc==AVERROR_EOF)
            break;

        av_packet_unref(packet);
    }
    av_packet_free(&packet);

    return result;
}
