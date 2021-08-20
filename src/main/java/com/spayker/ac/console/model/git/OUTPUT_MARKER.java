package com.spayker.ac.console.model.git;

public enum OUTPUT_MARKER {

    MODIFIED("modified:"),
    NEW("New "),
    DELETED("deleted:"),
    NOT_STAGED("Changes not staged for commit"),
    BRANCH_AHEAD("branch is ahead");

    private final String value;

    public String getValue() {
        return value;
    }

    OUTPUT_MARKER(String value){
        this.value = value;
    }



}
