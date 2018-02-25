// Blashyrkh.maniac.coding
// BTC:1Maniaccv5vSQVuwrmRtfazhf2WsUJ1KyD DOGE:DManiac9Gk31A4vLw9fLN9jVDFAQZc2zPj

#ifndef _Analyzer_h
#define _Analyzer_h

#include <vector>
#include <string>
#include <map>
#include <cstdint>
#include <memory>
#include <zbar.h>


class Analyzer
{
public:
    class Factory;
    class Config;
    class Result;

    Analyzer();
    virtual ~Analyzer() = default;

    virtual Result analyzeFile() = 0;

protected:
    static std::vector<uint8_t> decodeNumber(const std::string &s);
    static bool debug_save_image_to_png(
        const unsigned char *data,
        unsigned int         width,
        unsigned int         height,
        const std::string   &filename);
    void analyzeGrayscaleImage(
        unsigned int  width,
        unsigned int  height,
        const char   *data,
        Result       &result);

private:
    zbar::ImageScanner _imgscanner;
};

class Analyzer::Factory
{
public:
    static std::unique_ptr<Analyzer> createAnalyzer(
        const std::string      &filename,
        const Analyzer::Config &config);
};

class Analyzer::Config
{
public:
    Config();

    Config &setScale(unsigned int width, unsigned int height);
    Config &setVerbosity(int verbosity);
    Config &setSaveToPng(bool saveToPng);

    unsigned int getScaleWidth() const;
    unsigned int getScaleHeight() const;
    int getVerbosity() const;
    bool getSaveToPngFlag() const;

private:
    unsigned int _scaleWidth;
    unsigned int _scaleHeight;
    int          _verbosity;
    bool         _saveToPng;
};

class Analyzer::Result
{
public:
    Result();

    Result &setError();
    Result &incrementCodeCounter(const std::vector<uint8_t> &data);

    operator bool() const;
    const std::map<std::vector<uint8_t>, unsigned int> &getCodes() const;

private:
    bool                                         _error;
    std::map<std::vector<uint8_t>, unsigned int> _codes;
};

#endif
