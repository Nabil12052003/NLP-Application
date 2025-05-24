package com.NLP.service;

public interface AnnotationService {
    void saveAnnotation(Long tacheId, Long annotateurId, Long coupleId, String classe);
}
