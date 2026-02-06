package com.yumyum.sns.member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class StartController {

    @GetMapping("/")
    public String mainAPI(){

        return "index";
    }

    @GetMapping("/main")
    public String mainPage(){
        return "main";
    }

    @GetMapping("/user/{nickname}")
    public String userDetailPage(@PathVariable String nickname){
        return "userDetail";
    }
}
