package com.spayker.ac.model.git;

public enum CHANGE {

    ADDED("added"),
    CHANGED("changed"),
    CONFLICTING("conflicting"),
    CONFLICTING_STAGE_STATE("conflicting stage state"),
    IGNORED_NOT_IN_INDEX("ignored not in index"),
    MISSING("missing"),
    MODIFIED("modified"),
    REMOVED("removed");

    private final String value;

    public String getValue() {
        return value;
    }

    CHANGE(String value){
        this.value = value;
    }

}
