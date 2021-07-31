package com.spayker.ac.jgit.model.git;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.eclipse.jgit.api.Git;

@AllArgsConstructor
public class GitData {

    @Getter
    private Git git;

    @Getter
    private String folderName;

    private GitData() { }
}
