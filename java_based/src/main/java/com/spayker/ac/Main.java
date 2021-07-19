package com.spayker.ac;

import com.spayker.ac.task.ChangeProcessor;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Main {

    private static final String CURRENT_APP_RUNNING_DIR = System.getProperty("user.dir");

    private static final int POOL_SIZE = 1;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(POOL_SIZE);

    private static final long INITIAL_DELAY = 1;
    private static final long PERIOD = 15;
    private static final TimeUnit UNIT = TimeUnit.MINUTES;

    public static void main(String[] args) {
        String path, token;

        if (args.length == 2) {
            path = args[0];
            token = args[1];
        } else {
            path = CURRENT_APP_RUNNING_DIR;
            token = args[0];
        }

        // get folder projects
        log.info("Working Directory = " + path);

        scheduler.scheduleAtFixedRate(new ChangeProcessor(path, token), INITIAL_DELAY, PERIOD, UNIT);
    }

}