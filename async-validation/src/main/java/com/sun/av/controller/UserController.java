package com.sun.av.controller;

import com.sun.av.aspect.annotation.SensitiveLog;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping("/public")
    public String publicInfo(@RequestParam String name) {
        return "Public info for " + name;
    }

    @SensitiveLog(hideParams = true)
    @PostMapping("/private")
    public String privateInfo(@RequestBody String sensitiveData) {
        return "Sensitive info processed";
    }

}