#!/bin/bash

java -jar ../releases/de/unitrier/st/gitlogparser/1.1/gitlogparser-1.1-jar-with-dependencies.jar -i /data/comparison_project_sample_100/ -o /data/parsed-logs/ -f "rb java" > output.log 2>&1

