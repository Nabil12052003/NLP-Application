package com.NLP.repository;


import com.NLP.Entity.Dataset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DatasetRepository extends JpaRepository<Dataset, Long> {
    @Query("SELECT d FROM Dataset d WHERE LOWER(d.nomDataset) = LOWER(:nomDataset)")
    Dataset findByNomDatasetIgnoreCase(@Param("nomDataset") String nomDataset);

    // Si vous avez déjà une méthode findByNomDataset, vous pouvez la garder
    Optional<Dataset> findByNomDataset(String nomDataset);
}
