package com.spayker.ac;

import com.spayker.ac.task.ChangeProcessor;
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

public class Main {

    private static final int POOL_SIZE = 1;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(POOL_SIZE);
    private static final String EMPTY_STRING = "";

    private static final long INITIAL_DELAY = 5;
    private static final long PERIOD = 5;
    private static final TimeUnit UNIT = TimeUnit.MINUTES;

    public static void main(String[] args) {
        scheduler.scheduleAtFixedRate(new ChangeProcessor(Arrays.stream(args).findFirst().orElse(EMPTY_STRING)), INITIAL_DELAY, PERIOD, UNIT);
    }

}