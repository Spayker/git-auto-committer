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
import java.util.Optional;

import static com.spayker.ac.console.model.git.COMMAND.ADD;
import static com.spayker.ac.console.model.git.COMMAND.PUSH;
import static com.spayker.ac.console.model.git.COMMAND.STATUS;

@Slf4j
@AllArgsConstructor
public class ChangeProcessorTask implements Runnable {

    private static final String GIT_REMOTE_TYPE = "origin";

    // toDo: set git path read from config file
    private static final String GIT_APP_PATH = "C:\\Program Files\\Git\\cmd\\git.exe";
    private static final String GIT_ADD_ALL_OPTION = " .";

    private final GitFolderRecognizer gitFolderRecognizer;
    private final String projectsPath;

    @Override
    public void run() {
        List<File> filteredGitFolders = gitFolderRecognizer.getGitFolders(projectsPath);
        if (filteredGitFolders.isEmpty()) {
            log.warn("No git projects found by provided path: " + projectsPath);
        } else {
            Map<File, Map <COMMAND, List<String>>> projectDifferences = collectProjectFolders(filteredGitFolders);
            projectDifferences.forEach(this::performRemoteRepoUpdate);
        }
    }

    private void performRemoteRepoUpdate(File folder, Map<COMMAND, List<String>> commandListMap) {
        log.debug("Performing git repo update for: " + folder.getPath());

        for (COMMAND incomingCommand : commandListMap.keySet()) {
            StringBuilder gitParams = new StringBuilder();
            switch (incomingCommand) {
                case ADD: {
                    gitParams.append(ADD.getValue()).append(GIT_ADD_ALL_OPTION);
                    break;
                }
                case COMMIT: {
                    List<String> changes = commandListMap.get(incomingCommand);
                    changes.forEach(gitParams::append);
                    break;
                }
                case PUSH: {
                    //todo: get current branch name
                    gitParams.append(PUSH.getValue()).append("origin branch_name");
                }
            }
            // todo: finish process build composition
            // ProcessBuilder processBuilder = GitProcessFactory.createProcessBuilder(folder, GIT_APP_PATH, gitParams.toString());
        }
    }

    Map<File, Map <COMMAND, List<String>>> collectProjectFolders(List<File> projectFolders) {
        Map<File, Map <COMMAND, List<String>>> projectDifferences = new HashMap<>();
        projectFolders.forEach(folder -> processGitStatus(folder, projectDifferences));
        return projectDifferences;
    }

    void processGitStatus(File folder, Map<File, Map <COMMAND, List<String>>> projectDifferences) {
        ProcessBuilder processBuilder = GitProcessFactory.createProcessBuilder(folder, GIT_APP_PATH, STATUS.getValue());
        Optional<BufferedReader> gitStatusOutputContent = executeGitCommand(processBuilder);

        if(gitStatusOutputContent.isPresent()){
            BufferedReader gitStatusOutputReader = gitStatusOutputContent.get();
            List<String> changes = new ArrayList<>();
            StringBuilder outputContent = new StringBuilder();
            gitStatusOutputReader.lines().forEach(line ->  {
                outputContent.append(line);
                collectChanges(changes, line);
            });

            if(!changes.isEmpty()) {
                log.info("Found changes at [" + folder.getName() + "] folder");
                COMMAND preferableGitCommand = getCommandByGitStatus(outputContent.toString());
                if(!preferableGitCommand.equals(STATUS)){
                    Map <COMMAND, List<String>> projectChangeContent = Collections.singletonMap(preferableGitCommand, changes);
                    projectDifferences.put(folder, projectChangeContent);
                }
            }
        } else {
            log.warn("No git status output found while [" + folder.getName() + "] processing folder");
        }
    }

    Optional<BufferedReader> executeGitCommand(ProcessBuilder processBuilder) {
        Optional<BufferedReader> outputContent = Optional.empty();
        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            int exitCode = process.waitFor();
            log.debug("Exited with error code: " + exitCode);
            outputContent = Optional.of(reader);
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage());
        }
        return outputContent;
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
