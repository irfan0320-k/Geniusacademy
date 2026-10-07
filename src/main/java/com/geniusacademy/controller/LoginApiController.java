package com.geniusacademy.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class LoginApiController {

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password) {

        if ("admin".equals(username) &&
            "Genius@2026#".equals(password)) {

            return "success";
        }

        return "invalid";
    }
}