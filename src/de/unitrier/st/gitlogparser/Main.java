package de.unitrier.st.gitlogparser;

import org.apache.commons.cli.*;

import java.nio.file.Path;
import java.nio.file.Paths;

class Main {
    public static void main(String[] args) {

        System.out.println("GitLogParser");

        Options options = new Options();

        Option inputDir = new Option("i", "input-dir", true, "path to input directory");
        inputDir.setRequired(true);
        options.addOption(inputDir);

        CommandLineParser commandLineParser = new DefaultParser();
        HelpFormatter commandLineFormatter = new HelpFormatter();
        CommandLine commandLine;

        try {
            commandLine = commandLineParser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            commandLineFormatter.printHelp("GitLogParser", options);
            System.exit(1);
            return;
        }

        Path inputDirPath = Paths.get(commandLine.getOptionValue("input-dir"));

        GitLogParser gitLogParser = new GitLogParser(inputDirPath);
        gitLogParser.parseFiles();

    }
}
