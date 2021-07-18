package com.spayker.ac.task;

import com.spayker.ac.model.git.GitData;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.DiffCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.lib.UserConfig;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.spayker.ac.model.git.CHANGE.*;
import static com.spayker.ac.model.git.CHANGE.REMOVED;

@Slf4j
public class ChangeProcessor implements Runnable {


    private static final String GIT_REMOTE_TYPE = "origin";

    private String projectsPath;
    private String accessToken;
    private final String GIT_FOLDER_NAME = ".git";

    private ChangeProcessor() { }

    public ChangeProcessor(String projectsPath, String accessToken) {
        this.projectsPath = projectsPath;
        this.accessToken = accessToken;
    }

    @Override
    public void run() {
        File directory = new File(projectsPath);
        List<File> subFolders = getSubDirs(directory);

        List<File> filteredGitFolders = subFolders.stream()
                .filter(f -> containGitFolder(f.getPath()))
                .collect(Collectors.toList());
        Map<GitData, Map<String, String>> projectDifferences = collectProjectFolders(filteredGitFolders);
        projectDifferences.forEach(this::processFoundChanges);
    }

    private List<File> getSubDirs(File file) {
        List<File> subDirs = Arrays.stream(Objects.requireNonNull(file.listFiles(File::isDirectory)))
                .filter(f -> !f.getName().contains(GIT_FOLDER_NAME))
                .collect(Collectors.toList());

        List<File> deepSubDirs = new ArrayList<>();
        for(File subDir: subDirs) {
            deepSubDirs.addAll(getSubDirs(subDir));
        }
        subDirs.addAll(deepSubDirs);
        return subDirs;
    }

    private boolean containGitFolder(String absolutePath) {
        File directory = new File(absolutePath);
        File[] sameLevelFolders = directory.listFiles(File::isDirectory);
        return Arrays.stream(Objects.requireNonNull(sameLevelFolders))
                .anyMatch(f -> f.getName().equalsIgnoreCase(GIT_FOLDER_NAME));
    }

    private void processFoundChanges(GitData gitData, Map<String, String> changes) {
        Git git = gitData.getGit();
        StoredConfig config = git.getRepository().getConfig();
        String author = config.get(UserConfig.KEY).getAuthorName();
        String email = config.get(UserConfig.KEY).getAuthorEmail();
        CredentialsProvider cp = new UsernamePasswordCredentialsProvider(email, accessToken);
        try {
            for (String changeType : changes.keySet()) {
                String fileName = changes.get(changeType);
                git.add().addFilepattern(fileName).call();
                git.commit().setAuthor(author, email).setMessage(changeType + " " + fileName).call();
                log.info("COMMITTED into " + gitData.getFolderName() + " project");
            }

            git.push().setCredentialsProvider(cp).setRemote(GIT_REMOTE_TYPE).call();
            log.info("PUSHED into " + gitData.getFolderName() + " project, changes: "  + changes.size());
        } catch (GitAPIException e) {
            log.error(e.getMessage());
        }
    }

    private Map<GitData, Map<String, String>> collectProjectFolders(List<File> projectFolders) {
        Map<GitData, Map<String, String>> projectDifferences = new HashMap<>();

        projectFolders.forEach(folder -> {
            try {
                Git git = Git.open( folder );
                Status status = git.status().call();
                boolean hasNoChange = hasChanges(status);

                if(hasNoChange){
                    log.info("Project has no changes in: " + folder);
                    GitData gitData = new GitData(git, folder.getName());
                    projectDifferences.put(gitData, Collections.emptyMap());
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

    private boolean hasChanges(Status status) {
        return status.getAdded().isEmpty() && status.getChanged().isEmpty() && status.getConflicting().isEmpty()
                && status.getConflictingStageState().isEmpty() && status.getIgnoredNotInIndex().isEmpty()
                && status.getMissing().isEmpty() && status.getModified().isEmpty() && status.getRemoved().isEmpty();
    }
}
