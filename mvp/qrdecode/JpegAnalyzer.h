// Blashyrkh.maniac.coding
// BTC:1Maniaccv5vSQVuwrmRtfazhf2WsUJ1KyD DOGE:DManiac9Gk31A4vLw9fLN9jVDFAQZc2zPj

#ifndef _JpegAnalyzer_h
#define _JpegAnalyzer_h

#include "Analyzer.h"


class JpegAnalyzer : public Analyzer
{
public:
    JpegAnalyzer(
        const std::string &filename,
        const Config      &config);

    virtual Result analyzeFile() override;

private:
    const std::string _filename;
    const Config      _config;
};

#endif
