package com.spayker.ac.model.git;

import org.eclipse.jgit.api.Git;

public class GitData {

    private Git git;
    private String folderName;

    private GitData() { }

    public GitData(Git git, String folderName) {
        this.git = git;
        this.folderName = folderName;
    }

    public Git getGit() {
        return git;
    }

    public String getFolderName() {
        return folderName;
    }
}
