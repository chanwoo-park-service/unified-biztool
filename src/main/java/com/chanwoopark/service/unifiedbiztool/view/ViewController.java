package com.chanwoopark.service.unifiedbiztool.view;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ViewController {

    @GetMapping("/index")
    public ModelAndView index() {
        return new ModelAndView("index");
    }

    @GetMapping("/token-page")
    public ModelAndView tokenPage() {
        return new ModelAndView("settings");
    }

    @GetMapping("/upload-page")
    public ModelAndView uploadPage() {
        return new ModelAndView("upload-page");
    }

    @GetMapping("/signin")
    public ModelAndView signin() {
        return new ModelAndView("signin");
    }
}
