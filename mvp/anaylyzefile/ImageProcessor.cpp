// Blashyrkh.maniac.coding
// BTC:1Maniaccv5vSQVuwrmRtfazhf2WsUJ1KyD DOGE:DManiac9Gk31A4vLw9fLN9jVDFAQZc2zPj

#include "ImageProcessor.h"
#include <stdio.h>

extern "C"
{

#include <libavfilter/buffersrc.h>
#include <libavfilter/buffersink.h>

}


ImageProcessor::ImageProcessor(
    unsigned int      sourceWidth,
    unsigned int      sourceHeight,
    int               pixelFormat,
    const AVRational *timeBase,
    unsigned int      targetWidth,
    unsigned int      targetHeight,
    int               rotationAngle) :

    _graph(NULL),
    _buffer(NULL),
    _sink(NULL)
{
    AVFilterGraph *graph=avfilter_graph_alloc();

    const AVFilter *buffer=avfilter_get_by_name("buffer");
    const AVFilter *planeExtractor=avfilter_get_by_name("extractplanes");
    const AVFilter *scaler=avfilter_get_by_name("scale");
    const AVFilter *rotator=avfilter_get_by_name("rotate");
    const AVFilter *buffersink=avfilter_get_by_name("buffersink");

    if(!buffer || !planeExtractor || !scaler || !rotator || !buffersink)
        return;

    AVFilterContext *bufferContext=NULL;
    AVFilterContext *planeExtractorContext=NULL;
    AVFilterContext *scalerContext=NULL;
    AVFilterContext *rotatorContext=NULL;
    AVFilterContext *buffersinkContext=NULL;

    char configstr[200];
    snprintf(configstr, 200, "width=%u:height=%u:pix_fmt=%d:time_base=%d/%d", sourceWidth, sourceHeight, pixelFormat, timeBase->num, timeBase->den);
    if(avfilter_graph_create_filter(&bufferContext, buffer, NULL, configstr, NULL, graph)!=0)
    {
        fprintf(stderr, "Failed to initialize \"buffer\" context\n");
        avfilter_graph_free(&graph);
        return;
    }
    if(avfilter_graph_create_filter(&planeExtractorContext, planeExtractor, NULL, "y", NULL, graph)!=0)
    {
        fprintf(stderr, "Failed to initialize \"extractplanes\" context\n");
        avfilter_free(bufferContext);
        avfilter_graph_free(&graph);
        return;
    }
    snprintf(configstr, 200, "w=%u:h=%u", targetWidth, targetHeight);
    if(avfilter_graph_create_filter(&scalerContext, scaler, NULL, configstr, NULL, graph)!=0)
    {
        fprintf(stderr, "Failed to initialize \"scale\" context\n");
        avfilter_free(bufferContext);
        avfilter_free(planeExtractorContext);
        avfilter_graph_free(&graph);
        return;
    }
/*    if(rotationAngle==0)
        snprintf(configstr, 200, "angle=0:bilinear=0:ow=iw:oh:ih");
    else if(rotationAngle==90 || rotationAngle==-270)
        snprintf(configstr, 200, "angle=PI/2:bilinear=0:ow=ih:oh:iw");
    else if(rotationAngle==180 || rotationAngle==-180)
        snprintf(configstr, 200, "angle=PI:bilinear=0:ow=iw:oh:ih");
    else if(rotationAngle==270 || rotationAngle==-90)
        snprintf(configstr, 200, "angle=-PI/2:bilinear=0:ow=ih:oh:iw");*/
    snprintf(configstr, 200, "angle=PI*%d/180:bilinear=0", rotationAngle);
    if(avfilter_graph_create_filter(&rotatorContext, rotator, NULL, configstr, NULL, graph)!=0)
    {
        fprintf(stderr, "Failed to initialize \"buffersink\" context\n");
        avfilter_free(bufferContext);
        avfilter_free(planeExtractorContext);
        avfilter_free(scalerContext);
        avfilter_graph_free(&graph);
    }
    if(avfilter_graph_create_filter(&buffersinkContext, buffersink, NULL, "", NULL, graph)!=0)
    {
        fprintf(stderr, "Failed to initialize \"buffersink\" context\n");
        avfilter_free(bufferContext);
        avfilter_free(planeExtractorContext);
        avfilter_free(scalerContext);
        avfilter_free(rotatorContext);
        avfilter_graph_free(&graph);
        return;
    }

    if(avfilter_link(bufferContext, 0, planeExtractorContext, 0)!=0 ||
       avfilter_link(planeExtractorContext, 0, scalerContext, 0)!=0 ||
       avfilter_link(scalerContext, 0, rotatorContext, 0)!=0 ||
       avfilter_link(rotatorContext, 0, buffersinkContext, 0)!=0 ||
       avfilter_graph_config(graph, NULL)!=0)
    {
        fprintf(stderr, "Failed to link filters\n");
        avfilter_free(bufferContext);
        avfilter_free(planeExtractorContext);
        avfilter_free(scalerContext);
        avfilter_free(rotatorContext);
        avfilter_free(buffersinkContext);
        avfilter_graph_free(&graph);
        return;
    }

    _buffer=bufferContext;
    _sink=buffersinkContext;
}

ImageProcessor::~ImageProcessor()
{
}

bool ImageProcessor::processImage(AVFrame *frame)
{
    int rc;

    if((rc=av_buffersrc_write_frame(_buffer, frame))!=0)
    {
        fprintf(stderr, "Failed to push frame to buffer, rc=%d\n", rc);
        return false;
    }

    av_frame_unref(frame);
    if((rc=av_buffersink_get_frame(_sink, frame))!=0)
    {
        fprintf(stderr, "Failed to pull frame from sink, rc=%d\n", rc);
        return false;
    }

    return true;
}
