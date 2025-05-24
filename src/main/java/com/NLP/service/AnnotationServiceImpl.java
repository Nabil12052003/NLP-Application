package com.NLP.service;

import com.NLP.Entity.*;

import com.NLP.repository.AnnotationRepository;
import com.NLP.repository.CoupleTexteRepository;
import com.NLP.repository.TacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AnnotationServiceImpl implements AnnotationService {
    @Autowired private AnnotationRepository annotationRepository;
    @Autowired private CoupleTexteRepository coupleTexteRepository;
    @Autowired private TacheRepository tacheRepository;
    @Autowired private UtilisateurService utilisateurService;

    @Transactional
    public void saveAnnotation(Long tacheId, Long annotateurId, Long coupleId, String classe) {
        Utilisateur utilisateur = utilisateurService.findById(annotateurId);
        if (!(utilisateur instanceof Annotateur)) {
            throw new IllegalArgumentException("L'utilisateur avec ID " + annotateurId + " n'est pas un annotateur");
        }
        Annotateur annotateur = (Annotateur) utilisateur;

        CoupleTexte coupleTexte = coupleTexteRepository.findById(coupleId)
                .orElseThrow(() -> new IllegalArgumentException("Couple non trouvé"));

        Optional<Annotation> existingAnnotation = annotationRepository.findByCoupleTexteAndAnnotateur(coupleTexte, annotateur);
        Annotation annotation;

        if (existingAnnotation.isPresent()) {
            annotation = existingAnnotation.get();
            annotation.setClasse(classe);
        } else {
            annotation = new Annotation();
            annotation.setCoupleTexte(coupleTexte);
            annotation.setAnnotateur(annotateur);
            annotation.setClasse(classe);
        }
        annotationRepository.save(annotation);

        Tache tache = tacheRepository.findById(tacheId)
                .orElseThrow(() -> new IllegalArgumentException("Tache non trouvée avec ID " + tacheId));
        List<CoupleTexte> couples = tache.getAssignedCouples();
        if (couples == null || couples.isEmpty()) {
            return;
        }
        long annotatedCount = couples.stream()
                .filter(c -> annotationRepository.findByCoupleTexteAndAnnotateur(c, annotateur).isPresent())
                .count();
        double avancementTache = (double) annotatedCount / couples.size() * 100;
        tache.setAvancement(Math.min(avancementTache, 100.0));
        tacheRepository.save(tache);
    }
}