package com.chanwoopark.service.unifiedbiztool.view;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ViewController {

    @GetMapping(value = {"/index", ""})
    public ModelAndView index() {
        return new ModelAndView("index");
    }

    @GetMapping("/setting")
    public ModelAndView setting() {
        return new ModelAndView("settings");
    }

    @GetMapping("/upload-page")
    public ModelAndView uploadPage() {
        return new ModelAndView("uploadPage");
    }

    @GetMapping("/login")
    public ModelAndView loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "expired", required = false) String expired
    ) {
        ModelAndView modelAndView = new ModelAndView("signin");

        if (error != null) {
            modelAndView.addObject("errorMessage", "로그인에 실패했습니다. 아이디와 비밀번호를 확인해주세요.");
        }

        if (logout != null) {
            modelAndView.addObject("successMessage", "성공적으로 로그아웃되었습니다.");
        }

        if (expired != null) {
            modelAndView.addObject("expireMessage", "세션이 만료되었습니다. 다시 로그인해주세요.");
        }


        return modelAndView;
    }
}
