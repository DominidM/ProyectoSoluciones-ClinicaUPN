package com.clinica.universitaria.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error,
                        @RequestParam(required = false) String logout,
                        Model model) {
        model.addAttribute("pageTitle", "Iniciar sesión");
        model.addAttribute("loginError", error != null);
        model.addAttribute("logoutOk", logout != null);
        return "auth/login";
    }
}
