package de.unitrier.st.gitlogparser;

import org.apache.commons.cli.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class GitLogParser {
    private static final Pattern fileNamePattern = Pattern.compile("(?i)^(.+_.+)§(.+)_(commits|merges)\\.log$");
    private static final Pattern commitHashPattern = Pattern.compile("(?i)^commit\\s+(\\w{40}).*");
    private static final Pattern authorNamePattern = Pattern.compile("(?i)^Author:\\s+([^<]+).*");
    private static final Pattern authorEmailPattern = Pattern.compile("(?i)^Author:\\s+[^<]+<([^>]+)>.*");
    private static final Pattern commitNamePattern = Pattern.compile("(?i)^Commit:\\s+([^<]+).*");
    private static final Pattern commitEmailPattern = Pattern.compile("(?i)^Commit:\\s+[^<]+<([^>]+)>.*");
    private static final Pattern mergePattern = Pattern.compile("(?i)^Merge:\\s+([\\w\\s]+).*");
    private static final Pattern authorDatePattern = Pattern.compile("(?i)^AuthorDate:\\s+([\\w\\s-:+]+).*");
    private static final Pattern commitDatePattern = Pattern.compile("(?i)^CommitDate:\\s+([\\w\\s-:+]+).*");
    private static final Pattern fileStatsPattern = Pattern.compile("(?i)^(\\d+|-)\\s+(\\d+|-)\\s+(.+)");
    private static final Pattern linesAddedDeletedPattern = Pattern.compile("(?i)^(\\d+)\\s+(\\d+)\\s+(.+)"); // ignores binary files (-	- PATH)
    private static final Pattern mergedBranchPattern = Pattern.compile("(?i)\\s*Merged?(?:\\s+remote)?(?:\\s+branch)?\\s+'([^\\s]+)'(?:\\s+of\\s+([^\\s]+))?(?:\\s+into\\s+([^\\s]+))?.*");
    private static final Pattern mergedRemoteTrackingBranchPattern = Pattern.compile("(?i)\\s*Merged? remote-tracking branch '([^\\s]+)'.*");
    private static final Pattern mergeTagPattern = Pattern.compile("(?i)\\s*Merged?\\s+tag\\s+'(.+)'(?:\\s+into\\s+([^\\s]+))?.*");
    private static final Pattern mergedPullRequestPattern = Pattern.compile("(?i)\\s*Merged?\\s+pull\\s+request\\s+#(\\d+)(?:\\s+from ([^\\s]+)/([^\\s]+))?.*");
    private static final Pattern mergedCommitPattern = Pattern.compile("(?i)\\s*Merged?(?:\\s+commit)?\\s+'([^\\s]+)'(?:\\s+into\\s+([^\\s]+))?.*");

    private Path inputDirPath, outputDirPath;
    private LinkedList<Commit> commits;
    private String project;
    private String branch;
    private String type; // "commits" or "merges"

    GitLogParser(Path inputDirPath, Path outputDirPath) {
        this.inputDirPath = inputDirPath;
        this.outputDirPath = outputDirPath;
    }

    void parseFiles() {

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(inputDirPath)) {
            for (Path path : directoryStream) {
                File file = new File(path.toAbsolutePath().toString());
                if (file.exists() && file.isFile() && !file.isHidden()
                        && FilenameUtils.getExtension(file.getName()).equals("log")) {
                    commits = parseFile(file);
                    writeData(outputDirPath);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private LinkedList<Commit> parseFile(File file) {

        System.out.println("Parsing file: " + file.getName());

        commits = new LinkedList<>();
        project = "";
        branch = "";
        type = "";

        try {
            if (file.getName().endsWith("_commits.log")) {
                type = "commits";
            } else if (file.getName().endsWith("_merges.log")){
                type = "merges";
            } else {
                throw new IllegalArgumentException("File must either be a commit or merge log file.");
            }

            // extract project and branch from file name
            Matcher fileNameMatcher = fileNamePattern.matcher(file.getName());
            if (fileNameMatcher.matches()) {
                project = fileNameMatcher.group(1);
                branch = fileNameMatcher.group(2);
            } else {
                throw new IllegalArgumentException("Illegal file name format.");
            }

            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            Commit currentCommit = null;
            boolean readingHeader = true;
            boolean readingLogMessage = false;
            StringBuilder logMessageBuilder = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                // commit hash
                Matcher commitHashMatcher = commitHashPattern.matcher(line);
                if (commitHashMatcher.matches()) {

                    // save previous commit
                    if (currentCommit != null) {
                        // save log message without trailing empty lines
                        currentCommit.setLogMessage(logMessageBuilder.toString().trim());
                        logMessageBuilder = new StringBuilder();
                        readingLogMessage = false; // needed in case file stats not present on log (true for merges)
                        commits.add(currentCommit);
                    }

                    String commitHash = commitHashMatcher.group(1);
                    currentCommit = new Commit(project, branch, commitHash);
                    readingHeader = true;
                    continue;
                }

                if (readingHeader && currentCommit != null) {
                    // merged commits
                    Matcher mergeMatcher = mergePattern.matcher(line);
                    if (mergeMatcher.matches()) {
                        String mergedCommits = mergeMatcher.group(1);
                        currentCommit.setMergedCommits(mergedCommits);
                        continue;
                    }

                    // author name and email
                    Matcher authorNameMatcher = authorNamePattern.matcher(line);
                    Matcher authorEmailMatcher = authorEmailPattern.matcher(line);
                    if (authorNameMatcher.matches() || authorEmailMatcher.matches()) {
                        if (authorNameMatcher.matches()) {
                            String authorName = authorNameMatcher.group(1).trim();
                            currentCommit.setAuthorName(authorName);
                        }
                        if (authorEmailMatcher.matches()) {
                            String authorEmail = authorEmailMatcher.group(1).trim();
                            if (authorEmail.contains("@")) {
                                currentCommit.setAuthorEmail(authorEmail);
                            } else {
                                currentCommit.setAuthorEmail("");
                            }
                        }
                        continue;
                    }

                    // author date
                    Matcher authorDateMatcher = authorDatePattern.matcher(line);
                    if (authorDateMatcher.matches()) {
                        String date = authorDateMatcher.group(1);
                        currentCommit.setAuthorDate(date);
                        continue;
                    }

                    // commit name and email
                    Matcher commitNameMatcher = commitNamePattern.matcher(line);
                    Matcher commitEmailMatcher = commitEmailPattern.matcher(line);
                    if (commitNameMatcher.matches() || commitEmailMatcher.matches()) {
                        if (commitNameMatcher.matches()) {
                            String commitName = commitNameMatcher.group(1).trim();
                            currentCommit.setCommitName(commitName);
                        }
                        if (commitEmailMatcher.matches()) {
                            String commitEmail = commitEmailMatcher.group(1).trim();
                            if (commitEmail.contains("@")) {
                                currentCommit.setCommitEmail(commitEmail);
                            } else {
                                currentCommit.setCommitEmail("");
                            }
                        }
                        continue;
                    }

                    // commit date
                    Matcher commitDateMatcher = commitDatePattern.matcher(line);
                    if (commitDateMatcher.matches()) {
                        String date = commitDateMatcher.group(1);
                        currentCommit.setCommitDate(date);
                        continue;
                    }

                    // detect beginning of log message
                    // an empty line separates the header from the log message and the file stats from the next commit,
                    // see https://git-scm.com/docs/pretty-formats
                    if (line.trim().length() == 0) {
                        readingHeader = false;
                        readingLogMessage = true;
                    }
                } else { // readingHeader is false

                    if (currentCommit == null) {
                        continue;
                    }

                    // file stats (lines added/deleted or "- -" in case of binary files)
                    Matcher fileStatsMatcher = fileStatsPattern.matcher(line);
                    if (fileStatsMatcher.matches()) {
                        readingLogMessage = false;

                        // extract lines added/deleted and ignore binary files ("- -")
                        Matcher linesAddedDeletedMatcher = linesAddedDeletedPattern.matcher(line);
                        if (linesAddedDeletedMatcher.matches()) {
                            int linesAdded = Integer.parseInt(linesAddedDeletedMatcher.group(1));
                            int linesDeleted = Integer.parseInt(linesAddedDeletedMatcher.group(2));
                            String path = linesAddedDeletedMatcher.group(3);
                            currentCommit.addFile(new CommitFile(linesAdded, linesDeleted, path));
                            continue;
                        }
                    }

                    if (readingLogMessage) {
                        // log messages are indented by 4 blanks
                        if (line.startsWith("    ")) {
                            line = line.substring(4);
                        }

                        // append current line to string builder
                        logMessageBuilder.append(line).append("\n");

                        // check if log contains information about merged branch
                        Matcher mergedBranchMatcher = mergedBranchPattern.matcher(line);
                        if (mergedBranchMatcher.matches()) {
                            String sourceBranch = mergedBranchMatcher.group(1);
                            currentCommit.setSourceBranch(sourceBranch);

                            if (mergedBranchMatcher.group(2) != null) {
                                // "of"-part present
                                String sourceRepo = mergedBranchMatcher.group(2);
                                currentCommit.setSourceRepo(sourceRepo);
                            }

                            if (mergedBranchMatcher.group(3) != null) {
                                // "into"-part present
                                String targetBranch = mergedBranchMatcher.group(3);
                                currentCommit.setTargetBranch(targetBranch);
                            }

                            continue;
                        }

                        // check if log contains information about merged remote-tracking branch
                        Matcher mergedRemoteTrackingBranchMatcher = mergedRemoteTrackingBranchPattern.matcher(line);
                        if (mergedRemoteTrackingBranchMatcher.matches()) {
                            String remoteTrackingBranch = mergedRemoteTrackingBranchMatcher.group(1);
                            currentCommit.setSourceBranch(remoteTrackingBranch);
                            continue;
                        }

                        // check if log contains information about merged tag
                        Matcher mergedTagMatcher = mergeTagPattern.matcher(line);
                        if (mergedTagMatcher.matches()) {
                            String tagName = mergedTagMatcher.group(1);
                            currentCommit.setTagName(tagName);

                            if (mergedTagMatcher.group(2) != null) {
                                String targetBranch = mergedTagMatcher.group(2);
                                currentCommit.setTargetBranch(targetBranch);
                            }

                            continue;
                        }

                        // check if log contains information about merged pull request
                        Matcher mergedPullRequestMatcher = mergedPullRequestPattern.matcher(line);
                        if (mergedPullRequestMatcher.matches()) {
                            String pullRequestId = mergedPullRequestMatcher.group(1);
                            currentCommit.setPullRequestId(pullRequestId);

                            if (mergedPullRequestMatcher.group(2) != null && mergedPullRequestMatcher.group(3) != null) {
                                // "from"-part present
                                String pullRequestUser = mergedPullRequestMatcher.group(2);
                                currentCommit.setSourceUser(pullRequestUser);
                                String sourceBranch = mergedPullRequestMatcher.group(3);
                                currentCommit.setSourceBranch(sourceBranch);
                            }

                            continue;
                        }

                        // check if commit contains information about merged commit (cherry-picking)
                        Matcher mergedCommitMatcher = mergedCommitPattern.matcher(line);
                        if (mergedCommitMatcher.matches()) {
                            String mergedCommit = mergedCommitMatcher.group(1);
                            currentCommit.setSourceCommit(mergedCommit);

                            if (mergedCommitMatcher.group(2) != null) {
                                // "into"-part present
                                String targetBranch = mergedCommitMatcher.group(2);
                                currentCommit.setTargetBranch(targetBranch);
                            }

                            continue;
                        }

                        // print merge log messages that were not matched
                        //if (line.trim().toLowerCase().startsWith("merge")) {
                        //    System.out.println(line);
                        //}
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        return commits;
    }

    private void writeData(Path outputDirPath) {

        File outputDir = new File(outputDirPath.toAbsolutePath().toString());

        if (!(outputDir.exists() && outputDir.isDirectory())) {
            throw new IllegalArgumentException("Illegal output directory");
        }

        CSVFormat csvFormat = CSVFormat.DEFAULT
                .withDelimiter(';')
                .withQuote('"')
                .withQuoteMode(QuoteMode.ALL)
                .withEscape('\\')
                .withNullString("");

        switch (type) {
            case "commits":
                csvFormat = csvFormat.withHeader(Commit.csvHeaderCommits.class);
                break;
            case "merges":
                csvFormat = csvFormat.withHeader(Commit.csvHeaderMerges.class);
                break;
        }

        Path targetFilePath = Paths.get(outputDirPath.toAbsolutePath().toString(),
                project + "§" + branch + "_" + type + ".csv");
        File targetFile = new File(targetFilePath.toAbsolutePath().toString());

        System.out.println("Writing file: " + targetFile.getName());

        try (CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(targetFile), csvFormat)) {
            // header is automatically written
            // write commit data
            for (Commit commit : commits) {
                switch (type) {
                    case "commits":
                        csvPrinter.printRecord(Arrays.asList(commit.getValuesCommits()));
                        break;
                    case "merges":
                        csvPrinter.printRecord(Arrays.asList(commit.getValuesMerges()));
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {

        System.out.println("GitLogParser");

        Options options = new Options();

        Option inputDir = new Option("i", "input-dir", true, "path to input directory");
        inputDir.setRequired(true);
        options.addOption(inputDir);

        Option outputDir = new Option("o", "output-dir", true, "path to output directory");
        inputDir.setRequired(true);
        options.addOption(outputDir);

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
        Path outputDirPath = Paths.get(commandLine.getOptionValue("output-dir"));

        GitLogParser gitLogParser = new GitLogParser(inputDirPath, outputDirPath);
        gitLogParser.parseFiles();
    }
}
