package de.unitrier.st.gitlogparser;

import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class GitLogParser {
    private static final Pattern fileNamePattern = Pattern.compile("^(.+_.+)§(.+)\\.log$");
    private static final Pattern commitHashPattern = Pattern.compile("^commit\\s+(\\w{40}).*");
    private static final Pattern authorNamePattern = Pattern.compile("^Author:\\s+([^<]+).*");
    private static final Pattern authorEmailPattern = Pattern.compile("^Author:\\s+[^<]+<([^>]+)>.*");
    private static final Pattern commitNamePattern = Pattern.compile("^Commit:\\s+([^<]+).*");
    private static final Pattern commitEmailPattern = Pattern.compile("^Commit:\\s+[^<]+<([^>]+)>.*");
    private static final Pattern mergePattern = Pattern.compile("^Merge:\\s+([\\w\\s]+).*");
    private static final Pattern authorDatePattern = Pattern.compile("^AuthorDate:\\s+([\\w\\s-:+]+).*");
    private static final Pattern commitDatePattern = Pattern.compile("^CommitDate:\\s+([\\w\\s-:+]+).*");
    private static final Pattern linesAddedDeletedPattern = Pattern.compile("^(\\d+)\\s+(\\d+)\\s+(.+)"); // ignores binary files (-	- PATH)
    private static final Pattern mergedBranchPattern = Pattern.compile("\\s*Merge branch '(.+)' into (.+)");
    private static final Pattern mergedRemoteTrackingBranchPattern = Pattern.compile("\\s*Merge remote-tracking branch '(.+)'.*");
    private static final Pattern mergeTagPattern = Pattern.compile("\\s*Merge tag '(.+)' into (.+)");
    private static final Pattern mergedPullRequestPattern = Pattern.compile("^\\s*Merge pull request #(\\d+) from (.+)/(.+)");

    private Path inputDir;

    GitLogParser(Path inputDir) {
        this.inputDir = inputDir;
    }

    void parseFiles() {

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(inputDir)) {
            for (Path path : directoryStream) {
                File file = new File(path.toAbsolutePath().toString());
                if (file.exists() && file.isFile() && !file.isHidden()
                        && FilenameUtils.getExtension(file.getName()).equals("log")) {
                    System.out.println("Now parsing file: " + file.getName());
                    parseFile(file);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void parseFile(File file) {
        try {
            if (!(file.getName().endsWith("_commits.log") || (file.getName().endsWith("_merges.log")))) {
                throw new IllegalArgumentException("File must either be a commit or merge log file.");
            }

            Commit commit = null;
            String project;
            String branch;

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
            boolean readingLogMessage = false;
            boolean readingFileStats = false;
            StringBuilder logMessageBuilder = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                if (!readingLogMessage) {
                    // commit hash
                    Matcher commitHashMatcher = commitHashPattern.matcher(line);
                    if (commitHashMatcher.matches()) {
                        String commitHash = commitHashMatcher.group(1);
                        commit = new Commit(project, branch, commitHash);
                        continue;
                    }

                    // merged commits
                    Matcher mergeMatcher = mergePattern.matcher(line);
                    if (mergeMatcher.matches()) {
                        String mergedCommits = mergeMatcher.group(1);
                        commit.setMergedCommits(mergedCommits);
                        continue;
                    }

                    // author name and email
                    Matcher authorNameMatcher = authorNamePattern.matcher(line);
                    Matcher authorEmailMatcher = authorEmailPattern.matcher(line);
                    if (authorNameMatcher.matches() || authorEmailMatcher.matches()) {
                        if (authorNameMatcher.matches()) {
                            String authorName = authorNameMatcher.group(1).trim();
                            commit.setAuthorName(authorName);
                        }
                        if (authorEmailMatcher.matches()) {
                            String authorEmail = authorEmailMatcher.group(1).trim();
                            commit.setAuthorEmail(authorEmail);
                        }
                        continue;
                    }

                    // author date
                    Matcher authorDateMatcher = authorDatePattern.matcher(line);
                    if (authorDateMatcher.matches()) {
                        String date = authorDateMatcher.group(1);
                        commit.setAuthorDate(date);
                        continue;
                    }

                    // commit name and email
                    Matcher commitNameMatcher = commitNamePattern.matcher(line);
                    Matcher commitEmailMatcher = commitEmailPattern.matcher(line);
                    if (commitNameMatcher.matches() || commitEmailMatcher.matches()) {
                        if (commitNameMatcher.matches()) {
                            String commitName = commitNameMatcher.group(1).trim();
                            commit.setCommitName(commitName);
                        }
                        if (commitEmailMatcher.matches()) {
                            String commitEmail = commitEmailMatcher.group(1).trim();
                            commit.setCommitEmail(commitEmail);
                        }
                        continue;
                    }

                    // commit date
                    Matcher commitDateMatcher = commitDatePattern.matcher(line);
                    if (commitDateMatcher.matches()) {
                        String date = commitDateMatcher.group(1);
                        commit.setCommitDate(date);
                        continue;
                    }

                    // file stats (lines added/deleted)
                    Matcher linesAddedDeletedMatcher = linesAddedDeletedPattern.matcher(line);
                    if (linesAddedDeletedMatcher.matches()) {
                        readingFileStats = true;
                        int linesAdded = Integer.parseInt(linesAddedDeletedMatcher.group(1));
                        int linesDeleted = Integer.parseInt(linesAddedDeletedMatcher.group(2));
                        String path = linesAddedDeletedMatcher.group(3);
                        commit.addFile(new CommitFile(linesAdded, linesDeleted, path));
                        continue;
                    }

                    // detect beginning of log message
                    if (line.trim().length() == 0) {
                        // an empty line separates the header from the log message and the file stats from the next commit,
                        // see https://git-scm.com/docs/pretty-formats
                        if (readingFileStats) {
                            // end of file stats
                            readingFileStats = false;
                        } else {
                            // begin of log message
                            readingLogMessage = true;
                        }
                    }
                } else { // readingLogMessage is true

                    // detect end of log message
                    if (line.trim().length() == 0) {
                        // save log message
                        commit.setLogMessage(logMessageBuilder.toString());
                        logMessageBuilder = new StringBuilder();

                        // an empty line separates the header from the log message and the file stats,
                        // see https://git-scm.com/docs/pretty-formats
                        readingLogMessage = false;
                        continue;
                    }

                    // log messages are indented by 4 blanks
                    if (line.startsWith("    ")) {
                        line = line.substring(4);
                    }

                    // append current line to string builder
                    logMessageBuilder.append(line);

                    // check if log contains information about merged branch
                    Matcher mergedBranchMatcher = mergedBranchPattern.matcher(line);
                    if (mergedBranchMatcher.matches()) {
                        String sourceBranch = mergedBranchMatcher.group(1);
                        String targetBranch = mergedBranchMatcher.group(2);
                        commit.setSourceBranch(sourceBranch);
                        commit.setTargetBranch(targetBranch);
                        continue;
                    }

                    // check if log contains information about merged remote-tracking branch
                    Matcher mergedRemoteTrackingBranchMatcher = mergedRemoteTrackingBranchPattern.matcher(line);
                    if (mergedBranchMatcher.matches()) {
                        String remoteTrackingBranch = mergedRemoteTrackingBranchMatcher.group(1);
                        commit.setSourceBranch(remoteTrackingBranch);
                        continue;
                    }

                    // check if log contains information about merged tag
                    Matcher mergedTagMatcher = mergeTagPattern.matcher(line);
                    if (mergedTagMatcher.matches()) {
                        String tagName = mergedTagMatcher.group(1);
                        String targetBranch = mergedTagMatcher.group(2);
                        commit.setTagName(tagName);
                        commit.setTargetBranch(targetBranch);
                        System.out.println(line);
                        continue;
                    }

                    // check if log contains information about merged pull request
                    Matcher mergedPullRequestMatcher = mergedPullRequestPattern.matcher(line);
                    if (mergedPullRequestMatcher.matches()) {
                        String pullRequestId = mergedPullRequestMatcher.group(1);
                        String pullRequestUser = mergedPullRequestMatcher.group(2);
                        String sourceBranch = mergedPullRequestMatcher.group(3);
                        commit.setPullRequestId(Integer.parseInt(pullRequestId));
                        commit.setSourceBranch(sourceBranch);
                        commit.setPullRequestUser(pullRequestUser);
                        continue;
                    }

                    if (line.trim().startsWith("merge")) {
                        System.out.println(line);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
