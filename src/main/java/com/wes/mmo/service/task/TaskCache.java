package com.wes.mmo.service.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TaskCache {

    private static final Log LOG = LogFactory.getLog(TaskCache.class);

    private static volatile TaskCache INSTANCE = null;

    public static TaskCache GetTaskCache(){
        if(INSTANCE == null){
            synchronized (TaskCache.class) {
                if(INSTANCE == null) {
                    INSTANCE = new TaskCache();
                }
            }
        }
        return INSTANCE;
    }

    private ScheduledExecutorService scheduledExecutorService= null;

    private TaskCache() {
        scheduledExecutorService = Executors.newScheduledThreadPool(10);
    }

    public void scheduleTask(Thread thread, long actionTimeStampt) {
        if(actionTimeStampt <= System.currentTimeMillis())
            thread.start();
        else
            scheduledExecutorService.schedule(thread, actionTimeStampt - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }
}
