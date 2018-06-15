# git-log-parser

Parse files containing git-log data and export the extracted information to CSV files.

#### Build and execute from console

    mvn clean install

Windows:

    mvn exec:java -D"exec.mainClass"="de.unitrier.st.gitlogparser.GitLogParser" -D"exec.args"="-i /data/logs -o /data/parsed-logs -f java rb"

Linux/macOS:

    mvn exec:java -Dexec.mainClass="de.unitrier.st.gitlogparser.GitLogParser" -Dexec.args="-i /data/logs -o /data/parsed-logs -f java rb" > output.log

#### Parameters

`-i` or `--input-dir` Path to input directory
`-o` or `--output-dir` Path to output directory
`-f` or `--file-extension-filter` File extension filter (without point, separated by spaces)

[![DOI](https://zenodo.org/badge/94235400.svg)](https://zenodo.org/badge/latestdoi/94235400)
