package com.spayker.ac;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class Main {

    private static final String CURRENT_APP_RUNNING_DIR = System.getProperty("user.dir");

    // get user login & password
    // get folder path with with git projects

    // run git add, git commit, git push commands

    public static void main(String[] args) {
        // get user login password

        // get folder projects
        System.out.println("Working Directory = " + CURRENT_APP_RUNNING_DIR);
        File directory = new File("E:\\projects\\arma3\\");
        File[] projectFolders = directory.listFiles(File::isDirectory);

        validateProjectFolders(projectFolders);
        // git related API
        // Create a Repository object
        // git.add();

        // Now, we do the commit with a message
        // RevCommit rev = git.commit().setAuthor("spayker", "gildas@example.com").setMessage("My first commit").call();
        // git.push();
    }

    private static void validateProjectFolders(File[] projectFolders) {
        System.out.println("Found project folders:");

        Arrays.stream(projectFolders).forEach(folder -> {
            System.out.println(folder);
            try {
                Git git = Git.open( folder );


                Status status = git.status().call();
                System.out.println("Added: " + status.getAdded());
                System.out.println("Changed: " + status.getChanged());
                System.out.println("Conflicting: " + status.getConflicting());
                System.out.println("ConflictingStageState: " + status.getConflictingStageState());
                System.out.println("IgnoredNotInIndex: " + status.getIgnoredNotInIndex());
                System.out.println("Missing: " + status.getMissing());
                System.out.println("Modified: " + status.getModified());
                System.out.println("Removed: " + status.getRemoved());
                System.out.println("Untracked: " + status.getUntracked());
                System.out.println("UntrackedFolders: " + status.getUntrackedFolders());
            } catch (IOException | GitAPIException e) {
                e.printStackTrace();
            }
        });
    }
}