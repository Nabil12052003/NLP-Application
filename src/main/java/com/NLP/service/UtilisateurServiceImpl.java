package com.NLP.service;

import com.NLP.Entity.Administrateur;
import com.NLP.Entity.Annotateur;
import com.NLP.Entity.Role;
import com.NLP.Entity.Utilisateur;

import com.NLP.repository.RoleRepository;
import com.NLP.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class UtilisateurServiceImpl implements UtilisateurService {
    @Autowired private UtilisateurRepository utilisateurRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;


    public List<Utilisateur> findAll() {
        return utilisateurRepository.findAll();
    }

    public String createUser(String login, String nom, String prenom, String email, String roleName) {
        if (utilisateurRepository.findByLogin(login).isPresent()) {
            throw new IllegalArgumentException("Login déjà utilisé");
        }
        String rawPassword = generateRandomPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);

        Utilisateur utilisateur;
        if ("ADMINISTRATEUR".equalsIgnoreCase(roleName)) {
            utilisateur = new Administrateur();
        } else if ("ANNOTATEUR".equalsIgnoreCase(roleName)) {
            utilisateur = new Annotateur();
        } else {
            throw new IllegalArgumentException("Rôle non valide");
        }

        utilisateur.setLogin(login);
        utilisateur.setNom(nom);
        utilisateur.setPrenom(prenom);
        utilisateur.setEmail(email);
        utilisateur.setPassword(encodedPassword);
        utilisateur.setDeleted(false);

        Role role = roleRepository.findByNomRole(roleName.toUpperCase());
        if (role == null) {
            throw new IllegalArgumentException("Rôle non trouvé");
        }
        utilisateur.setRoles(Arrays.asList(role));

        utilisateurRepository.save(utilisateur);

        return rawPassword;
    }

    public void updateUser(Long id, String login, String nom, String prenom, String email) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
        utilisateur.setLogin(login);
        utilisateur.setNom(nom);
        utilisateur.setPrenom(prenom);
        utilisateur.setEmail(email);
        utilisateurRepository.save(utilisateur);
    }

    public void deleteUser(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
        utilisateur.setDeleted(true);
        utilisateurRepository.save(utilisateur);
    }

    public List<Utilisateur> getAnnotators() {
        return utilisateurRepository.findByDeletedFalse().stream()
                .filter(user -> user instanceof Annotateur)
                .collect(Collectors.toList());
    }

    public Utilisateur findById(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
    }

    public Utilisateur findByLogin(String login) {
        return utilisateurRepository.findByLogin(login)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }


}