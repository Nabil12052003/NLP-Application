package com.NLP.repository;


import com.NLP.Entity.Annotateur;
import com.NLP.Entity.Annotation;
import com.NLP.Entity.CoupleTexte;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AnnotationRepository extends JpaRepository<Annotation, Long> {
    List<Annotation> findByCoupleTexte_DatasetId(Long datasetId);
    long countByAnnotateurIdAndCoupleTexte_DatasetId(Long annotateurId, Long datasetId);
    Optional<Annotation> findByCoupleTexteAndAnnotateur(CoupleTexte coupleTexte, Annotateur annotateur);
    long countByCoupleTexte_DatasetId(Long datasetId);
}