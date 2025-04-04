package com.example.demo.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller responsible for handling login page requests.
 * Provides feedback for unsuccessful login attempts or logout actions.
 */
@Controller
public class LoginController {

    /**
     * Displays the login page with optional error or logout messages.
     *
     * @param error Optional parameter indicating a failed login attempt.
     * @param logout Optional parameter indicating a successful logout.
     * @return ModelAndView object containing view name and relevant messages.
     */
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
