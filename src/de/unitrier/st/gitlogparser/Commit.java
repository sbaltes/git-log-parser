package de.unitrier.st.gitlogparser;

import java.util.ArrayList;

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
    private String sourceBranch;
    private String targetBranch;
    // only for merged pull request
    private int pullRequestId;
    private String pullRequestUser;
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
        output += "SourceBranch: " + sourceBranch + "; ";
        output += "TargetBranch: " + targetBranch + "; ";
        output += "PullRequestId: " + pullRequestId + "; ";
        output += "PullRequestUser: " + pullRequestUser + "; ";
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
        this.project = project;
    }

    String getBranch() {
        return branch;
    }

    void setBranch(String branch) {
        this.branch = branch;
    }

    String getHashValue() {
        return hashValue;
    }

    void setHashValue(String hashValue) {
        this.hashValue = hashValue;
    }

    String getAuthorName() {
        return authorName;
    }

    void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    String getAuthorEmail() {
        return authorEmail;
    }

    void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    String getAuthorDate() {
        return authorDate;
    }

    void setAuthorDate(String authorDate) {
        this.authorDate = authorDate;
    }

    String getCommitName() {
        return commitName;
    }

    void setCommitName(String commitName) {
        this.commitName = commitName;
    }

    String getCommitEmail() {
        return commitEmail;
    }

    void setCommitEmail(String commitEmail) {
        this.commitEmail = commitEmail;
    }

    String getCommitDate() {
        return commitDate;
    }

    void setCommitDate(String commitDate) {
        this.commitDate = commitDate;
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
        this.mergedCommits = mergedCommits;
    }

    String getSourceBranch() {
        return sourceBranch;
    }

    void setSourceBranch(String sourceBranch) {
        this.sourceBranch = sourceBranch;
    }

    String getTargetBranch() {
        return targetBranch;
    }

    void setTargetBranch(String targetBranch) {
        this.targetBranch = targetBranch;
    }

    int getPullRequestId() {
        return pullRequestId;
    }

    void setPullRequestId(int pullRequestId) {
        this.pullRequestId = pullRequestId;
    }

    String getPullRequestUser() {
        return pullRequestUser;
    }

    void setPullRequestUser(String pullRequestUser) {
        this.pullRequestUser = pullRequestUser;
    }

    String getTagName() {
        return tagName;
    }

    void setTagName(String tagName) {
        this.tagName = tagName;
    }
}
