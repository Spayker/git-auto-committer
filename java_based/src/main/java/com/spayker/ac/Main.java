package com.spayker.ac;

import com.spayker.ac.task.ChangeProcessor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.lib.UserConfig;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.spayker.ac.model.git.CHANGE.*;

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