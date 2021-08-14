package com.spayker.ac.console.task;

import com.spayker.ac.console.model.git.COMMAND;
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
            Map<String, List<String>> projectDifferences = collectProjectFolders(filteredGitFolders);
            //processGitFolders(projectDifferences);
        }
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

    private Map<String, List<String>> collectProjectFolders(List<File> projectFolders) {
        Map<String, Map <COMMAND, List<String>>> projectDifferences = new HashMap<>();
        projectFolders.forEach(f -> processGitStatus(f, projectDifferences));
        return null;
    }

    private void processGitStatus(File folder, Map<String, Map <COMMAND, List<String>>> projectDifferences) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(GIT_APP_PATH, STATUS.getValue());
        processBuilder.directory(folder);

        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            final StringBuilder outputContent = new StringBuilder();
            final List<String> changes = new ArrayList<>();
            reader.lines().forEach(line -> {
                collectChanges(changes, line);
            });

            int exitCode = process.waitFor();
            log.debug("Exited with error code: " + exitCode);

            COMMAND preferableGitCommand = getCommandByGitStatus(outputContent.toString());
            Map <COMMAND, List<String>> projectChangeContent = new HashMap<>();
            projectChangeContent.put(preferableGitCommand, changes);

            projectDifferences.put(folder.getPath(), projectChangeContent);
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage());
        }
    }

    private void collectChanges(List<String> changes, String outputStatusRow) {
        if(outputStatusRow.contains("modified:")) {
            changes.add(outputStatusRow);
        }

        if(outputStatusRow.contains("New ")) {
            changes.add(outputStatusRow);
        }

        if(outputStatusRow.contains("deleted:")) {
            changes.add(outputStatusRow);
        }

    }

    private COMMAND getCommandByGitStatus(String gitStatusOutput) {
        if (gitStatusOutput.contains("Changes not staged for commit")){
            return COMMAND.ADD;
        }
        if (gitStatusOutput.contains("branch is ahead")){
            return COMMAND.PUSH;
        }
        return STATUS;
    }
}
