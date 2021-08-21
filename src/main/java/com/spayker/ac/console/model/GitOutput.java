package com.spayker.ac.console.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
public class GitOutput {

    @Getter
    private String output;

    @Getter
    private List<String> changes;

}
