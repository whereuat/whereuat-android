package xyz.whereuat.whereuat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 */
public class AsyncExecutor {
    public static ExecutorService service = Executors.newSingleThreadExecutor();
}
