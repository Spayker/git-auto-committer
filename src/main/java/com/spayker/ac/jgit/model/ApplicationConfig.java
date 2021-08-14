package com.spayker.ac.jgit.model;

import lombok.Getter;

public class ApplicationConfig {
    @Getter
    private String token;
    @Getter
    private int runPeriod;
    @Getter
    private int initialDelay;
}
