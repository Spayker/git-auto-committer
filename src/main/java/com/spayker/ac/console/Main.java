package com.spayker.ac.console;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.spayker.ac.console.model.ApplicationConfig;
import com.spayker.ac.console.task.ChangeProcessorTask;
import com.spayker.ac.util.file.GitFolderRecognizer;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class Main {

    private static final String APP_CURRENT_DIR = System.getProperty("user.dir");
    private static final String APP_YAML_CONFIG_PATH = "/jgac.yml";

    private static final int POOL_SIZE = 1;
    private static final int DEFAULT_INITIAL_DELAY_MIN = 0;
    private static final int DEFAULT_PERIOD_MIN = 15;

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(POOL_SIZE);
    private static final TimeUnit UNIT = TimeUnit.MINUTES;

    public static void main(String[] args) {
        int initialDelay, period;

        List<String> arguments = Arrays.stream(args).collect(Collectors.toList());
        // get path
        final String path = getPath(arguments);
        log.info("Working Directory = " + path);

        try {
            // check yaml config
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            mapper.findAndRegisterModules();
            ApplicationConfig appConfig = mapper.readValue(new File(path + APP_YAML_CONFIG_PATH), ApplicationConfig.class);

            initialDelay = appConfig.getInitialDelay();
            period = appConfig.getRunPeriod();
        } catch (IOException e) {
            log.warn(e.getMessage());
            log.info("Setting up default values for initial delay: " + DEFAULT_INITIAL_DELAY_MIN + " minutes");
            initialDelay = DEFAULT_INITIAL_DELAY_MIN;
            log.info("Setting up default values for schedule period: " + DEFAULT_PERIOD_MIN + " minutes");
            period = DEFAULT_PERIOD_MIN;
        }
        scheduler.scheduleAtFixedRate(new ChangeProcessorTask(new GitFolderRecognizer(), path), initialDelay, period, UNIT);
    }

    private static String getPath(final List<String> args) {
        for (String arg : args) {
            File file = new File(arg);
            if (file.isDirectory()) {
                return arg;
            } else {
                return APP_CURRENT_DIR;
            }
        }
        return APP_CURRENT_DIR;
    }

}
