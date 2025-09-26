package com.sun.av.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {


    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/demo")
    public String demo(Model model) {
        model.addAttribute("message", "異步操作演示頁面");
        return "demo";
    }

}