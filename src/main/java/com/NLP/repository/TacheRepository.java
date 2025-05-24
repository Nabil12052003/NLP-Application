package com.NLP.repository;


import com.NLP.Entity.Annotateur;
import com.NLP.Entity.Dataset;
import com.NLP.Entity.Tache;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TacheRepository extends JpaRepository<Tache, Long> {
    List<Tache> findByDatasetId(Long datasetId);
    List<Tache> findByAnnotateurId(Long annotateurId);
    List<Tache> findByDataset(Dataset dataset);
    Optional<Tache> findByDatasetAndAnnotateur(Dataset dataset, Annotateur annotateur);
    boolean existsByDatasetAndAnnotateurIsNotNull(Dataset dataset);
    Tache findByAnnotateurIdAndDatasetId(Long annotateurId, Long datasetId);
}
