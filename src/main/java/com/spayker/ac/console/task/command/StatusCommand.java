package com.spayker.ac.console.task.command;

import com.spayker.ac.console.model.GitOutputData;
import com.spayker.ac.console.model.COMMAND;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.spayker.ac.console.model.COMMAND.STATUS;
import static com.spayker.ac.console.model.OUTPUT_MARKER.BRANCH_AHEAD;
import static com.spayker.ac.console.model.OUTPUT_MARKER.NOT_STAGED;
import static com.spayker.ac.console.model.OUTPUT_MARKER.TO_BE_COMMITTED;

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
    public GitOutputData runCommand(File projectFolder, String gitPath) {
        ProcessBuilder processBuilder = GitProcessFactory.createProcessBuilder(projectFolder, gitPath, STATUS.getValue());

        BufferedReader reader = runOuterProcess(processBuilder);
        List<String> changes = new ArrayList<>();

        reader.lines().forEach(line -> {
            output.append(line);
            collectChanges(changes, line);
        });

        COMMAND preferableGitCommand = getCommandByGitStatus(output.toString());
        if(!preferableGitCommand.equals(STATUS)) {
            log.info("Project [" + projectFolder.getName() + "] contains differences comparing to current project branch");
            Map<COMMAND, List<String>> projectChangeContent = Collections.singletonMap(preferableGitCommand, changes);
            projectDifferences.put(projectFolder, projectChangeContent);
        }

        return new GitOutputData(output.toString(), changes);
    }

    private COMMAND getCommandByGitStatus(String gitStatusOutput) {
        if (gitStatusOutput.contains(NOT_STAGED.getValue())) {
            return COMMAND.ADD;
        }
        if (gitStatusOutput.contains(TO_BE_COMMITTED.getValue())) {
            return COMMAND.COMMIT;
        }
        if (gitStatusOutput.contains(BRANCH_AHEAD.getValue())) {
            return COMMAND.PUSH;
        }
        return STATUS;
    }

}
