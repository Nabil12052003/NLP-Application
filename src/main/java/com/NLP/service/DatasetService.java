package com.NLP.service;

import com.NLP.Entity.*;

import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DatasetService {
    List<Dataset> getAllDatasets();
    Dataset createDataset(String nomDataset, String description, String classes);
    Page<CoupleTexte> getCoupleTextesByDatasetId(Long datasetId, int page, int size);
    List<Utilisateur> getAnnotatorsByDatasetId(Long datasetId);
    void assignAnnotators(Long datasetId, List<Long> annotateurIds, LocalDate dateLimite);
    String exportDataset(Long datasetId);
    List<ClassesPossibles> getClassesByDatasetId(Long datasetId);
    List<Tache> getTachesByAnnotateur(Long annotateurId);
    Optional<Dataset> findById(Long id);
    List<Annotateur> getAnnotatorsForDataset(Long datasetId);
    List<Annotateur> getAvailableAnnotators(Dataset dataset, Annotateur excludedAnnotator);
}