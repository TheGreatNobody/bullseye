package com.sun.av.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AsyncTaskService {

    private static final Logger logger = LoggerFactory.getLogger(AsyncTaskService.class);

    @Async("taskExecutor")
    public CompletableFuture<String> performLongRunningTask(String taskName) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Starting task: {} on thread: {}", taskName, Thread.currentThread().getName());

            try {
                // 模擬長時間運行的任務
                Thread.sleep(ThreadLocalRandom.current().nextInt(2000, 6000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new CompletionException(e);
            }

            String result = "Task completed: " + taskName + " on thread: " + Thread.currentThread().getName();
            logger.info("Completed task: {}", taskName);
            return result;
        });
    }

    @Async("taskExecutor")
    public CompletableFuture<List<String>> performLongRunningTask(List<String> tasks) {
        logger.info("Starting task on thread: {}", Thread.currentThread().getName());

        List<CompletableFuture<String>> futureList = tasks.stream()
                .map(t -> {
                    logger.info(t);
                    return performLongRunningTask(t);
                })
                .toList();

        CompletableFuture<List<String>> futureCombine = CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]))
                .thenApply(v -> futureList.stream().map(CompletableFuture::join).toList());

        logger.info("Completed task- Task completed on thread: {}", Thread.currentThread().getName());

        return futureCombine;
    }

    @Async("taskExecutor")
    public CompletableFuture<String> performLongRunningTaskPrivate(String taskName){
        logger.info("performLongRunningTaskPrivate Starting task: {} on thread: {}", taskName, Thread.currentThread().getName());

        try {
            // 模擬長時間運行的任務
            Thread.sleep(ThreadLocalRandom.current().nextInt(2000, 15000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return CompletableFuture.completedFuture("Task interrupted: " + taskName);
        }

        String result = "Task completed: " + taskName + " on thread: " + Thread.currentThread().getName();
        logger.info("performLongRunningTaskPrivate Completed task: {}", taskName);

        return CompletableFuture.completedFuture(result);
    }

    @Async("taskExecutor")
    public CompletableFuture<Integer> calculateFactorial(int number) {
        logger.info("Calculating factorial of {} on thread: {}", number, Thread.currentThread().getName());

        try {
            Thread.sleep(1000); // 模擬計算時間
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return CompletableFuture.completedFuture(0);
        }

        int result = 1;
        for (int i = 1; i <= number; i++) {
            result *= i;
        }

        logger.info("Factorial of {} is {}", number, result);
        return CompletableFuture.completedFuture(result);
    }

    @Async("emailExecutor")
    public CompletableFuture<Void> sendEmailAsync(String recipient, String subject, String content) {
        logger.info("Sending email to {} on thread: {}", recipient, Thread.currentThread().getName());

        try {
            // 模擬發送郵件的時間
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return CompletableFuture.completedFuture(null);
        }

        logger.info("Email sent successfully to: {}", recipient);
        return CompletableFuture.completedFuture(null);
    }

}