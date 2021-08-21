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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.spayker.ac.console.model.git.COMMAND.BRANCH;

@Slf4j
@AllArgsConstructor
public class BranchCommand extends Command implements GitRunnable {

    private Map<COMMAND, List<String>> commandListMap;

    private BranchCommand(){
        this.output = new StringBuilder();
    }

    public static BranchCommand CreateBranchCommand(Map<COMMAND, List<String>> commandListMap){
        return new BranchCommand(commandListMap);
    }

    @Override
    public GitOutput runCommand(File projectFolder, String gitPath) {
        ProcessBuilder processBuilder = GitProcessFactory.createProcessBuilder(projectFolder, gitPath, getParam());

        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            int exitCode = process.waitFor();
            log.debug("Exited with error code: " + exitCode);
            reader.lines().forEach(output::append);
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage());
        }
        return new GitOutput(output.toString(), Collections.emptyList());
    }

    private String getParam() {
        StringBuilder gitParams = new StringBuilder(SPACE);
        gitParams.append(BRANCH.getValue());
        log.debug("Formed branch command: " + gitParams);
        return gitParams.toString();
    }

}
