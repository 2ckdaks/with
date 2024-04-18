package com.with.with;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class MyExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception e) {
        ModelAndView mav = new ModelAndView("error"); // error.html 템플릿 사용
        mav.addObject("message", e.getMessage()); // 예외 메시지
        mav.addObject("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        mav.addObject("error", "Internal Server Error");
        mav.addObject("path", "/path-of-request-if-needed"); // 요청 경로 추가 가능
        mav.addObject("exception", e.getClass().getSimpleName()); // 예외 유형의 단순 이름
        return mav;
    }
}
