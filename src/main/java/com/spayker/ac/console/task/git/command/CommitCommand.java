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
import java.util.List;
import java.util.Map;

import static com.spayker.ac.console.model.git.COMMAND.COMMIT;

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
    public GitOutput runCommand(File projectFolder, String gitPath) {
        List<String> changes = commandListMap.get(COMMIT);
        ProcessBuilder processBuilder = GitProcessFactory.createProcessBuilder(projectFolder, gitPath, getParam(changes));

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

    private String getParam(List<String> changes){
        StringBuilder gitParams = new StringBuilder(SPACE);
        gitParams.append(COMMIT.getValue()).append(SPACE).append(GIT_COMMIT_OPTION).append(SPACE).append(QUOTE_CHARACTER);
        changes.forEach(gitParams::append);
        gitParams.append(QUOTE_CHARACTER);
        log.debug("Formed commit command: " + gitParams);
        return gitParams.toString();
    }
}
