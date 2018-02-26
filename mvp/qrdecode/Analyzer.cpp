// Blashyrkh.maniac.coding
// BTC:1Maniaccv5vSQVuwrmRtfazhf2WsUJ1KyD DOGE:DManiac9Gk31A4vLw9fLN9jVDFAQZc2zPj

#include "Analyzer.h"
#include <algorithm>
#include <png.h>
#include "VideoAnalyzer.h"
#include "JpegAnalyzer.h"


Analyzer::Analyzer()
{
    _imgscanner.set_config(zbar::ZBAR_NONE, zbar::ZBAR_CFG_ENABLE, 0);
    _imgscanner.set_config(zbar::ZBAR_QRCODE, zbar::ZBAR_CFG_ENABLE, 1);
    _imgscanner.set_config(zbar::ZBAR_QRCODE, zbar::ZBAR_CFG_MIN_LEN, 1);
}

std::vector<uint8_t> Analyzer::decodeNumber(const std::string &s)
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

bool Analyzer::debug_save_image_to_png(
    const unsigned char *data,
    unsigned int         width,
    unsigned int         height,
    const std::string   &filename)
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

void Analyzer::analyzeGrayscaleImage(
    unsigned int  width,
    unsigned int  height,
    const char   *data,
    Result       &result)
{
    zbar::Image zimg(width, height, "GREY", data, width*height);

    int res=_imgscanner.scan(zimg);

    zbar::SymbolSet ss=_imgscanner.get_results();
    for(zbar::SymbolIterator it=ss.symbol_begin(); it!=ss.symbol_end(); ++it)
    {
        if(it->get_type()==zbar::ZBAR_QRCODE)
        {
            std::vector<uint8_t> data=decodeNumber(it->get_data());
            if(data.size()==46)
                result.incrementCodeCounter(data);
        }
    }
}


std::unique_ptr<Analyzer> Analyzer::Factory::createAnalyzer(
    const std::string      &filename,
    const Analyzer::Config &config)
{
    auto dotpos=filename.rfind('.');
    if(dotpos==std::string::npos)
        return {};

    if(filename.compare(dotpos, std::string::npos, ".jpg")==0 ||
       filename.compare(dotpos, std::string::npos, ".JPG")==0 ||
       filename.compare(dotpos, std::string::npos, ".jpeg")==0 ||
       filename.compare(dotpos, std::string::npos, ".JPEG")==0)
    {
        return std::unique_ptr<Analyzer>(new JpegAnalyzer(filename, config));
    }
    else
    {
        return std::unique_ptr<Analyzer>(new VideoAnalyzer(filename, config));
    }
}


Analyzer::Config::Config() :
    _scaleWidth(0),
    _scaleHeight(0),
    _verbosity(0),
    _saveToPng(false)
{
}

Analyzer::Config &Analyzer::Config::setScale(unsigned int width, unsigned int height)
{
    _scaleWidth=width;
    _scaleHeight=height;
    return *this;
}

Analyzer::Config &Analyzer::Config::setVerbosity(int verbosity)
{
    _verbosity=verbosity;
    return *this;
}

Analyzer::Config &Analyzer::Config::setSaveToPng(bool saveToPng)
{
    _saveToPng=saveToPng;
    return *this;
}

unsigned int Analyzer::Config::getScaleWidth() const
{
    return _scaleWidth;
}

unsigned int Analyzer::Config::getScaleHeight() const
{
    return _scaleHeight;
}

int Analyzer::Config::getVerbosity() const
{
    return _verbosity;
}

bool Analyzer::Config::getSaveToPngFlag() const
{
    return _saveToPng;
}


Analyzer::Result::Result() : _error(false)
{
}

Analyzer::Result &Analyzer::Result::setError()
{
    _error=true;
    return *this;
}

Analyzer::Result &Analyzer::Result::incrementCodeCounter(const std::vector<uint8_t> &data)
{
    ++_codes[data];
    return *this;
}

Analyzer::Result::operator bool() const
{
    return !_error;
}

const std::map<std::vector<uint8_t>, unsigned int> &Analyzer::Result::getCodes() const
{
    return _codes;
}
