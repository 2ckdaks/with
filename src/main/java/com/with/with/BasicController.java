package com.with.with;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BasicController {

    @GetMapping("/")
    public String mainPage(){

        return "redirect:/list/page/1";  // 첫 번째 페이지로 리다이렉트
    }

}
