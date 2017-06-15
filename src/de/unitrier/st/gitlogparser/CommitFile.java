package de.unitrier.st.gitlogparser;

import org.apache.commons.io.FilenameUtils;

class CommitFile {
    private int linesAdded;
    private int linesDeleted;
    private String path;
    private String fileExtension;

    CommitFile() {
        linesAdded = 0;
        linesDeleted = 0;
        path = "";
        fileExtension = "";
    }

    CommitFile(int linesAdded, int linesDeleted, String path) {
        setLinesAdded(linesAdded);
        setLinesDeleted(linesDeleted);
        setPath(path);
    }

    int getLinesAdded() {
        return linesAdded;
    }

    void setLinesAdded(int linesAdded) {
        this.linesAdded = linesAdded;
    }

    int getLinesDeleted() {
        return linesDeleted;
    }

    void setLinesDeleted(int linesDeleted) {
        this.linesDeleted = linesDeleted;
    }

    String getPath() {
        return path;
    }

    void setPath(String path) {
        this.path = path;
        this.fileExtension = FilenameUtils.getExtension(path);
    }

    String getFileExtension() {
        return fileExtension;
    }
}