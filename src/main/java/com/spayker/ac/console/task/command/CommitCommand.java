package com.spayker.ac.console.task.command;

import com.spayker.ac.console.model.GitOutputData;
import com.spayker.ac.console.model.COMMAND;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.util.List;
import java.util.Map;

import static com.spayker.ac.console.model.COMMAND.COMMIT;

@Slf4j
@AllArgsConstructor
public class CommitCommand extends Command implements GitRunnable {

    private Map<COMMAND, List<String>> commandListMap;

    private static final String GIT_COMMIT_OPTION = "-m";
    private static final char QUOTE_CHARACTER = '"';

    private CommitCommand(){
        this.output = new StringBuilder();
    }

    public static CommitCommand CreateCommitCommand(Map<COMMAND, List<String>> commandListMap){
        return new CommitCommand(commandListMap);
    }

    @Override
    public GitOutputData runCommand(File projectFolder, String gitPath) {
        List<String> changes = commandListMap.get(COMMIT);
        ProcessBuilder processBuilder = GitProcessFactory.createProcessBuilder(projectFolder, gitPath, COMMIT.getValue(), GIT_COMMIT_OPTION, getParam(changes));
        BufferedReader reader = runOuterProcess(processBuilder);
        reader.lines().forEach(line -> output.append(line));
        return new GitOutputData(output.toString(), null);
    }

    private String getParam(List<String> changes){
        StringBuilder gitParams = new StringBuilder();
        gitParams.append(QUOTE_CHARACTER);
        changes.forEach(gitParams::append);
        gitParams.append(QUOTE_CHARACTER);
        log.info("Formed commit command: git " + gitParams);
        return gitParams.toString();
    }
}
