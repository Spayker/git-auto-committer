package com.spayker.ac.task;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.lib.UserConfig;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.spayker.ac.model.git.CHANGE.*;
import static com.spayker.ac.model.git.CHANGE.REMOVED;

@Slf4j
public class ChangeProcessor implements Runnable {


    private static final String GIT_REMOTE_TYPE = "origin";

    private String projectsPath;
    private String privateAccessToken;

    private ChangeProcessor() { }

    public ChangeProcessor(String projectsPath, String privateAccessToken) {
        this.projectsPath = projectsPath;
        this.privateAccessToken = privateAccessToken;
    }

    @Override
    public void run() {
        File directory = new File(projectsPath);
        File[] projectFolders = directory.listFiles(File::isDirectory);

        Map<Git, Map<String, String>> projectDifferences = collectProjectFolders(projectFolders);
        projectDifferences.forEach(this::processFoundChanges);
    }

    private void processFoundChanges(Git git, Map<String, String> changes) {
        changes.forEach((type, fileName) -> {
            log.info(" type: " + type + " fileName: "+ fileName);

            StoredConfig config = git.getRepository().getConfig();

            String author = config.get( UserConfig.KEY ).getAuthorName();
            String email = config.get( UserConfig.KEY ).getAuthorEmail();

            try {
                git.add().addFilepattern(fileName).call();
                git.commit().setAuthor(author, email).setMessage(type + " " + fileName).call();

                CredentialsProvider cp = new UsernamePasswordCredentialsProvider(email, privateAccessToken);
                git.push().setCredentialsProvider(cp).setRemote(GIT_REMOTE_TYPE).call();
            } catch (GitAPIException e) {
                log.warn(e.getMessage());
            }
        });
    }

    private Map<Git, Map<String, String>> collectProjectFolders(File[] projectFolders) {
        Map<Git, Map<String, String>> projectDifferences = new HashMap<>();

        Arrays.stream(projectFolders).forEach(folder -> {
            try {
                Git git = Git.open( folder );
                Status status = git.status().call();
                boolean hasNoChange = withoutChanges(status);

                if(hasNoChange){
                    log.info("Project has no changes in: " + folder);
                } else {
                    Map<String, String> changes = getChanges(status);
                    changes.forEach((name, path) -> System.out.println(name.toUpperCase() + ": " + path));
                    projectDifferences.put(git, changes);
                }
            } catch (IOException | GitAPIException e) {
                log.warn(e.getMessage());
            }
        });
        return projectDifferences;
    }

    private Map<String, String> getChanges(Status status){
        Map<String, String> changes = new HashMap<>();
        status.getAdded().forEach(a -> changes.put(ADDED.getValue(), a));
        status.getChanged().forEach(a -> changes.put(CHANGED.getValue(), a));
        status.getConflicting().forEach(a -> changes.put(CONFLICTING.getValue(), a));
        status.getConflictingStageState().forEach((path, stageState) -> changes.put(CONFLICTING_STAGE_STATE.getValue(), path));
        status.getMissing().forEach(a -> changes.put(MISSING.getValue(), a));
        status.getModified().forEach(a -> changes.put(MODIFIED.getValue(), a));
        status.getRemoved().forEach(a -> changes.put(REMOVED.getValue(), a));
        return changes;
    }

    private boolean withoutChanges(Status status) {
        return status.getAdded().isEmpty() && status.getChanged().isEmpty() && status.getConflicting().isEmpty()
                && status.getConflictingStageState().isEmpty() && status.getIgnoredNotInIndex().isEmpty()
                && status.getMissing().isEmpty() && status.getModified().isEmpty() && status.getRemoved().isEmpty();
    }


}
