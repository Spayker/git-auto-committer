package com.spayker.ac.console.model.git;

public enum CHANGE {

    ADDED("added"),
    CHANGED("changed"),
    MISSING("missing"),
    MODIFIED("modified"),
    UNTRACKED("untracked"),
    REMOVED("removed"),
    AHEAD("ahead");

    private final String value;

    public String getValue() {
        return value;
    }

    CHANGE(String value){
        this.value = value;
    }

}
