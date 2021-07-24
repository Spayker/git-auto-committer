package com.spayker.ac.model;

import lombok.Getter;

public class AppConfig {
    @Getter
    private String token;
    @Getter
    private int runPeriod;
    @Getter
    private int initialDelay;
}
