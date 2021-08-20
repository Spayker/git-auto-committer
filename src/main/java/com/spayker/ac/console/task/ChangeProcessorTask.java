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
import static com.spayker.ac.console.model.git.COMMAND.BRANCH;
import static com.spayker.ac.console.model.git.COMMAND.COMMIT;
import static com.spayker.ac.console.model.git.COMMAND.PUSH;
import static com.spayker.ac.console.model.git.COMMAND.STATUS;

@Slf4j
@AllArgsConstructor
public class ChangeProcessorTask implements Runnable {

    // toDo: set git path read from config file
    private static final String GIT_APP_PATH = "C:\\Program Files\\Git\\cmd\\git.exe";
    private static final String GIT_PUSH_REMOTE_OPTION = "origin";
    private static final String GIT_ADD_ALL_OPTION = ".";
    private static final String GIT_COMMIT_OPTION = "-m";
    private static final String SPACE = " ";

    private final GitFolderRecognizer gitFolderRecognizer;
    private final String projectsPath;

    @Override
    public void run() {
        List<File> filteredGitFolders = gitFolderRecognizer.getGitFolders(projectsPath);
        if (filteredGitFolders.isEmpty()) {
            log.warn("No git projects found by provided path: " + projectsPath);
        } else {
            try {
                Map<File, Map <COMMAND, List<String>>> projectDifferences = collectProjectFolders(filteredGitFolders);
                for (File folder : projectDifferences.keySet()) {
                    Map<COMMAND, List<String>> commandListMap = projectDifferences.get(folder);
                    performRemoteRepoUpdate(folder, commandListMap);
                }
            } catch (IOException | InterruptedException e) {
                log.error(e.getMessage());
            }
        }
    }

    private void performRemoteRepoUpdate(File folder, Map<COMMAND, List<String>> commandListMap) throws IOException, InterruptedException {
        log.debug("Performing git repo update for: " + folder.getPath());

        for (COMMAND incomingCommand : commandListMap.keySet()) {
            StringBuilder gitParams;
            ProcessBuilder processBuilder;

            //todo: form command scenario
            //todo: remove explicit string expressions
            switch (incomingCommand) {
                case ADD: {
                    gitParams = new StringBuilder();
                    gitParams.append(ADD.getValue()).append(SPACE).append(GIT_ADD_ALL_OPTION);
                    processBuilder = GitProcessFactory.createProcessBuilder(folder, GIT_APP_PATH, gitParams.toString());
                    processBuilder.start().waitFor();
                }
                case COMMIT: {
                    gitParams = new StringBuilder();
                    gitParams.append(COMMIT.getValue()).append(SPACE).append(GIT_COMMIT_OPTION).append(SPACE).append('"');
                    List<String> changes = commandListMap.get(incomingCommand);
                    changes.forEach(gitParams::append);
                    gitParams.append('"');
                    processBuilder = GitProcessFactory.createProcessBuilder(folder, GIT_APP_PATH, gitParams.toString());
                    processBuilder.start().waitFor();
                }
                case BRANCH: {
                    gitParams = new StringBuilder();
                    gitParams.append(SPACE).append(BRANCH.getValue());
                    List<String> changes = commandListMap.get(incomingCommand);
                    changes.forEach(gitParams::append);
                    processBuilder = GitProcessFactory.createProcessBuilder(folder, GIT_APP_PATH, gitParams.toString());
                    processBuilder.start().waitFor();
                }
                case PUSH: {
                    gitParams = new StringBuilder();
                    gitParams.append(SPACE).append(PUSH.getValue()).append(SPACE).append(GIT_PUSH_REMOTE_OPTION).append(SPACE);
                    processBuilder = GitProcessFactory.createProcessBuilder(folder, GIT_APP_PATH, gitParams.toString());
                    processBuilder.start().waitFor();
                }
            }
        }
    }

    Map<File, Map <COMMAND, List<String>>> collectProjectFolders(List<File> projectFolders) throws IOException, InterruptedException {
        Map<File, Map <COMMAND, List<String>>> projectDifferences = new HashMap<>();

        for (File folder : projectFolders) {
            processGitStatus(folder, projectDifferences);
        }
        return projectDifferences;
    }

    void processGitStatus(File folder, Map<File, Map <COMMAND, List<String>>> projectDifferences) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = GitProcessFactory.createProcessBuilder(folder, GIT_APP_PATH, STATUS.getValue());
        Optional<BufferedReader> gitStatusOutputContent = executeGitCommand(processBuilder);

        if(gitStatusOutputContent.isPresent()) {
            BufferedReader gitStatusOutputReader = gitStatusOutputContent.get();
            List<String> changes = new ArrayList<>();
            StringBuilder outputContent = new StringBuilder();
            gitStatusOutputReader.lines().forEach(line -> {
                outputContent.append(line);
                collectChanges(changes, line);
            });

            if(changes.isEmpty()) {
                log.info("No git status output found while [" + folder.getName() + "] processing folder");
            } else {
                log.info("Found changes at [" + folder.getName() + "] folder");
                COMMAND preferableGitCommand = getCommandByGitStatus(outputContent.toString());
                if(!preferableGitCommand.equals(STATUS)) {
                    Map <COMMAND, List<String>> projectChangeContent = Collections.singletonMap(preferableGitCommand, changes);
                    projectDifferences.put(folder, projectChangeContent);
                }
            }
        } else {
            log.info("No git status output found while [" + folder.getName() + "] processing folder");
        }
    }

    Optional<BufferedReader> executeGitCommand(ProcessBuilder processBuilder) throws IOException, InterruptedException {
        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        int exitCode = process.waitFor();
        log.debug("Exited with error code: " + exitCode);
        return Optional.of(reader);
    }

    void collectChanges(List<String> changes, String outputStatusRow) {
        //todo: remove explicit string expressions
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
        //todo: remove explicit string expressions
        if (gitStatusOutput.contains("Changes not staged for commit")) {
            return COMMAND.ADD;
        }
        if (gitStatusOutput.contains("branch is ahead")) {
            return COMMAND.PUSH;
        }
        return STATUS;
    }
}
