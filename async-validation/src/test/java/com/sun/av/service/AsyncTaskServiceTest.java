package com.sun.av.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
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

    public static String defineIdentifyType(String strForTest){
        if(StringUtils.isEmpty(strForTest)){
            return "";
        }
        return strForTest.substring(1, 2).matches("^[A-Za-z89]+$") ? "05" : "01";
    }

    @Test
    void testLocalDate(){


        String id = "F123456789";
        System.out.println(defineIdentifyType(id));

        String id2 = "F813456789";
        System.out.println(defineIdentifyType(id2));

        String id3 = "FC23456789";
        System.out.println(defineIdentifyType(id3));

        String date1 = "1981-06-19";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate parse1 = LocalDate.parse(date1, formatter);
        System.out.println(parse1);

        LocalDate date2 = LocalDate.of(1981, 6, 19);
        System.out.println(date2);

        LocalDate date3 = LocalDate.parse("1981-06-19");
        System.out.println(date3);
//        LocalDate parse2 = LocalDate.parse(date2);
//        System.out.println(parse2);

        String strDate4 = "1981-06-19";
        LocalDate date4 = LocalDate.parse(strDate4, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        System.out.println(date4);

//        String realName = "劉𤧟王文𠍇";
        String test = "李佳\uD852\uDDDF";
//        String realName = "李佳𤧟  ";
//        String realName = "魏\uD866\uDC73㟒";
//        String realName = "魏䠷千";
        String realName = "魏𩡳㟒";

        System.out.println("原名: "+  realName);
//        String maskName = cleanXmlInvalidChars(realName);
//        System.out.println(maskName);
//        String finalName = maskName.replace("**", "★");
//        System.out.println(finalName);


        String maskNameNew = cleanXmlInvalidCharsNew(realName);
        System.out.println("原名修改後: "+  maskNameNew);
    }

    private static String cleanXmlInvalidChars(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            // 允許的字符範圍：
            // 0x9 (tab), 0xA (換行), 0xD (回車)
            // 0x20-0xD7FF (基本多語言平面，但排除代理對)
            // 0xE000-0xFFFD (私人使用區和其他字符)
            // 排除：0xD800-0xDFFF (代理對字符)
            if ((c == 0x9) || (c == 0xA) || (c == 0xD) ||
                    ((c >= 0x20) && (c <= 0xD7FF)) ||
                    ((c >= 0xE000) && (c <= 0xFFFD))) {
                sb.append(c);
            } else {
                // 非法字符，用 * 替換
                sb.append('*');
//                if (log.isDebugEnabled()) {
//                    log.info("用 * 替換非法 XML 字符: 0x" + Integer.toHexString(c));
                    System.out.println("用 * 替換非法 XML 字符: 0x" + Integer.toHexString(c));
//                }
            }
        }

        return sb.toString();
    }


    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            return true;
        }
        return false;
    }


    private static String cleanXmlInvalidCharsNew(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            int codePoint = c;
            log.info("codePoint {}", codePoint);
            boolean isChinese = isChinese(c);
            log.info("isChinese {} ", isChinese );

            log.info("isExtensionA {} ", Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A);
            log.info("isExtensionB {} ", Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B);

            // 檢查是否為高代理（High Surrogate: 0xD800-0xDBFF）
            if (c >= 0xD800 && c <= 0xDBFF) {
                // 檢查是否有配對的低代理
                if (i + 1 < input.length()) {
                    char next = input.charAt(i + 1);
                    if (next >= 0xDC00 && next <= 0xDFFF) {
                        // 有效的代理對，但 XML 1.0 不支援，替換為 ★
                        sb.append('★');
                        i++; // 跳過下一個字符（低代理）
                        log.info("用 ★ 替換補充平面字符（代理對）: 0x{} 0x{}", Integer.toHexString(c), Integer.toHexString(next));
                        continue;
                    }
                }
                // 無效的單獨高代理，替換為 ★
                sb.append('★');
                log.info("用 ★ 替換無效的高代理字符: 0x{}", Integer.toHexString(c));
            } else if (c >= 0xDC00 && c <= 0xDFFF) {
                // 單獨出現的低代理（無效），替換為 ★
                sb.append('★');
                log.info("用 ★ 替換無效的低代理字符: 0x{}", Integer.toHexString(c));
            } else if ((c == 0x9) || (c == 0xA) || (c == 0xD) ||
                    ((c >= 0x20) && (c <= 0xD7FF)) ||
                    ((c >= 0xE000) && (c <= 0xFFFD))) {
                // 允許的字符範圍
                sb.append(c);
            } else {
                // 其他非法字符，用 ★ 替換
                sb.append('★');
                log.info("用 ★ 替換非法 XML 字符: 0x{}", Integer.toHexString(c));
            }
        }

        return sb.toString();
    }

}
