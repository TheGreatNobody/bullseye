package com.sun.av.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

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

    public CompletableFuture<List<String>> toMultipleAsyncPerformTask(List<String> multipleTask){
        log.info("類別: {}; to AsyncTaskService before...", this.getClass().getName());

        log.info("步驟1️⃣: 準備執行 {} 個任務", multipleTask.size());

        List<CompletableFuture<String>> futures = new ArrayList<>();

        for(String task: multipleTask){
            CompletableFuture<String> future = asyncTaskService.performLongRunningTask(task);
            futures.add(future);
            log.info("步驟2️⃣.{}: 創建 Future 對象 (isDone: {})", futures.size(), future.isDone());
        }

        // 步驟3: 轉換為數組（這裡是關鍵！）
        log.info("步驟3️⃣: 將 List 轉換為數組");
        log.info("List 大小: {}", futures.size());
        CompletableFuture<String>[] futureArray = futures.toArray(new CompletableFuture[0]);
        log.info("數組長度: {} (自動調整為正確大小！)", futureArray.length);
        log.info("數組內容: {}", Arrays.toString(futureArray));

        // 步驟4: 使用 allOf 等待所有任務完成
        log.info("步驟4️⃣: 使用 allOf 等待所有任務完成");
        CompletableFuture<Void> allOfFuture = CompletableFuture.allOf(futureArray);
        log.info("allOf Future 創建完成 (isDone: {})", allOfFuture.isDone());

        // 步驟5: 當所有任務完成後，收集結果
        return allOfFuture.thenApply(v -> {
            log.info("步驟5️⃣: 所有任務已完成，開始收集結果");

            // 從原始的 futures List 中獲取結果（不是從 futureArray）
            List<String> results = futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());

            log.info("收集到 {} 個結果", results.size());
            for (int i = 0; i < results.size(); i++) {
                log.info("結果[{}]: {}", i, results.get(i));
            }

            return results;
        });
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
