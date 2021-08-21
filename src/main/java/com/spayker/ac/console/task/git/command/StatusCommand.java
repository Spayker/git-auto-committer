package com.spayker.ac.console.task.git.command;

import com.spayker.ac.console.model.GitOutput;
import com.spayker.ac.console.model.git.COMMAND;
import com.spayker.ac.console.task.factory.GitProcessFactory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.spayker.ac.console.model.git.COMMAND.STATUS;
import static com.spayker.ac.console.model.git.OUTPUT_MARKER.BRANCH_AHEAD;
import static com.spayker.ac.console.model.git.OUTPUT_MARKER.NOT_STAGED;

@Slf4j
@AllArgsConstructor
public class StatusCommand extends Command implements GitRunnable {

    private Map<File, Map <COMMAND, List<String>>> projectDifferences;

    private StatusCommand(){
        this.output = new StringBuilder();
    }

    public static StatusCommand CreateStatusCommand(Map<File, Map <COMMAND, List<String>>> projectDifferences){
        return new StatusCommand(projectDifferences);
    }

    @Override
    public GitOutput runCommand(File projectFolder, String gitPath) {

        StringBuilder outputContent = new StringBuilder();
        List<String> changes = new ArrayList<>();
        ProcessBuilder processBuilder = GitProcessFactory.createProcessBuilder(projectFolder, gitPath, STATUS.getValue());

        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            int exitCode = process.waitFor();
            log.debug("Exited with error code: " + exitCode);
            reader.lines().forEach(line -> {
                outputContent.append(line);
                collectChanges(changes, line);
            });
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage());
        }

        GitOutput gitOutput = new GitOutput(outputContent.toString(), changes);

        log.info("Found changes at [" + projectFolder.getName() + "] folder");
        COMMAND preferableGitCommand = getCommandByGitStatus(gitOutput.getOutput());
        if(!preferableGitCommand.equals(STATUS)) {
            Map<COMMAND, List<String>> projectChangeContent = Collections.singletonMap(preferableGitCommand, gitOutput.getChanges());
            projectDifferences.put(projectFolder, projectChangeContent);
        }

        return gitOutput;
    }

    private COMMAND getCommandByGitStatus(String gitStatusOutput) {
        if (gitStatusOutput.contains(NOT_STAGED.getValue())) {
            return COMMAND.ADD;
        }
        if (gitStatusOutput.contains(BRANCH_AHEAD.getValue())) {
            return COMMAND.PUSH;
        }
        return STATUS;
    }

}
