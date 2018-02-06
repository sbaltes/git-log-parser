#!/bin/bash

java -jar ../releases/de/unitrier/st/gitlogparser/1.1/gitlogparser-1.1-jar-with-dependencies.jar -i /media/data/logs/ -o /media/data2/parsed-logs/ -f "rb java" > output.log 2>&1

