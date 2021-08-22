package com.spayker.ac.console.task.command;

import com.spayker.ac.console.model.GitOutputData;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;

import static com.spayker.ac.console.model.COMMAND.ADD;

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
    public GitOutputData runCommand(File projectFolder, String gitPath) {
        ProcessBuilder processBuilder = GitProcessFactory.createProcessBuilder(projectFolder, gitPath, ADD.getValue(), GIT_ADD_ALL_OPTION);
        BufferedReader reader = runOuterProcess(processBuilder);
        reader.lines().forEach(line -> output.append(line));
        return new GitOutputData(output.toString(), null);
    }
}
