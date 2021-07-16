package com.spayker.ac.task;

import com.spayker.ac.model.git.GitData;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.lib.UserConfig;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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
        List<File> subFolders = getSubdirs(directory);

        List<File> filteredGitFolders = subFolders.stream()
                .filter(f -> containGitFolder(f.getPath()))
                .collect(Collectors.toList());
        Map<GitData, Map<String, String>> projectDifferences = collectProjectFolders(filteredGitFolders);
        projectDifferences.forEach(this::processFoundChanges);
    }

    private List<File> getSubdirs(File file) {
        List<File> subdirs = Arrays.stream(Objects.requireNonNull(file.listFiles(File::isDirectory)))
                .filter(f -> !f.getName().contains(".git"))
                .collect(Collectors.toList());
        subdirs = new ArrayList<>(subdirs);

        List<File> deepSubdirs = new ArrayList<>();
        for(File subdir : subdirs) {
            deepSubdirs.addAll(getSubdirs(subdir));
        }
        subdirs.addAll(deepSubdirs);
        return subdirs;
    }

    private boolean containGitFolder(String absolutePath) {
        File directory = new File(absolutePath);
        File[] sameLevelFolders = directory.listFiles(File::isDirectory);
        return Arrays.stream(Objects.requireNonNull(sameLevelFolders))
                .anyMatch(f -> f.getName().equalsIgnoreCase(".git"));
    }

    private void processFoundChanges(GitData gitData, Map<String, String> changes) {

        if(changes.size() > 0) {
            Git git = gitData.getGit();
            StoredConfig config = git.getRepository().getConfig();
            String author = config.get(UserConfig.KEY).getAuthorName();
            String email = config.get(UserConfig.KEY).getAuthorEmail();
            CredentialsProvider cp = new UsernamePasswordCredentialsProvider(email, privateAccessToken);

            changes.forEach((type, fileName) -> {
                try {
                    git.add().addFilepattern(fileName).call();
                    git.commit().setAuthor(author, email).setMessage(type + " " + fileName).call();
                    git.push().setCredentialsProvider(cp).setRemote(GIT_REMOTE_TYPE).call();
                    log.info("PUSHED into " + gitData.getFolderName() + " project");
                } catch (GitAPIException e) {
                    e.printStackTrace();
                    log.error(e.getMessage());
                }
            });
        }
    }

    private Map<GitData, Map<String, String>> collectProjectFolders(List<File> projectFolders) {
        Map<GitData, Map<String, String>> projectDifferences = new HashMap<>();

        projectFolders.forEach(folder -> {
            try {
                Git git = Git.open( folder );
                Status status = git.status().call();
                boolean hasNoChange = withoutChanges(status);

                if(hasNoChange){
                    log.info("Project has no changes in: " + folder);
                } else {
                    Map<String, String> changes = getChanges(status);
                    changes.forEach((name, path) -> log.info(name.toUpperCase() + ": " + path));

                    GitData gitData = new GitData(git, folder.getName());
                    projectDifferences.put(gitData, changes);
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
