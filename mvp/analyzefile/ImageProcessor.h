// Blashyrkh.maniac.coding
// BTC:1Maniaccv5vSQVuwrmRtfazhf2WsUJ1KyD DOGE:DManiac9Gk31A4vLw9fLN9jVDFAQZc2zPj

#ifndef _ImageProcessor_h
#define _ImageProcessor_h

extern "C"
{
#include <libavformat/avformat.h>
#include <libavfilter/avfilter.h>
}


class ImageProcessor
{
public:
    ImageProcessor(
        unsigned int      sourceWidth,
        unsigned int      sourceHeight,
        int               pixelFormat,
        const AVRational *timeBase,
        unsigned int      targetWidth,
        unsigned int      targetHeight);
    ~ImageProcessor();

    bool isValid() const;

    bool processImage(AVFrame *frame);

private:
    AVFilterGraph   *_graph;
    AVFilterContext *_buffer;
    AVFilterContext *_sink;
};

#endif
