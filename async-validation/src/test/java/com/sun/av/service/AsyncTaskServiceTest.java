package com.sun.av.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AsyncTaskServiceTest {

    @Autowired
    private AsyncTaskService asyncTaskService;

    @Test
    void testPerformLongRunningTask() throws ExecutionException, InterruptedException {
        String taskName = "TestTask";

        long startTime = System.currentTimeMillis();
        CompletableFuture<String> future = asyncTaskService.performLongRunningTask(taskName);

        assertNotNull(future);
        assertFalse(future.isDone());

        String result = future.get();
        long endTime = System.currentTimeMillis();

        assertNotNull(result);
        assertTrue(result.contains(taskName));
        assertTrue(result.contains("Task completed"));
        assertTrue((endTime - startTime) >= 2000); // 至少 2 秒
    }

    @Test
    void testCalculateFactorial() throws ExecutionException, InterruptedException {
        int number = 5;

        CompletableFuture<Integer> future = asyncTaskService.calculateFactorial(number);

        assertNotNull(future);

        Integer result = future.get();

        assertNotNull(result);
        assertEquals(120, result); // 5! = 120
    }

    @Test
    void testSendEmailAsync() throws ExecutionException, InterruptedException {
        String recipient = "test@example.com";
        String subject = "Test Subject";
        String content = "Test Content";

        long startTime = System.currentTimeMillis();
        CompletableFuture<Void> future = asyncTaskService.sendEmailAsync(recipient, subject, content);

        assertNotNull(future);

        future.get(); // 等待完成
        long endTime = System.currentTimeMillis();

        assertTrue(future.isDone());
        assertTrue((endTime - startTime) >= 2000); // 至少 2 秒
    }

    @Test
    void testMultipleAsyncTasks() throws ExecutionException, InterruptedException {
        CompletableFuture<String> task1 = asyncTaskService.performLongRunningTask("Task-1");
        CompletableFuture<String> task2 = asyncTaskService.performLongRunningTask("Task-2");
        CompletableFuture<String> task3 = asyncTaskService.performLongRunningTask("Task-3");

        long startTime = System.currentTimeMillis();

        CompletableFuture.allOf(task1, task2, task3).get();

        long endTime = System.currentTimeMillis();

        assertTrue(task1.isDone());
        assertTrue(task2.isDone());
        assertTrue(task3.isDone());

        // 三個任務應該並行執行，總時間應該小於 15 秒（3 * 5秒）
        assertTrue((endTime - startTime) < 15000);
    }
}
