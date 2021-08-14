package com.spayker.ac.util.file;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GitFolderRecognizerTest {

    private GitFolderRecognizer gitFolderRecognizer;
    private static final String USER_HOME_FOLDER = System.getProperty("user.dir");

    @BeforeEach
    public void setup() {
        gitFolderRecognizer = new GitFolderRecognizer();
    }

    private static Stream<Arguments> provideStableFilesToScan() {
        return Stream.of(
            Arguments.of(new File(USER_HOME_FOLDER))
        );
    }

    private static Stream<Arguments> provideUnstableStableFilesToScan() {
        return Stream.of(
            Arguments.of(new File("           ")),
            Arguments.of(new File("////////")),
            Arguments.of(new File("dgdfgdfgdfg"))
        );
    }

    @ParameterizedTest
    @MethodSource("provideStableFilesToScan")
    @DisplayName("Returns list of sub folders by given file object")
    public void shouldReturnSubDirs(File file) {
        // given
        // when
        List<File> subDirs = gitFolderRecognizer.getSubDirs(file);
        // then
        assertNotNull(subDirs);
    }

    @ParameterizedTest
    @MethodSource("provideUnstableStableFilesToScan")
    @DisplayName("Returns no lists of sub folders by given file object")
    public void shouldReturnNoSubDirs(File file) {
        // given
        // when
        List<File> subDirs = gitFolderRecognizer.getSubDirs(file);
        // then
        assertNotNull(subDirs);
        assertTrue(subDirs.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("provideStableFilesToScan")
    @DisplayName("Checks sub folder existing logic")
    public void shouldReturnTrueIfSubFoldersExist(File file) {
        // given
        // when
        boolean gitFolderExist = gitFolderRecognizer.containGitFolder(file.getPath());
        // then
        assertTrue(gitFolderExist);
    }

    @ParameterizedTest
    @MethodSource("provideUnstableStableFilesToScan")
    @DisplayName("Checks sub folder existing logic")
    public void shouldReturnFalseIfSubFoldersExist(File file) {
        // given
        // when
        boolean gitFolderExist = gitFolderRecognizer.containGitFolder(file.getPath());
        // then
        assertFalse(gitFolderExist);
    }

}