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

    private static final String REV_PARSE_OPTION = "rev-parse";
    private static final String ABBREV_REF_OPTION = "--abbrev-ref";
    private static final String HEAD_OPTION = "HEAD";

    private BranchCommand(){
        this.output = new StringBuilder();
    }

    public static BranchCommand CreateBranchCommand(Map<COMMAND, List<String>> commandListMap){
        return new BranchCommand(commandListMap);
    }

    @Override
    public GitOutputData runCommand(File projectFolder, String gitPath) {
        ProcessBuilder processBuilder = GitProcessFactory.createProcessBuilder(projectFolder, gitPath, REV_PARSE_OPTION, ABBREV_REF_OPTION, HEAD_OPTION);
        BufferedReader reader = runOuterProcess(processBuilder);
        reader.lines().forEach(line -> output.append(line));
        return new GitOutputData(output.toString(), null);
    }

}
