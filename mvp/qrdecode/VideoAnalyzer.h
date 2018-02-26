// Blashyrkh.maniac.coding
// BTC:1Maniaccv5vSQVuwrmRtfazhf2WsUJ1KyD DOGE:DManiac9Gk31A4vLw9fLN9jVDFAQZc2zPj

#ifndef _VideoAnalyzer_h
#define _VideoAnalyzer_h

#include "Analyzer.h"


class VideoAnalyzer : public Analyzer
{
public:
    VideoAnalyzer(
        const std::string &filename,
        const Config      &config);

    virtual Result analyzeFile() override;

private:
    const std::string _filename;
    const Config      _config;
};

#endif
