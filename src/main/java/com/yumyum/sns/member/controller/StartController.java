package com.yumyum.sns.member.controller;

import com.yumyum.sns.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class StartController {

    private final MemberService memberService;

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
        if (!memberService.checkNicknameDuplicate(nickname)) {
            return "redirect:/error/404";
        }
        return "userDetail";
    }
}
