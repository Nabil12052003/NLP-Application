package com.NLP.repository;


import com.NLP.Entity.ClassesPossibles;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClassesPossiblesRepository extends JpaRepository<ClassesPossibles, Long> {
    List<ClassesPossibles> findByDatasetId(Long datasetId);
}