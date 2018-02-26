// Blashyrkh.maniac.coding
// BTC:1Maniaccv5vSQVuwrmRtfazhf2WsUJ1KyD DOGE:DManiac9Gk31A4vLw9fLN9jVDFAQZc2zPj

#include <getopt.h>
#include <stdlib.h>
#include <string>
#include <cstdio>
#include <cstring>
#include <iostream>
#include <cassert>
#include "Analyzer.h"


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

    auto config=Analyzer::Config();
    config.setScale(scaleWidth, scaleHeight);
    config.setVerbosity(verbosity);
    config.setSaveToPng(savePng);

    auto analyzer=Analyzer::Factory::createAnalyzer(filename, config);
    if(!analyzer)
    {
        fprintf(stderr, "File format is not supported\n");
        return 2;
    }

    auto result=analyzer->analyzeFile();
    if(!result)
    {
        return 2;
    }

    unsigned int mostFrequentCount=0;
    unsigned int totalCount=0;
    std::vector<uint8_t> mostFrequentCode;

    for(auto it=result.getCodes().begin(); it!=result.getCodes().end(); ++it)
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
