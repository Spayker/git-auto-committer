package com.spayker.ac.jgit.task;

import com.spayker.ac.jgit.model.git.GitData;
import com.spayker.ac.util.file.GitFolderRecognizer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.GitCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.lib.UserConfig;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.spayker.ac.jgit.model.git.CHANGE.*;

@Slf4j
@AllArgsConstructor
public class ChangeProcessorTask implements Runnable {

    private static final String GIT_REMOTE_TYPE = "origin";
    private static final String GIT_FOLDER_NAME = ".git";
    private static final String SPACE_CHARACTER = " ";

    private GitFolderRecognizer gitFolderRecognizer;
    private String projectsPath;
    private String accessToken;

    @Override
    public void run() {
        List<File> filteredGitFolders = gitFolderRecognizer.getGitFolders(projectsPath);
        Map<GitData, Map<String, List<String>>> projectDifferences = collectProjectFolders(filteredGitFolders);
        projectDifferences.forEach(this::processFoundChanges);
    }

    private void processFoundChanges(GitData gitData, Map<String, List<String>> changes) {
        Git git = gitData.getGit();
        StoredConfig config = git.getRepository().getConfig();
        String author = config.get(UserConfig.KEY).getAuthorName();
        String email = config.get(UserConfig.KEY).getAuthorEmail();
        CredentialsProvider cp = new UsernamePasswordCredentialsProvider(email, accessToken);
        int changesAmount = 0;
        for (List<String> listChange : changes.values()) {
            changesAmount = changesAmount + listChange.size();
        }

        if (changesAmount > 0) {
            StringBuilder commitMessage = new StringBuilder();
            for (String changeType : changes.keySet()) {
                List<String> fileNames = changes.get(changeType);
                fileNames.forEach(f -> {
                    commitMessage.append(changeType)
                        .append(SPACE_CHARACTER)
                        .append(Paths.get(f).getFileName())
                        .append(System.lineSeparator());
                    executeGitCommand(git.add().addFilepattern(f));
                });
            }
            log.info("Found changes at [" + gitData.getFolderName() + "] project");
            log.info("Next changes have been committed: " + System.lineSeparator() + commitMessage);
            executeGitCommand(git.commit().setAuthor(author, email).setMessage(commitMessage.toString()));
        }
        executeGitCommand(git.push().setCredentialsProvider(cp).setRemote(GIT_REMOTE_TYPE));
        log.info("Pushed into " + gitData.getFolderName() + " project, changes: "  + changesAmount);
    }

    private void executeGitCommand(final GitCommand<?> commitCommand){
        try {
            commitCommand.call();
        } catch (GitAPIException e) {
            log.error(e.getMessage());
        }
    }

    private Map<GitData, Map<String, List<String>>> collectProjectFolders(List<File> projectFolders) {
        Map<GitData, Map<String, List<String>>> projectDifferences = new HashMap<>();

        projectFolders.forEach(folder -> {
            try {
                Git git = Git.open( folder );
                Status status = git.status().call();
                boolean hasNoChange = hasChanges(status);

                if(hasNoChange){
                    log.info("Project has no changes in: " + folder);
                } else {
                    Map<String, List<String>> changes = getChanges(status);
                    GitData gitData = new GitData(git, folder.getName());
                    projectDifferences.put(gitData, changes);
                }
            } catch (IOException | GitAPIException e) {
                log.warn(e.getMessage());
            }
        });
        return projectDifferences;
    }

    private Map<String, List<String>> getChanges(Status status){
        Map<String, List<String>> changes = new HashMap<>();
        ArrayList<String> added = new ArrayList<>(status.getAdded());
        if(added.size() > 0) {
            changes.put(ADDED.getValue(), added);
        }

        ArrayList<String> changed = new ArrayList<>(status.getChanged());
        if(changed.size() > 0) {
            changes.put(CHANGED.getValue(), changed);
        }

        ArrayList<String> missed = new ArrayList<>(status.getMissing());
        if(missed.size() > 0) {
            changes.put(MISSING.getValue(), missed);
        }

        ArrayList<String> modified = new ArrayList<>(status.getModified());
        if(modified.size() > 0) {
            changes.put(MODIFIED.getValue(), modified);
        }

        ArrayList<String> removed = new ArrayList<>(status.getRemoved());
        if(removed.size() > 0) {
            changes.put(REMOVED.getValue(), removed);
        }

        ArrayList<String> untracked = new ArrayList<>(status.getUntracked());
        if(untracked.size() > 0) {
            changes.put(UNTRACKED.getValue(), untracked);
        }
        return changes;
    }

    private boolean hasChanges(Status status) {
        return status.getAdded().isEmpty() && status.getChanged().isEmpty() && status.getUntracked().isEmpty()
                && status.getMissing().isEmpty() && status.getModified().isEmpty() && status.getRemoved().isEmpty();
    }
}
