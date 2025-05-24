package com.NLP.service;

import com.NLP.Entity.Annotation;
import com.NLP.Entity.CoupleTexte;
import com.NLP.Entity.Dataset;
import com.NLP.repository.AnnotationRepository;
import com.NLP.repository.DatasetRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExportServiceImpl implements ExportService {
    @Autowired private DatasetRepository datasetRepository;
    @Autowired private AnnotationRepository annotationRepository;

    public String exportDataset(Long datasetId) {
        Dataset dataset = datasetRepository.findById(datasetId)
                .orElseThrow(() -> new IllegalArgumentException("Dataset non trouvé"));
        List<CoupleTexte> couples = dataset.getCouples();
        List<Annotation> annotations = annotationRepository.findByCoupleTexte_DatasetId(datasetId);

        StringBuilder csv = new StringBuilder();
        csv.append("Text1,Text2,Class\n");
        for (CoupleTexte couple : couples) {
            String annotationClass = annotations.stream()
                    .filter(a -> a.getCoupleTexte().getId().equals(couple.getId()))
                    .map(Annotation::getClasse)
                    .findFirst()
                    .orElse("");
            csv.append(String.format("\"%s\",\"%s\",\"%s\"\n", couple.getText1(), couple.getText2(), annotationClass));
        }
        return csv.toString();
    }
}