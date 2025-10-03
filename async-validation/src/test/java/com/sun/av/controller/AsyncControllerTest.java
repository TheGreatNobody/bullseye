package com.sun.av.controller;

import com.sun.av.service.AsyncTaskService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AsyncController.class)
class AsyncControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AsyncTaskService asyncTaskService;


    @Test
    void testPerformAsyncTask() throws Exception {
        when(asyncTaskService.performLongRunningTask(anyString()))
                .thenReturn(CompletableFuture.completedFuture("Task completed: TestTask"));

        mockMvc.perform(post("/async/task")
                        .param("taskName", "TestTask"))
                .andExpect(status().isOk());
    }

    @Test
    void testCalculateFactorial() throws Exception {
        when(asyncTaskService.calculateFactorial(anyInt()))
                .thenReturn(CompletableFuture.completedFuture(120));

        mockMvc.perform(post("/async/factorial")
                        .param("number", "5"))
                .andExpect(status().isOk());
    }

    @Test
    void testSendEmail() throws Exception {
        when(asyncTaskService.sendEmailAsync(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));

        mockMvc.perform(post("/async/email")
                        .param("recipient", "test@example.com")
                        .param("subject", "Test Subject")
                        .param("content", "Test Content"))
                .andExpect(status().isOk());
    }


}