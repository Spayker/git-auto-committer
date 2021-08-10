package com.spayker.ac.console.task;

import com.spayker.ac.console.model.git.CHANGE;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.spayker.ac.console.model.git.COMMAND.STATUS;

@Slf4j
public class ChangeProcessorTask implements Runnable {

    private static final String GIT_REMOTE_TYPE = "origin";
    private static final String GIT_FOLDER_NAME = ".git";
    // toDo: set git path read from config file
    private static final String GIT_APP_PATH = "C:\\Program Files\\Git\\cmd\\git.exe";

    private String projectsPath;
    private String accessToken;

    private ChangeProcessorTask() { }

    public ChangeProcessorTask(String projectsPath, String accessToken) {
        this.projectsPath = projectsPath;
        this.accessToken = accessToken;
    }

    @Override
    public void run() {
        File directory = new File(projectsPath);
        List<File> subFolders = getSubDirs(directory);

        List<File> filteredGitFolders = subFolders.stream()
                .filter(f -> containGitFolder(f.getPath()))
                .collect(Collectors.toList());

        if (filteredGitFolders.isEmpty()){
            log.warn("No git projects found by provided path: " + projectsPath);
        } else {
            Map<CHANGE, Map<String, List<String>>> projectDifferences = collectProjectFolders(filteredGitFolders);
            processGitFolders(projectDifferences);
        }
    }

    private void processGitFolders(Map<CHANGE, Map<String, List<String>>> projectDifferences) {

    }

    private List<File> getSubDirs(File file) {
        List<File> subDirs = Arrays.stream(Objects.requireNonNull(file.listFiles(File::isDirectory)))
                .filter(f -> !f.getName().contains(GIT_FOLDER_NAME))
                .collect(Collectors.toList());

        List<File> deepSubDirs = new ArrayList<>();
        for(File subDir: subDirs) {
            deepSubDirs.addAll(getSubDirs(subDir));
        }
        subDirs.addAll(deepSubDirs);
        return subDirs;
    }

    private boolean containGitFolder(String absolutePath) {
        File directory = new File(absolutePath);
        File[] sameLevelFolders = directory.listFiles(File::isDirectory);
        return Arrays.stream(Objects.requireNonNull(sameLevelFolders))
                .anyMatch(f -> f.getName().equalsIgnoreCase(GIT_FOLDER_NAME));
    }

    private Map<CHANGE, Map<String, List<String>>> collectProjectFolders(List<File> projectFolders) {
        Map<CHANGE, Map<String, List<String>>> projectDifferences = new HashMap<>();
        projectFolders.forEach(folder -> processGitStatus(projectDifferences, folder));
        return projectDifferences;
    }

    private void processGitStatus(Map<CHANGE, Map<String, List<String>>> projectDifferences, File folder) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(GIT_APP_PATH, STATUS.getValue());
        processBuilder.directory(folder);

        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));


            StringBuilder outputContent = new StringBuilder();
            reader.lines().forEach(outputContent::append);
            int exitCode = process.waitFor();
            log.debug("Exited with error code: " + exitCode);




        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage());
        }
    }
}
