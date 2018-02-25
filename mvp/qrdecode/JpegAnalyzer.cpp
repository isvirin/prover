// Blashyrkh.maniac.coding
// BTC:1Maniaccv5vSQVuwrmRtfazhf2WsUJ1KyD DOGE:DManiac9Gk31A4vLw9fLN9jVDFAQZc2zPj

#include "JpegAnalyzer.h"
#include <cstdio>
#include <cstring>
#include <jpeglib.h>


JpegAnalyzer::JpegAnalyzer(
    const std::string &filename,
    const Config      &config) :

    _filename(filename),
    _config(config)
{
}

Analyzer::Result JpegAnalyzer::analyzeFile()
{
    Result result;

    FILE *f=fopen(_filename.c_str(), "rb");
    if(!f)
    {
        fprintf(stderr, "Failed to open file\n");
        return result.setError();
    }

    struct jpeg_decompress_struct jpeg;
    struct jpeg_error_mgr jerr;
    memset(&jpeg, 0, sizeof(jpeg));
    jpeg.err=jpeg_std_error(&jerr);

    // TODO: custom error handling

    jpeg_create_decompress(&jpeg);

    jpeg_stdio_src(&jpeg, f);

    jpeg_read_header(&jpeg, true);

    jpeg.out_color_space=JCS_GRAYSCALE;
    jpeg_start_decompress(&jpeg);

    if(_config.getVerbosity()>0)
        fprintf(stderr, "Starting decompress, w=%u h=%u\n", jpeg.output_width, jpeg.output_height);

    std::vector<uint8_t> imagedata;
    imagedata.resize(jpeg.output_width*jpeg.output_height);

    std::vector<JSAMPROW> scanlines;
    scanlines.reserve(jpeg.output_height);
    for(unsigned int i=0; i<jpeg.output_height; ++i)
        scanlines.push_back(imagedata.data()+i*jpeg.output_width);

    unsigned int y=0;
    while(y<jpeg.output_height)
    {
        int rc=jpeg_read_scanlines(&jpeg, scanlines.data()+y, jpeg.output_height-y);
        if(rc<=0)
        {
            fprintf(stderr, "jpeg_read_scanlines retcode is %d\n", rc);
            return result.setError();
        }
        y+=rc;
    }

    analyzeGrayscaleImage(
        jpeg.output_width,
        jpeg.output_height,
        (const char *)imagedata.data(),
        result);

    jpeg_finish_decompress(&jpeg);
    jpeg_destroy_decompress(&jpeg);

    fclose(f);

    return result;
}
