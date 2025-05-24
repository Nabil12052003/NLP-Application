package com.NLP.service;

import com.NLP.Entity.Utilisateur;

import java.util.List;

public interface UtilisateurService {
    List<Utilisateur> findAll();
    String createUser(String login, String nom, String prenom, String email, String roleName);
    void updateUser(Long id, String login, String nom, String prenom, String email);
    void deleteUser(Long id);
    List<Utilisateur> getAnnotators();
    Utilisateur findById(Long id);
    Utilisateur findByLogin(String login);
}
