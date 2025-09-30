package com.sun.av.controller;

import com.sun.av.aspect.annotation.SensitiveLog;
import com.sun.av.service.AsyncTaskService;
import com.sun.av.service.SyncTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

@RequestMapping("/sync")
@Controller
public class SyncController {

    private static final Logger logger = LoggerFactory.getLogger(SyncController.class);
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Autowired
    private SyncTaskService syncTaskService;

    @Autowired
    private AsyncTaskService asyncTaskService;

//    @GetMapping("/")
//    public String index() {
//        return "index";
//    }

    @SensitiveLog(hideParams = false)
    @PostMapping("/to-async-perform-task")
    @ResponseBody
    public CompletableFuture<String> syncToAsyncPerformTask() {
        logger.info("執行前");
        CompletableFuture<String> future = syncTaskService.toAsyncPerformTask();
        logger.info("執行後");
        return future;
    }

    @PostMapping("/to-multiple-async-perform-task")
    @ResponseBody
    public List<String> syncToMultipleAsyncPerformTask() throws ExecutionException, InterruptedException, TimeoutException {
        logger.info("執行前");
        CompletableFuture<List<String>> future = syncTaskService.toMultipleAsyncPerformTask(Arrays.asList("任務一","任務二","任務三"));
//        List<String> results = future.join(); // 會阻塞
        List<String> results = future.get(30, TimeUnit.SECONDS);
        logger.info("執行後");
        return results;
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