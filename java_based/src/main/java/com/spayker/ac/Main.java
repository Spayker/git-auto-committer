package com.spayker.ac;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Objects;

public class Main {

    private static final String CURRENT_APP_RUNNING_DIR = System.getProperty("user.dir");

    public static void main(String[] args) {

        System.out.println("Working Directory = " + CURRENT_APP_RUNNING_DIR);

        // get folder projects
        File directory = new File(CURRENT_APP_RUNNING_DIR);
        File[] projectFolders = directory.listFiles(File::isDirectory);

        System.out.println("Found project folders:");
        Arrays.stream(Objects.requireNonNull(projectFolders)).forEach(System.out::println);
    }

}