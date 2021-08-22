package com.spayker.ac.console.task.command;

import com.spayker.ac.console.model.GitOutputData;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;

import static com.spayker.ac.console.model.COMMAND.COMMIT;
import static com.spayker.ac.console.model.COMMAND.PUSH;

@Slf4j
@AllArgsConstructor
public class PushCommand extends Command implements GitRunnable {

    private String branchName;

    private static final String GIT_PUSH_REMOTE_OPTION = "origin";

    private PushCommand(){
        this.output = new StringBuilder();
    }

    public static PushCommand CreatePushCommand(String branchName){
        return new PushCommand(branchName);
    }

    @Override
    public GitOutputData runCommand(File projectFolder, String gitPath) {
        ProcessBuilder processBuilder = GitProcessFactory.createProcessBuilder(projectFolder, gitPath, PUSH.getValue(), GIT_PUSH_REMOTE_OPTION, branchName);
        BufferedReader reader = runOuterProcess(processBuilder);
        reader.lines().forEach(line -> output.append(line));
        return new GitOutputData(output.toString(), null);
    }
}
