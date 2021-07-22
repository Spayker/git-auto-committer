package com.spayker.ac;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.spayker.ac.model.git.AppConfig;
import com.spayker.ac.task.ChangeProcessor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Main {

    private static final String CURRENT_APP_RUNNING_DIR = System.getProperty("user.dir");
    private static final String APP_YAML_CONFIG_PATH = CURRENT_APP_RUNNING_DIR + "/jgac.yml";

    private static final int POOL_SIZE = 1;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(POOL_SIZE);

    private static int initialDelay = 1;
    private static int period = 15;
    private static final TimeUnit UNIT = TimeUnit.MINUTES;

    public static void main(String[] args) {
        String path = CURRENT_APP_RUNNING_DIR;
        String token;

        try {
            // check yaml config
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            mapper.findAndRegisterModules();
            AppConfig appConfig = mapper.readValue(new File(APP_YAML_CONFIG_PATH), AppConfig.class);

            token = appConfig.getToken();
            initialDelay = Optional.ofNullable(appConfig.getInitialDelay()).orElse(1);
            period = Optional.ofNullable(appConfig.getRunPeriod()).orElse(15);
        } catch (IOException e) {
            log.error(e.getMessage());
            if (args.length == 2) {
                path = args[0];
                token = args[1];
            } else {
                path = CURRENT_APP_RUNNING_DIR;
                token = args[0];
            }
        }

        // get folder projects
        log.info("Working Directory = " + path);
        scheduler.scheduleAtFixedRate(new ChangeProcessor(path, token), initialDelay, period, UNIT);
    }

}