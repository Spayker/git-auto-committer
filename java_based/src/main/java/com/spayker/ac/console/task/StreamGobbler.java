package com.spayker.ac.console.task;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamGobbler implements Runnable {

    private InputStream inputStream;
    private StringBuilder output;

    public StreamGobbler(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public void run() {
        new BufferedReader(new InputStreamReader(inputStream)).lines()
                .forEach(l -> output.append(l));
    }

    public String getOutput() {
        return output.toString();
    }
}
