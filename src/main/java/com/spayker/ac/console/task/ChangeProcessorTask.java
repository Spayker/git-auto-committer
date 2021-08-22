package com.spayker.ac.console.task;

import com.spayker.ac.console.model.GitOutputData;
import com.spayker.ac.console.model.COMMAND;
import com.spayker.ac.util.file.GitFolderRecognizer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.spayker.ac.console.task.command.AddCommand.CreateAddCommand;
import static com.spayker.ac.console.task.command.BranchCommand.CreateBranchCommand;
import static com.spayker.ac.console.task.command.CommitCommand.CreateCommitCommand;
import static com.spayker.ac.console.task.command.PushCommand.CreatePushCommand;
import static com.spayker.ac.console.task.command.StatusCommand.CreateStatusCommand;

@Slf4j
@AllArgsConstructor
public class ChangeProcessorTask implements Runnable {

    private static final String GIT_APP_PATH = "C:\\Program Files\\Git\\cmd\\git.exe";
    private final GitFolderRecognizer gitFolderRecognizer;
    private final String projectsPath;

    @Override
    public void run() {
        List<File> filteredGitFolders = gitFolderRecognizer.getGitFolders(projectsPath);
        if (filteredGitFolders.isEmpty()) {
            log.warn("No git projects found by provided path: " + projectsPath);
        } else {
            collectProjectFolders(filteredGitFolders).forEach(this::performRemoteRepoUpdate);
        }
    }

    private void performRemoteRepoUpdate(File folder, Map<COMMAND, List<String>> commandListMap) {
        log.debug("Performing git repo update for: " + folder.getPath());

        for (COMMAND incomingCommand : commandListMap.keySet()) {
            String branchName;
            switch (incomingCommand) {
                case ADD: {
                    CreateAddCommand().runCommand(folder, GIT_APP_PATH);
                }
                case COMMIT: {
                    CreateCommitCommand(commandListMap).runCommand(folder, GIT_APP_PATH);
                }
                case BRANCH:
                case PUSH: {
                    GitOutputData output = CreateBranchCommand(commandListMap).runCommand(folder, GIT_APP_PATH);
                    branchName = output.getOutput();
                    //toDo: replace with branchName after it will be updated with a proper branch name
                    CreatePushCommand(branchName).runCommand(folder, GIT_APP_PATH);
                }
            }
        }
    }

    Map<File, Map <COMMAND, List<String>>> collectProjectFolders(List<File> projectFolders) {
        Map<File, Map <COMMAND, List<String>>> projectDifferences = new HashMap<>();
        projectFolders.forEach(folder -> CreateStatusCommand(projectDifferences).runCommand(folder, GIT_APP_PATH));
        return projectDifferences;
    }
}
