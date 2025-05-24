package com.NLP.controller;

import com.NLP.Entity.Administrateur;
import com.NLP.Entity.Annotateur;
import com.NLP.Entity.*;
import com.NLP.Entity.Utilisateur;
import com.NLP.service.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UtilisateurService utilisateurService;

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "auth/access-denied";
    }

    @GetMapping("/redirect-by-role")
    public String redirectByRole(Authentication authentication) {
        String username = authentication.getName();
        Utilisateur user = utilisateurService.findByLogin(username);
        if (user instanceof Administrateur) {
            return "redirect:/admin/dashboard";
        } else if (user instanceof Annotateur) {
            return "redirect:/annotator/dashboard";
        }
        return "redirect:/auth/login?error";
    }
}
