package com.yumyum.sns.error.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/error")
public class ErrorPageController {

    @GetMapping("/500")
    public String error500(){
        return "error/500";
    }

    @GetMapping("/home")
    public String home(Authentication authentication){
        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {

            // 로그인 상태
            return "redirect:/main";
        }

        // 비로그인 상태
        return "redirect:/";
    }

}
