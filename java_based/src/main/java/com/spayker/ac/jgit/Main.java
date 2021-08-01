package com.spayker.ac.jgit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.spayker.ac.jgit.model.ApplicationConfig;
import com.spayker.ac.jgit.task.ChangeProcessorTask;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
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
    private static final String APP_GIT_TOKEN_PREFIX = "ghp_";

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

        // get token
        String token = getToken(arguments);

        try {
            // check yaml config
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            mapper.findAndRegisterModules();
            ApplicationConfig appConfig = mapper.readValue(new File(path + APP_YAML_CONFIG_PATH), ApplicationConfig.class);

            if(token == null) {
                token = appConfig.getToken();
            }
            initialDelay = appConfig.getInitialDelay();
            period = appConfig.getRunPeriod();
        } catch (IOException e) {
            log.warn(e.getMessage());
            log.info("Setting up default values for initial delay: " + DEFAULT_INITIAL_DELAY_MIN + " minutes");
            initialDelay = DEFAULT_INITIAL_DELAY_MIN;
            log.info("Setting up default values for schedule period: " + DEFAULT_PERIOD_MIN + " minutes");
            period = DEFAULT_PERIOD_MIN;
            if(token == null) {
                log.info("token was not found in config file, can't proceed next");
                return;
            }
        }
        scheduler.scheduleAtFixedRate(new ChangeProcessorTask(path, token), initialDelay, period, UNIT);
    }

    private static String getToken(final List<String> args) {
        for (String arg : args) {
           if (arg.toLowerCase().contains(APP_GIT_TOKEN_PREFIX)){
               return arg;
           }
        }
        return null;
    }

    private static String getPath(final List<String> args) {
        for (String arg : args) {
            File file = new File(arg);
            if (!file.isDirectory()) {
                file = file.getParentFile();
            }

            if (file.exists()){

            }

            return Paths.get(arg).toString();

        }
        return APP_CURRENT_DIR;
    }

}