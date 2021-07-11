package com.spayker.ac;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.UserConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.spayker.ac.model.git.CHANGE.*;

public class Main {

    private static final String CURRENT_APP_RUNNING_DIR = System.getProperty("user.dir");
    private static final String GIT_REMOTE_TYPE = "origin";

    // get user login & password
    // get folder path with with git projects

    // run git add, git commit, git push commands

    public static void main(String[] args) {
        // get user login password

        // get folder projects
        System.out.println("Working Directory = " + CURRENT_APP_RUNNING_DIR);
        File directory = new File("E:\\projects\\arma3\\");
        File[] projectFolders = directory.listFiles(File::isDirectory);

        Map<Git, Map<String, String>> projectDifferences = collectProjectFolders(projectFolders);
        projectDifferences.forEach(Main::processFoundChanges);
    }

    private static void processFoundChanges(Git git, Map<String, String> changes) {
        changes.forEach((type, fileName) -> {
            System.out.println(git.toString() + " type: " + type + " fileName: "+ fileName);
            String author = git.getRepository().getConfig().get( UserConfig.KEY ).getAuthorName();
            String email = git.getRepository().getConfig().get( UserConfig.KEY ).getAuthorEmail();

            try {
                git.add().addFilepattern(fileName).call();
                git.commit().setAuthor(author, email).setMessage(type + " " + fileName).call();

                CredentialsProvider cp = new UsernamePasswordCredentialsProvider(email, "1_2Qazxsw");
                git.push().setCredentialsProvider(cp).setRemote(GIT_REMOTE_TYPE).call();
            } catch (GitAPIException e) {
                e.printStackTrace();
            }
        });
    }

    private static Map<Git, Map<String, String>> collectProjectFolders(File[] projectFolders) {
        Map<Git, Map<String, String>> projectDifferences = new HashMap<>();

        Arrays.stream(projectFolders).forEach(folder -> {
            try {
                Git git = Git.open( folder );
                Status status = git.status().call();
                boolean hasNoChange = withoutChanges(status);

                if(hasNoChange){
                    System.out.println("Project has no changes in: " + folder);
                } else {
                    Map<String, String> changes = getChanges(status);
                    changes.forEach((name, path) -> System.out.println(name.toUpperCase() + ": " + path));
                    projectDifferences.put(git, changes);
                }
            } catch (IOException | GitAPIException e) {
                e.printStackTrace();
            }
        });
        return projectDifferences;
    }

    private static Map<String, String> getChanges(Status status){
        Map<String, String> changes = new HashMap<>();
        status.getAdded().forEach(a -> changes.put(ADDED.getValue(), a));
        status.getChanged().forEach(a -> changes.put(CHANGED.getValue(), a));
        status.getConflicting().forEach(a -> changes.put(CONFLICTING.getValue(), a));
        status.getConflictingStageState().forEach((path, stageState) -> changes.put(CONFLICTING_STAGE_STATE.getValue(), path));
        status.getIgnoredNotInIndex().forEach(a -> changes.put(IGNORED_NOT_IN_INDEX.getValue(), a));
        status.getMissing().forEach(a -> changes.put(MISSING.getValue(), a));
        status.getModified().forEach(a -> changes.put(MODIFIED.getValue(), a));
        status.getRemoved().forEach(a -> changes.put(REMOVED.getValue(), a));
        return changes;
    }
    
    private static boolean withoutChanges(Status status) {
        return status.getAdded().isEmpty() && status.getChanged().isEmpty() && status.getConflicting().isEmpty() 
            && status.getConflictingStageState().isEmpty() && status.getIgnoredNotInIndex().isEmpty()
                && status.getMissing().isEmpty() && status.getModified().isEmpty() && status.getRemoved().isEmpty();
    }

}