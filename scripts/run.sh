#!/bin/bash

#java -jar ../releases/de/unitrier/st/gitlogparser/1.1.0/gitlogparser-1.1.0-jar-with-dependencies.jar -i /data/logs/ -o /data/parsed-logs/ -f "rb java" > output_parse.log 2>&1
./merge_parsed_logs.sh  /data/parsed-logs > output_merge.log 2>&1
