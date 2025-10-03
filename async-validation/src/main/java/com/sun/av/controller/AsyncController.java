package com.sun.av.controller;

import com.sun.av.service.AsyncTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

@RequestMapping("/async")
@Controller
public class AsyncController {

    private static final Logger logger = LoggerFactory.getLogger(AsyncController.class);
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Autowired
    private AsyncTaskService asyncTaskService;

    @PostMapping("/task")
    @ResponseBody
    public CompletableFuture<String> performAsyncTask(@RequestParam String taskName) {
        logger.info("[Before Service] Received request for async task: {}", taskName);
        CompletableFuture<String> future = asyncTaskService.performLongRunningTask(taskName);
        logger.info("[After Service] Received request for async task: {}", taskName);
        return future;
    }


    @PostMapping("/tasks")
    @ResponseBody
    public List<String> performAsyncTask(@RequestParam Integer timeout) {
        logger.info("[Before Service] Received request for async task");
        CompletableFuture<List<String>> futureCombine = asyncTaskService.performLongRunningTask(Arrays.asList("任務一", "任務二", "任務三"));
        logger.info("[After Service] Received request for async task");
        List<String> stringList = new ArrayList<>();
        try {
            stringList = futureCombine.get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("tasks failed.", e);
        }
        return stringList;
    }

    @GetMapping("/tasks/stream")
    public SseEmitter performAsyncTaskStream(@RequestParam(required = false) String tasks, @RequestParam Integer timeout) {
        logger.info("[Stream] Received request for async task stream");
        List<String> taskList = tasks != null ? Arrays.asList(tasks.split(",")) : Arrays.asList("任務一", "任務二", "任務三");
        Long timeoutL = timeout.longValue() * 1000;
        SseEmitter emitter = new SseEmitter(timeoutL); // 6秒超時

        executor.execute(() -> {
            try {
                List<CompletableFuture<String>> futures = taskList.stream()
                        .map(asyncTaskService::performLongRunningTask)
                        .toList();

                // 使用 CompletableFuture.anyOf 來監聽任何一個任務完成
                List<CompletableFuture<String>> pending = new ArrayList<>(futures);

                while (!pending.isEmpty()) {
                    CompletableFuture<Object> anyCompleted = CompletableFuture.anyOf(pending.toArray(new CompletableFuture[0]));
                    Object result = anyCompleted.get();

                    // 找出並移除已完成的任務
                    pending.removeIf(CompletableFuture::isDone);

                    // 立即推送完成的結果
                    emitter.send(SseEmitter.event()
                            .name("task-completed")
                            .data(result.toString()));

                    logger.info("[Stream] Sent completed task: {}", result);
                }

                emitter.send(SseEmitter.event().name("all-completed").data("所有任務已完成"));
                emitter.complete();
                logger.info("[Stream] All tasks completed");

            } catch (Exception e) {
                logger.error("[Stream] Error during streaming", e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    @PostMapping("/factorial")
    @ResponseBody
    public CompletableFuture<String> calculateFactorial(@RequestParam int number) {
        logger.info("Received request for factorial calculation: {}", number);
        return asyncTaskService.calculateFactorial(number)
                .thenApply(result -> "Factorial of " + number + " is: " + result);
    }

    @PostMapping("/email")
    @ResponseBody
    public CompletableFuture<String> sendEmail(@RequestParam String recipient,
                                               @RequestParam String subject,
                                               @RequestParam String content) {
        logger.info("Received request to send email to: {}", recipient);
        return asyncTaskService.sendEmailAsync(recipient, subject, content)
                .thenApply(result -> "Email sent successfully to: " + recipient);
    }

    @GetMapping("/multiple")
    @ResponseBody
    public CompletableFuture<String> performMultipleTasks() {
        logger.info("Starting multiple async tasks");

        CompletableFuture<String> task1 = asyncTaskService.performLongRunningTask("Task-1");
        CompletableFuture<String> task2 = asyncTaskService.performLongRunningTask("Task-2");
        CompletableFuture<String> task3 = asyncTaskService.performLongRunningTask("Task-3");

        return CompletableFuture.allOf(task1, task2, task3)
                .thenApply(v -> {
                    StringBuilder result = new StringBuilder();
                    result.append(task1.join()).append("; ");
                    result.append(task2.join()).append("; ");
                    result.append(task3.join());
                    return result.toString();
                });
    }

    @GetMapping("/sse")
    public SseEmitter streamEvents() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        executor.execute(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    Thread.sleep(1000);
                    emitter.send("Event " + i + " at " + System.currentTimeMillis());
                }
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

}