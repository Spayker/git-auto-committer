package com.spayker.ac.console.task.command;

import java.io.File;

public final class GitProcessFactory {

    private GitProcessFactory(){}

    public static ProcessBuilder createProcessBuilder(File directory, String... params) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(params);
        processBuilder.directory(directory);
        return processBuilder;
    }

}
