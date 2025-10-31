package com.yumyum.sns.post.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PostViewController {

    @GetMapping("/member/likes")
    public String likes(){
        return "likes";
    }
}
