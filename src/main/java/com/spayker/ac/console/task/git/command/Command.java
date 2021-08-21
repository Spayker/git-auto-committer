package com.spayker.ac.console.task.git.command;

import lombok.Getter;

import java.util.List;

import static com.spayker.ac.console.model.git.OUTPUT_MARKER.DELETED;
import static com.spayker.ac.console.model.git.OUTPUT_MARKER.MODIFIED;
import static com.spayker.ac.console.model.git.OUTPUT_MARKER.NEW;

public abstract class Command {

    @Getter
    StringBuilder output;

    static final String SPACE = " ";

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
