package com.example.demo.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class LoginController {

    @GetMapping("/login")
    public ModelAndView login(String error, String logout) {
        ModelAndView modelAndView = new ModelAndView("login");
        if (error != null) {
            modelAndView.addObject("error", "Invalid username or password. Please try again.");
        }
        if (logout != null) {
            modelAndView.addObject("logout", "You have been logged out successfully.");
        }
        return modelAndView;
    }
}
