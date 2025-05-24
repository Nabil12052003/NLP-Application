package com.NLP.service;

import com.NLP.Entity.Tache;
import java.util.List;

public interface TacheService {
    List<Tache> getTachesByAnnotateur(Long annotateurId);
}