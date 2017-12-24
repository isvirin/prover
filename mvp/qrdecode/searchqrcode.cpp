// Blashyrkh.maniac.coding
// BTC:1Maniaccv5vSQVuwrmRtfazhf2WsUJ1KyD DOGE:DManiac9Gk31A4vLw9fLN9jVDFAQZc2zPj

#include <getopt.h>
#include <stdlib.h>
#include <string>
#include <cstdio>
#include <cstring>
#include <iostream>
#include <algorithm>
#include <map>
#include <vector>
#include <zbar.h>
#include <png.h>
#include "VideoFileReader.h"
#include "ImageProcessor.h"


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

std::vector<uint8_t> decodeNumber(const std::string &s)
{
    std::vector<uint8_t> res;
    res.reserve(46);
    for(auto c: s)
    {
        if(c<'0' || c>'9')
            return {};

        unsigned int carry=c-'0';
        for(uint8_t &byte: res)
        {
            unsigned int tmp=byte*10+carry;
            byte=tmp%256;
            carry=tmp/256;
        }
        if(carry)
            res.push_back(carry);
    }
    res.resize(46, 0);

    std::reverse(res.begin(), res.end());

    return res;
}

int main(int argc, char *argv[])
{
    int savePng=0;
    static const struct option long_options[]=
    {
        {"width",     required_argument, 0,        'w'},
        {"height",    required_argument, 0,        'h'},
        {"png",       no_argument,       &savePng,  1 },
        {"verbose",   no_argument,       0,        'v'},
        {0,           0,                 0,         0 }
    };

    int opt;
    int option_index;

    int scaleWidth=320;
    int scaleHeight=240;
    int verbosity=0;

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
            ++verbosity;
            break;
        case '?':
            break;
        }
    }

    if(optind>=argc)
        return 1;

    std::string filename=argv[optind];

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

    zbar::ImageScanner imgscanner;
    imgscanner.set_config(zbar::ZBAR_NONE, zbar::ZBAR_CFG_ENABLE, 0);
    imgscanner.set_config(zbar::ZBAR_QRCODE, zbar::ZBAR_CFG_ENABLE, 1);
    imgscanner.set_config(zbar::ZBAR_QRCODE, zbar::ZBAR_CFG_MIN_LEN, 1);

    std::map<std::vector<uint8_t>, unsigned int> codes;

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

            if(savePng)
            {
                char filename[20];
                snprintf(filename, 20, "%04d.%03d.png", (int)(timestamp/1000), (int)(timestamp%1000));
                debug_save_image_to_png(frame->data[0], frame->width, frame->height, filename);
            }

            zbar::Image zimg(frame->width, frame->height, "GREY", frame->data[0], frame->width*frame->height);

            int res=imgscanner.scan(zimg);

            zbar::SymbolSet ss=imgscanner.get_results();
            for(zbar::SymbolIterator it=ss.symbol_begin(); it!=ss.symbol_end(); ++it)
            {
                if(it->get_type()==zbar::ZBAR_QRCODE)
                {
                    if(verbosity>0)
                    {
                        fprintf(stderr, "Code detected at frame %lu (%04d.%03d): %s\n", frameno, (int)(timestamp/1000), (int)(timestamp%1000), it->get_data().c_str());
                    }
                    std::vector<uint8_t> data=decodeNumber(it->get_data());
                    if(data.size()==46)
                        codes[data]++;
                }
            }

            ++frameno;
        }
        if(rc==AVERROR_EOF)
            break;

        av_packet_unref(packet);
    }
    av_packet_free(&packet);

    unsigned int mostFrequentCount=0;
    unsigned int totalCount=0;
    std::vector<uint8_t> mostFrequentCode;

    for(auto it=codes.begin(); it!=codes.end(); ++it)
    {
        if(it->second>mostFrequentCount)
        {
            mostFrequentCount=it->second;
            mostFrequentCode=it->first;
        }
        totalCount+=it->second;
    }

    if(totalCount>0)
    {
        assert(mostFrequentCode.size()==46);

        std::printf("{\"txhash\":\"0x");
        for(unsigned int i=0; i<32; ++i)
            std::printf("%02x", (unsigned int)(uint8_t)mostFrequentCode[i]);
        std::printf("\", \"blockhash\":\"0x");
        for(unsigned int i=0; i<14; ++i)
            std::printf("%02x", (unsigned int)(uint8_t)mostFrequentCode[32+i]);
        std::printf("\", \"weight\":%.3f}\n", (double)mostFrequentCount/(double)totalCount);
    }
    else
    {
        std::printf("{}\n");
    }

    return 0;
}
