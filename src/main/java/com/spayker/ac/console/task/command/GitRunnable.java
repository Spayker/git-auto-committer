package com.spayker.ac.console.task.command;

import com.spayker.ac.console.model.GitOutputData;

import java.io.File;

@FunctionalInterface
public interface GitRunnable {

    GitOutputData runCommand(File projectFolder, String gitPath);

}
