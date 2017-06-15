package de.unitrier.st.gitlogparser;

import java.util.ArrayList;
import java.util.HashSet;

class Commit {
    private String project;
    private String branch;
    private String hashValue;
    private String authorName;
    private String authorEmail;
    private String authorDate;
    private String commitName;
    private String commitEmail;
    private String commitDate;
    private String logMessage;
    private int logMessageLength;
    // only for regular commit
    private final ArrayList<CommitFile> files;
    // only for merge
    private String mergedCommits;
    private String sourceUser; // for pull requests, e.g. "Merge pull request #4996 from Fivell/issue_4977"
    private String sourceRepo; // e.g., "Merge branch 'master' of https://github.com/gregbell/active_admin into upstream/master"
    private String sourceBranch; // remote-tracking branches usually start with "origin/"
    private String sourceCommit; // cherry-pick, e.g., "Merge commit 'f6c66cec1a653af99f2abca222718d105c8ad39d' into dependency-management-refactor", but also Merge commit 'vhochstein/master'
    private String targetBranch;
    // only for merged pull request
    private String pullRequestId;
    // only for merged tag
    private String tagName;

    Commit(String project, String branch, String hashValue) {
        this.project = project;
        this.branch = branch;
        this.hashValue = hashValue;
        files = new ArrayList<>();
    }

    @Override
    public String toString() {
        String output = "";
        output += "Project: " + project + "; ";
        output += "Branch: " + branch + "; ";
        output += "Hash: " + hashValue + "; ";
        output += "Author: " + authorName +  " " + authorEmail + "; ";
        output += "AuthorDate: " + authorDate + "; ";
        output += "Committer: " + commitName +  " " + commitEmail + "; ";
        output += "CommitDate: " + commitDate + "; ";
        output += "LogMessageLength: " + logMessageLength + "; ";
        output += "FileCount: " + files.size() + "; ";
        output += "MergedCommits: " + mergedCommits + "; ";
        output += "SourceUser: " + sourceUser + "; ";
        output += "SourceRepo: " + sourceRepo + "; ";
        output += "SourceBranch: " + sourceBranch + "; ";
        output += "SourceCommit: " + sourceCommit + "; ";
        output += "TargetBranch: " + targetBranch + "; ";
        output += "PullRequestId: " + pullRequestId + "; ";
        output += "TagName: " + tagName + "; ";
        return output;
    }

    ArrayList<CommitFile> getFiles() {
        return files;
    }

    void addFile(CommitFile file) {
        files.add(file);
    }

    String getProject() {
        return project;
    }

    void setProject(String project) {
        this.project = project.trim();
    }

    String getBranch() {
        return branch;
    }

    void setBranch(String branch) {
        this.branch = branch.trim();
    }

    String getHashValue() {
        return hashValue;
    }

    void setHashValue(String hashValue) {
        this.hashValue = hashValue.trim();
    }

    String getAuthorName() {
        return authorName;
    }

    void setAuthorName(String authorName) {
        this.authorName = authorName.trim();
    }

    String getAuthorEmail() {
        return authorEmail;
    }

    void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail.trim();
    }

    String getAuthorDate() {
        return authorDate;
    }

    void setAuthorDate(String authorDate) {
        this.authorDate = authorDate.trim();
    }

    String getCommitName() {
        return commitName;
    }

    void setCommitName(String commitName) {
        this.commitName = commitName.trim();
    }

    String getCommitEmail() {
        return commitEmail;
    }

    void setCommitEmail(String commitEmail) {
        this.commitEmail = commitEmail.trim();
    }

    String getCommitDate() {
        return commitDate;
    }

    void setCommitDate(String commitDate) {
        this.commitDate = commitDate.trim();
    }

    String getLogMessage() {
        return logMessage;
    }

    void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
        this.logMessageLength = logMessage.length();
    }

    int getLogMessageLength() {
        return logMessageLength;
    }

    String getMergedCommits() {
        return mergedCommits;
    }

    void setMergedCommits(String mergedCommits) {
        this.mergedCommits = mergedCommits.trim();
    }

    String getSourceBranch() {
        return sourceBranch;
    }

    void setSourceBranch(String sourceBranch) {
        this.sourceBranch = sourceBranch.trim();
    }

    String getSourceRepo() {
        return sourceRepo;
    }

    void setSourceRepo(String sourceRepo) {
        this.sourceRepo = sourceRepo;
    }

    String getSourceCommit() {
        return sourceCommit;
    }

    void setSourceCommit(String sourceCommit) {
        this.sourceCommit = sourceCommit;
    }

    String getTargetBranch() {
        return targetBranch;
    }

    void setTargetBranch(String targetBranch) {
        this.targetBranch = targetBranch.trim();
    }

    String getPullRequestId() {
        return pullRequestId;
    }

    void setPullRequestId(String pullRequestId) {
        this.pullRequestId = pullRequestId;
    }

    String getSourceUser() {
        return sourceUser;
    }

    void setSourceUser(String sourceUser) {
        this.sourceUser = sourceUser.trim();
    }

    String getTagName() {
        return tagName;
    }

    void setTagName(String tagName) {
        this.tagName = tagName.trim();
    }

    int getFileCount() {
        return files.size();
    }

    int getLinesAdded() {
        int linesAdded = 0;
        for (CommitFile file : files) {
            linesAdded += file.getLinesAdded();
        }
        return linesAdded;
    }

    int getLinesDeleted() {
        int linesDeleted = 0;
        for (CommitFile file : files) {
            linesDeleted += file.getLinesDeleted();
        }
        return linesDeleted;
    }

    String getFileExtensions() {
        HashSet<String> fileExtensions = new HashSet<>();
        for (CommitFile file : files) {
            String fileExtension = file.getFileExtension();
            if (file.getFileExtension().length() == 0) {
                fileExtension = "none";
            }
            fileExtensions.add(fileExtension);
        }
        String result = "";
        for (String fileExtension : fileExtensions) {
            result = fileExtension.length() == 0 ? fileExtension : result + " " + fileExtension;
        }
        return result;
    }

    enum csvHeaderCommits {
        project, branch, hash_value,
        author_name, author_email, author_date,
        commit_name, commit_email, commit_date,
        log_message_length, /*log_message,*/
        file_count, lines_added, lines_deleted,
        file_extensions
    }

    String[] getValuesCommits() {
        return new String[]{
                getProject(), getBranch(), getHashValue(),
                getAuthorName(), getAuthorEmail(), getAuthorDate(),
                getCommitName(), getCommitEmail(), getCommitDate(),
                String.valueOf(getLogMessageLength()), /*getLogMessage(),*/
                String.valueOf(getFileCount()), String.valueOf(getLinesAdded()), String.valueOf(getLinesDeleted()),
                getFileExtensions(),
        };
    }

    enum csvHeaderMerges {
        project, branch, hash_value, merged_commits,
        author_name, author_email, author_date,
        commit_name, commit_email, commit_date,
        log_message_length, /*log_message,*/
        source_user, source_repo, source_branch, source_commit, target_branch,
        pull_request_id,
        tag_name
    }

    String[] getValuesMerges() {
        return new String[]{
                getProject(), getBranch(), getHashValue(), getMergedCommits(),
                getAuthorName(), getAuthorEmail(), getAuthorDate(),
                getCommitName(), getCommitEmail(), getCommitDate(),
                String.valueOf(getLogMessageLength()), /*getLogMessage(),*/
                getSourceUser(), getSourceRepo(), getSourceBranch(), getSourceCommit(), getTargetBranch(),
                getPullRequestId(),
                getTagName()
        };
    }

}
