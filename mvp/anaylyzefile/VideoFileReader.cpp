// Blashyrkh.maniac.coding
// BTC:1Maniaccv5vSQVuwrmRtfazhf2WsUJ1KyD DOGE:DManiac9Gk31A4vLw9fLN9jVDFAQZc2zPj

#include "VideoFileReader.h"
#include <stdio.h>

extern "C"
{
#include <libavutil/display.h>
}


VideoFileReader::VideoFileReader(const char *filename, bool h264toannexb) :
    _formatctx(NULL),
    _videoStreamIndex(-1),
    _bsfctx(NULL),
    _eof(false),
    _error(false),
    _packetSentToBSF(false)
{
    AVFormatContext *formatctx=NULL;
    int videoStreamIndex=-1;
    AVBSFContext *bsfctx=NULL;

    int rc=avformat_open_input(&formatctx, filename, NULL, NULL);
    if(rc<0)
    {
        fprintf(stderr, "avformat_open_input failed\n");
        return;
    }

    rc=avformat_find_stream_info(formatctx, NULL);
    if(rc<0)
    {
        fprintf(stderr, "avformat_file_stream_info failed rc=%d\n", rc);
        avformat_close_input(&formatctx);
        return;
    }

    videoStreamIndex=av_find_best_stream(formatctx, AVMEDIA_TYPE_VIDEO, -1, -1, NULL, 0);
    if(videoStreamIndex<0)
    {
        fprintf(stderr, "No video stream found\n");
        avformat_close_input(&formatctx);
        return;
    }

    int32_t *displaymatrix=(int32_t *)av_stream_get_side_data(
        formatctx->streams[videoStreamIndex],
        AV_PKT_DATA_DISPLAYMATRIX,
        NULL);
    printf("%f\n", av_display_rotation_get(displaymatrix));

    if(h264toannexb)
    {
        const AVBitStreamFilter *bsf=av_bsf_get_by_name("h264_mp4toannexb");
        if(!bsf)
        {
            fprintf(stderr, "failed to create bitstream filter\n");
            avformat_close_input(&formatctx);
            return;
        }

        if(av_bsf_alloc(bsf, &bsfctx)<0)
        {
            fprintf(stderr, "failed to create bitstream filter context\n");
            avformat_close_input(&formatctx);
            return;
        }

        avcodec_parameters_copy(bsfctx->par_in, formatctx->streams[videoStreamIndex]->codecpar);

        if(av_bsf_init(bsfctx)<0)
        {
            fprintf(stderr, "failed to initialize bitstream filter\n");
            av_bsf_free(&bsfctx);
            avformat_close_input(&formatctx);
            return;
        }
    }

    _formatctx=formatctx;
    _videoStreamIndex=videoStreamIndex;
    _bsfctx=bsfctx;
}

VideoFileReader::~VideoFileReader()
{
    av_bsf_free(&_bsfctx);
    avformat_close_input(&_formatctx);
}

bool VideoFileReader::isValid() const
{
    return _formatctx!=NULL;
}

bool VideoFileReader::isEof() const
{
    return _eof;
}

bool VideoFileReader::isError() const
{
    return _error;
}

const AVCodecParameters *VideoFileReader::getCodecParameters() const
{
    if(_formatctx && _videoStreamIndex!=-1)
        return _formatctx->streams[_videoStreamIndex]->codecpar;
    else
        return NULL;
}

const AVRational *VideoFileReader::getTimeBase() const
{
    if(_formatctx && _videoStreamIndex!=-1)
        return &_formatctx->streams[_videoStreamIndex]->time_base;
    else
        return NULL;
}

bool VideoFileReader::readPacket(AVPacket *packet)
{
    if(!_formatctx || _eof || _error)
        return false;

    int rc;

    if(!_bsfctx)
    {
        rc=av_read_frame(_formatctx, packet);
        if(rc==0)
            return true;

        _eof=(rc==AVERROR_EOF);
        _error=(rc!=AVERROR_EOF);

        return false;
    }
    else
    {
        while(true)
        {
            if(!_packetSentToBSF)
            {
                AVPacket origPacket;
                av_init_packet(&origPacket);

                while(true)
                {
                    rc=av_read_frame(_formatctx, &origPacket);
                    if(rc<0)
                    {
                        _eof=(rc==AVERROR_EOF);
                        _error=(rc!=AVERROR_EOF);
                        return false;
                    }

                    if(origPacket.stream_index==_videoStreamIndex)
                        break;
                    av_packet_unref(&origPacket);
                }

                av_bsf_send_packet(_bsfctx, &origPacket);
                av_packet_unref(&origPacket);
                _packetSentToBSF=true;
            }

            rc=av_bsf_receive_packet(_bsfctx, packet);
            if(rc==0)
                return true;

            if(rc!=AVERROR(EAGAIN))
            {
                _error=true;
                return false;
            }

            _packetSentToBSF=false;
        }
    }
}
