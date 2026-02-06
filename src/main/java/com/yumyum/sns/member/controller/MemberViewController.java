package com.yumyum.sns.member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MemberViewController {

    @GetMapping("/member/profile/edit")
    public String editProfile(){

        return "editProfile";
    }

    @GetMapping("/member/signup")
    public String register(){
        return "signup";
    }
}
