package com.spayker.ac.console.task;

import com.spayker.ac.console.model.git.COMMAND;
import com.spayker.ac.util.file.GitFolderRecognizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChangeProcessorTaskTest {

    private ChangeProcessorTask changeProcessorTask;

    @BeforeEach
    public void setup() {
        GitFolderRecognizer gitFolderRecognizer = new GitFolderRecognizer();
        String projectsPath = System.getProperty("user.dir");
        String accessToken = "ghp_ertwefdsgfdfg";

        changeProcessorTask = new ChangeProcessorTask(gitFolderRecognizer, projectsPath, accessToken);
    }

    private static Stream<Arguments> provideGitAddOutput() {
        return Stream.of(
            Arguments.of("Changes not staged for commit")
        );
    }

    private static Stream<Arguments> provideGitPushOutput() {
        return Stream.of(
            Arguments.of("branch is ahead")
        );
    }

    private static Stream<Arguments> provideGitStatusOutput() {
        return Stream.of(
            Arguments.of("dsfgdsfgsdfgsdfg")
        );
    }

    private static Stream<Arguments> provideOutputGitStatusChanges() {
        return Stream.of(
            Arguments.of(new ArrayList<>(), "modified:"),
            Arguments.of(new ArrayList<>(), "New "),
            Arguments.of(new ArrayList<>(), "deleted:")
        );
    }

    @ParameterizedTest
    @MethodSource("provideGitAddOutput")
    @DisplayName("Returns add git command according to received git status output")
    public void shouldReturnGitAddCommandByProvidedGitStatus(String gitStatusOutput) {
        // given
        // when
        COMMAND command = changeProcessorTask.getCommandByGitStatus(gitStatusOutput);

        // then
        assertNotNull(command);
        assertEquals(command, COMMAND.ADD);
    }

    @ParameterizedTest
    @MethodSource("provideGitPushOutput")
    @DisplayName("Returns push git command according to received git status output")
    public void shouldReturnGitPushCommandByProvidedGitStatus(String gitStatusOutput) {
        // given
        // when
        COMMAND command = changeProcessorTask.getCommandByGitStatus(gitStatusOutput);

        // then
        assertNotNull(command);
        assertEquals(command, COMMAND.PUSH);
    }

    @ParameterizedTest
    @MethodSource("provideGitStatusOutput")
    @DisplayName("Returns git status command according to received git status output")
    public void shouldReturnGitStatusCommandByProvidedGitStatus(String gitStatusOutput) {
        // given
        // when
        COMMAND command = changeProcessorTask.getCommandByGitStatus(gitStatusOutput);

        // then
        assertNotNull(command);
        assertEquals(command, COMMAND.STATUS);
    }

    @ParameterizedTest
    @MethodSource("provideOutputGitStatusChanges")
    @DisplayName("Returns git status command according to received git status output")
    public void shouldCollectChanges(List<String> changes, String outputStatusRow) {
        // given
        // when
        changeProcessorTask.collectChanges(changes, outputStatusRow);

        // then
        assertNotNull(changes);
        assertFalse(changes.isEmpty());
        assertEquals(outputStatusRow, changes.get(0));
    }


}