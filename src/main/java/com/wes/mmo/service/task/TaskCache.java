package com.wes.mmo.service.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

    private TaskCache() {

    }
}
