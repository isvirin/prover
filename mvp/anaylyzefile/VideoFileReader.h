// Blashyrkh.maniac.coding
// BTC:1Maniaccv5vSQVuwrmRtfazhf2WsUJ1KyD DOGE:DManiac9Gk31A4vLw9fLN9jVDFAQZc2zPj

#ifndef _VideoFileReader_h
#define _VideoFileReader_h

extern "C"
{
#include <libavformat/avformat.h>
}


class VideoFileReader
{
public:
    VideoFileReader(const char *filename, bool h264toannexb);
    ~VideoFileReader();

    bool isValid() const; // File opened
    bool isEof() const;   // Valid, EOF reached, no errors
    bool isError() const; // Valid, error happened during packet reading
    const AVCodecParameters *getCodecParameters() const;
    const AVRational *getTimeBase() const;

    // If the function returns true, packet should be av_packet_unref'ed after
    // use.
    bool readPacket(AVPacket *packet);

private:
    AVFormatContext *_formatctx;
    int              _videoStreamIndex;
    AVBSFContext    *_bsfctx;
    bool             _eof;
    bool             _error;
    bool             _packetSentToBSF;
};

#endif
