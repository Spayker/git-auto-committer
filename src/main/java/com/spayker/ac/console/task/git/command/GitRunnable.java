package com.spayker.ac.console.task.git.command;

import com.spayker.ac.console.model.GitOutput;

import java.io.File;

@FunctionalInterface
public interface GitRunnable {

    GitOutput runCommand(File projectFolder, String gitPath);


}
