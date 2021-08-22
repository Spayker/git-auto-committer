package com.spayker.ac.console.model;

public enum OUTPUT_MARKER {

    MODIFIED("modified:"),
    NEW("New "),
    DELETED("deleted:"),
    NOT_STAGED("Changes not staged for commit"),
    TO_BE_COMMITTED("Changes to be committed:"),
    BRANCH_AHEAD("branch is ahead");

    private final String value;

    public String getValue() {
        return value;
    }

    OUTPUT_MARKER(String value){
        this.value = value;
    }



}
