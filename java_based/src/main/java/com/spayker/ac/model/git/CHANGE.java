package com.spayker.ac.model.git;

public enum CHANGE {

    ADDED("added"),
    CHANGED("changed"),
    MISSING("missing"),
    MODIFIED("modified"),
    UNTRACKED("untracked"),
    REMOVED("removed");

    private final String value;

    public String getValue() {
        return value;
    }

    CHANGE(String value){
        this.value = value;
    }

}
