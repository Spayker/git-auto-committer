package com.spayker.ac.console.task.factory;

import java.io.File;

public final class GitProcessFactory {

    private GitProcessFactory(){}

    public static ProcessBuilder createProcessBuilder(File directory, String gitAppPath, String params) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(gitAppPath, params);
        processBuilder.directory(directory);
        return processBuilder;
    }


}
