package com.spayker.ac.console.task;

import com.spayker.ac.console.model.git.COMMAND;
import com.spayker.ac.console.task.factory.GitProcessFactory;
import com.spayker.ac.util.file.GitFolderRecognizer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.spayker.ac.console.model.git.COMMAND.STATUS;

@Slf4j
@AllArgsConstructor
public class ChangeProcessorTask implements Runnable {

    private static final String GIT_REMOTE_TYPE = "origin";

    // toDo: set git path read from config file
    private static final String GIT_APP_PATH = "C:\\Program Files\\Git\\cmd\\git.exe";

    private GitFolderRecognizer gitFolderRecognizer;
    private String projectsPath;
    private String accessToken;

    @Override
    public void run() {
        List<File> filteredGitFolders = gitFolderRecognizer.getGitFolders(projectsPath);
        if (filteredGitFolders.isEmpty()) {
            log.warn("No git projects found by provided path: " + projectsPath);
        } else {
            Map<String, Map <COMMAND, List<String>>> projectDifferences = collectProjectFolders(filteredGitFolders);
            projectDifferences.forEach(this::performRemoteRepoUpdate);
        }
    }

    private void performRemoteRepoUpdate(String projectPath, Map<COMMAND, List<String>> commandListMap) {
        log.debug("Performing git repo update for: " + projectPath);
        COMMAND commandType = STATUS;
        StringBuilder commitMessage = new StringBuilder();
        for (COMMAND command : commandListMap.keySet()){
            commandType = command;
            List<String> changes = commandListMap.get(command);
            changes.forEach(commitMessage::append);
        }

        switch (commandType) {
            case ADD:
            case COMMIT: {
                
                break;
            }
            case PUSH: {

            }
        }
    }

    Map<String, Map <COMMAND, List<String>>> collectProjectFolders(List<File> projectFolders) {
        Map<String, Map <COMMAND, List<String>>> projectDifferences = new HashMap<>();
        projectFolders.forEach(folder -> processGitStatus(folder, projectDifferences));
        return projectDifferences;
    }

    void processGitStatus(File folder, Map<String, Map <COMMAND, List<String>>> projectDifferences) {
        ProcessBuilder processBuilder = GitProcessFactory.createProcessBuilder(folder, GIT_APP_PATH, STATUS.getValue());
        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            StringBuilder outputContent = new StringBuilder();
            List<String> changes = new ArrayList<>();
            reader.lines().forEach(line ->  {
                outputContent.append(line);
                collectChanges(changes, line);
            });

            int exitCode = process.waitFor();
            log.debug("Exited with error code: " + exitCode);

            if(!changes.isEmpty()) {
                COMMAND preferableGitCommand = getCommandByGitStatus(outputContent.toString());
                if(!preferableGitCommand.equals(STATUS)){
                    Map <COMMAND, List<String>> projectChangeContent = Collections.singletonMap(preferableGitCommand, changes);
                    projectDifferences.put(folder.getPath(), projectChangeContent);
                }
            }
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage());
        }
    }

    void collectChanges(List<String> changes, String outputStatusRow) {
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

    COMMAND getCommandByGitStatus(String gitStatusOutput) {
        if (gitStatusOutput.contains("Changes not staged for commit")){
            return COMMAND.ADD;
        }
        if (gitStatusOutput.contains("branch is ahead")){
            return COMMAND.PUSH;
        }
        return STATUS;
    }
}
