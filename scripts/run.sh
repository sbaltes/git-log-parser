#!/bin/bash

java -jar ../target/GitLogParser-1.0.2-jar-with-dependencies.jar -i /media/data/logs/ -o /media/data2/parsed-logs/ -f "rb java" > output.log 2>&1

