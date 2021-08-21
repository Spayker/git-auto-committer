package com.spayker.ac.console.task.git.command;

import com.spayker.ac.console.model.GitOutput;
import com.spayker.ac.console.task.factory.GitProcessFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static com.spayker.ac.console.model.git.COMMAND.ADD;

@Slf4j
public class AddCommand extends Command implements GitRunnable {

    private static final String GIT_ADD_ALL_OPTION = ".";

    private AddCommand(){
        this.output = new StringBuilder();
    }

    public static AddCommand CreateAddCommand(){
        return new AddCommand();
    }

    @Override
    public GitOutput runCommand(File projectFolder, String gitPath) {
        ProcessBuilder processBuilder = GitProcessFactory.createProcessBuilder(projectFolder, gitPath, getParam());
        List<String> changes = new ArrayList<>();

        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            int exitCode = process.waitFor();
            log.debug("Exited with error code: " + exitCode);
            reader.lines().forEach(line -> {
                output.append(line);
                collectChanges(changes, line);
            });
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage());
        }
        return new GitOutput(output.toString(), changes);
    }

    private String getParam(){
        StringBuilder gitParams = new StringBuilder(SPACE);
        gitParams.append(ADD.getValue()).append(SPACE).append(GIT_ADD_ALL_OPTION);
        log.debug("Formed add command: " + gitParams);
        return gitParams.toString();
    }
}
