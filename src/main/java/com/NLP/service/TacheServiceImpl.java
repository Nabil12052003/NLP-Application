package com.NLP.service;

import com.NLP.Entity.Tache;
import com.NLP.repository.TacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TacheServiceImpl implements TacheService {
    @Autowired private TacheRepository tacheRepository;

    public List<Tache> getTachesByAnnotateur(Long annotateurId) {
        return tacheRepository.findByAnnotateurId(annotateurId);
    }
}