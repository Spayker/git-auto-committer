package com.spayker.ac.console.model.git;

public enum COMMAND {

    STATUS("status"),
    ADD("add"),
    COMMIT("commit"),
    BRANCH("branch"),
    PUSH("push");

    private final String value;

    public String getValue() {
        return value;
    }

    COMMAND(String value){
        this.value = value;
    }

}
