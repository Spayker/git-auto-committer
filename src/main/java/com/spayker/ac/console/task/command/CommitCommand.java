package com.spayker.ac.console.task.command;

import com.spayker.ac.console.model.GitOutputData;
import com.spayker.ac.console.model.COMMAND;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.spayker.ac.console.model.COMMAND.ADD;
import static com.spayker.ac.console.model.COMMAND.COMMIT;

@Slf4j
@AllArgsConstructor
public class CommitCommand extends Command implements GitRunnable {

    private Map<COMMAND, List<String>> commandListMap;

    private static final String GIT_COMMIT_OPTION = "-m";
    private static final char GIT_NEW_LINE_CHAR = '\n';
    private static final char QUOTE_CHARACTER = '"';

    private CommitCommand(){
        this.output = new StringBuilder();
    }

    public static CommitCommand CreateCommitCommand(Map<COMMAND, List<String>> commandListMap){
        return new CommitCommand(commandListMap);
    }

    @Override
    public GitOutputData runCommand(File projectFolder, String gitPath) {
        List<String> changes = Stream.of(commandListMap.getOrDefault(ADD, Collections.emptyList()),
                commandListMap.getOrDefault(COMMIT, Collections.emptyList()))
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());

        ProcessBuilder processBuilder = GitProcessFactory.createProcessBuilder(projectFolder, gitPath, COMMIT.getValue(), GIT_COMMIT_OPTION, getParam(changes));
        BufferedReader reader = runOuterProcess(processBuilder);
        reader.lines().forEach(line -> output.append(line));
        return new GitOutputData(output.toString(), null);
    }

    private String getParam(List<String> changes){
        StringBuilder gitParams = new StringBuilder();
        gitParams.append(QUOTE_CHARACTER);
        changes.forEach(change -> gitParams.append(change).append(GIT_NEW_LINE_CHAR));
        gitParams.append(QUOTE_CHARACTER);
        log.info("Formed commit command: git " + gitParams);
        return gitParams.toString();
    }
}
