package com.sun.av.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class SyncTaskService {

    @Autowired
    private AsyncTaskService asyncTaskService;

    public CompletableFuture<String> toAsyncPerformTask(){
        log.info("類別: {}; to AsyncTaskService before...", this.getClass().getName());
        CompletableFuture<String> future = asyncTaskService.performLongRunningTask("由同步method呼叫非同步Class");
        log.info("類別: {}; to AsyncTaskService after...", this.getClass().getName());
        return future;
    }

    public String performLongRunningTask(String taskName) {
        log.info("SyncTaskService.performLongRunningTask...");
        log.info("Starting task: {} on thread: {}", taskName, Thread.currentThread().getName());

        try {
            // 模擬長時間運行的任務
            Thread.sleep(ThreadLocalRandom.current().nextInt(2000, 15000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Task interrupted: " + taskName;
        }

        String result = "Task completed: " + taskName + " on thread: " + Thread.currentThread().getName();
        log.info("Completed task: {}", taskName);

        CompletableFuture<String> future = CompletableFuture.completedFuture(result);
        return result;
    }

}
