package com.spayker.ac.util.file;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class GitFolderRecognizer {

    private static final String GIT_FOLDER_NAME = ".git";

    public List<File> getGitFolders(String projectsPath) {
        File directory = new File(projectsPath);
        List<File> subFolders = getSubDirs(directory);

        return subFolders.stream()
                .filter(f -> containGitFolder(f.getPath()))
                .collect(Collectors.toList());
    }

    boolean containGitFolder(String absolutePath) {
        File directory = new File(absolutePath);
        File[] sameLevelFolders = directory.listFiles(File::isDirectory);
        if (sameLevelFolders == null) {
            return false;
        } else {
            return Arrays.stream(sameLevelFolders)
                    .anyMatch(f -> f.getName().equalsIgnoreCase(GIT_FOLDER_NAME));
        }
    }

    List<File> getSubDirs(File file) {
        File[] files = file.listFiles(File::isDirectory);
        if (files == null) {
            return Collections.emptyList();
        } else {
            List<File> subDirs = Arrays.stream(files)
                    .filter(f -> !f.getName().contains(GIT_FOLDER_NAME))
                    .collect(Collectors.toList());

            List<File> deepSubDirs = new ArrayList<>();
            for(File subDir: subDirs) {
                deepSubDirs.addAll(getSubDirs(subDir));
            }
            subDirs.addAll(deepSubDirs);
            return subDirs;
        }
    }
}
