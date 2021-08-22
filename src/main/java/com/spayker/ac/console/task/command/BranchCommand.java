package com.spayker.ac.console.task.command;

import com.spayker.ac.console.model.GitOutputData;
import com.spayker.ac.console.model.COMMAND;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.util.List;
import java.util.Map;

import static com.spayker.ac.console.model.COMMAND.BRANCH;

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
    public GitOutputData runCommand(File projectFolder, String gitPath) {
        ProcessBuilder processBuilder = GitProcessFactory.createProcessBuilder(projectFolder, gitPath, getParam());
        BufferedReader reader = runOuterProcess(processBuilder);
        reader.lines().forEach(line -> output.append(line));
        return new GitOutputData(output.toString(), null);
    }

    private String getParam() {
        StringBuilder gitParams = new StringBuilder();
        gitParams.append(BRANCH.getValue());
        log.info("Formed branch command: git " + gitParams);
        return gitParams.toString();
    }

}
