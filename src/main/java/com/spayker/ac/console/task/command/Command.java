package com.spayker.ac.console.task.command;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import static com.spayker.ac.console.model.OUTPUT_MARKER.DELETED;
import static com.spayker.ac.console.model.OUTPUT_MARKER.MODIFIED;
import static com.spayker.ac.console.model.OUTPUT_MARKER.NEW;

@Slf4j
abstract class Command {

    @Getter
    StringBuilder output = new StringBuilder();

    static final String SPACE = " ";

    BufferedReader runOuterProcess(ProcessBuilder processBuilder){
        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            process.waitFor();
            return reader;
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    void collectChanges(List<String> changes, String outputStatusRow) {
        if(outputStatusRow.contains(MODIFIED.getValue())) {
            changes.add(outputStatusRow);
        }
        if(outputStatusRow.contains(NEW.getValue())) {
            changes.add(outputStatusRow);
        }
        if(outputStatusRow.contains(DELETED.getValue())) {
            changes.add(outputStatusRow);
        }
    }

}
