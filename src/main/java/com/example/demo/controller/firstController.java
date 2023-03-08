package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class firstController {
    @RequestMapping("/index")
    public String firstPage(){
        return "index";
    }
}
