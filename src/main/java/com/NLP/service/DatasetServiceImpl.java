package com.NLP.service;

import com.NLP.Entity.*;
import com.NLP.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DatasetServiceImpl implements DatasetService {
    @Autowired private DatasetRepository datasetRepository;
    @Autowired private UtilisateurRepository utilisateurRepository;
    @Autowired private UtilisateurService utilisateurService;
    @Autowired private TacheRepository tacheRepository;
    @Autowired private ClassesPossiblesRepository classesPossiblesRepository;
    @Autowired private CoupleTexteRepository coupleTexteRepository;
    @Autowired private AnnotationRepository annotationRepository;

    public List<Dataset> getAllDatasets() {
        return datasetRepository.findAll();
    }

    public Dataset createDataset(String nomDataset, String description, String classes) {
        String normalizedName = nomDataset.trim().toLowerCase();
        Dataset existingDataset = datasetRepository.findByNomDatasetIgnoreCase(normalizedName);
        if (existingDataset != null) {
            throw new IllegalArgumentException("Nom de dataset déjà utilisé");
        }

        Dataset dataset = new Dataset();
        dataset.setNomDataset(nomDataset.trim());
        dataset.setDescription(description);
        dataset.setTaille(0);
        dataset.setAvancement(0.0);
        dataset = datasetRepository.save(dataset);

        String[] classesArray = classes.contains(";") ? classes.split(";") : classes.split(",");
        for (String classe : classesArray) {
            ClassesPossibles cp = new ClassesPossibles();
            cp.setTexteClass(classe.trim());
            cp.setDataset(dataset);
            classesPossiblesRepository.save(cp);
        }
        return dataset;
    }

    public Page<CoupleTexte> getCoupleTextesByDatasetId(Long datasetId, int page, int size) {
        return coupleTexteRepository.findByDatasetId(datasetId, PageRequest.of(page, size));
    }

    public List<Utilisateur> getAnnotatorsByDatasetId(Long datasetId) {
        List<Tache> taches = tacheRepository.findByDatasetId(datasetId);
        return taches.stream()
                .map(Tache::getAnnotateur)
                .filter(annotateur -> annotateur != null)
                .distinct()
                .collect(Collectors.toList());
    }

    public void assignAnnotators(Long datasetId, List<Long> annotateurIds, LocalDate dateLimite) {
        Dataset dataset = datasetRepository.findById(datasetId)
                .orElseThrow(() -> new IllegalArgumentException("Dataset non trouvé"));
        List<CoupleTexte> couples = coupleTexteRepository.findByDatasetId(datasetId);
        if (couples.isEmpty()) {
            throw new IllegalArgumentException("Aucun couple de texte à assigner");
        }

        Collections.shuffle(couples);
        int totalCouples = couples.size();
        int annotatorsCount = annotateurIds.size();
        if (annotatorsCount == 0) {
            throw new IllegalArgumentException("Aucun annotateur sélectionné");
        }
        int couplesPerAnnotator = totalCouples / annotatorsCount;
        int remainingCouples = totalCouples % annotatorsCount;

        int startIndex = 0;
        for (int i = 0; i < annotatorsCount; i++) {
            int currentCouplesCount = couplesPerAnnotator + (i < remainingCouples ? 1 : 0);
            if (startIndex + currentCouplesCount > totalCouples) break;
            List<CoupleTexte> assignedCouples = couples.subList(startIndex, startIndex + currentCouplesCount);

            Long annotateurId = annotateurIds.get(i);
            Utilisateur utilisateur = utilisateurRepository.findById(annotateurId)
                    .orElseThrow(() -> new IllegalArgumentException("Annotateur non trouvé"));
            if (utilisateur instanceof Annotateur) {
                Tache tache = tacheRepository.findByAnnotateurIdAndDatasetId(annotateurId, datasetId);
                if (tache == null) {
                    tache = new Tache();
                    tache.setDataset(dataset);
                    tache.setAnnotateur((Annotateur) utilisateur);
                }
                tache.setDateLimite(dateLimite);
                tache.setAssignedCouples(new ArrayList<>(assignedCouples));
                tache.setAvancement(0.0);
                tache.setTaille(assignedCouples.size());
                tacheRepository.save(tache);
            }
            startIndex += currentCouplesCount;
        }

        dataset.setTaille(totalCouples);
        dataset.setAvancement(0.0);
        datasetRepository.save(dataset);
    }

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

    public List<ClassesPossibles> getClassesByDatasetId(Long datasetId) {
        return classesPossiblesRepository.findByDatasetId(datasetId);
    }

    public List<Tache> getTachesByAnnotateur(Long annotateurId) {
        return tacheRepository.findByAnnotateurId(annotateurId);
    }

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

        updateDatasetAvancement(tache.getDataset());
    }

    private void updateDatasetAvancement(Dataset dataset) {
        if (dataset == null) {
            return;
        }
        List<Tache> taches = tacheRepository.findByDataset(dataset);
        if (taches.isEmpty()) {
            dataset.setAvancement(0.0);
        } else {
            double totalAvancement = taches.stream()
                    .mapToDouble(Tache::getAvancement)
                    .average()
                    .orElse(0.0);
            dataset.setAvancement(Math.min(totalAvancement, 100.0));
        }
        datasetRepository.save(dataset);
    }

    public Optional<Dataset> findById(Long id) {
        return datasetRepository.findById(id);
    }

    public List<Annotateur> getAnnotatorsForDataset(Long datasetId) {
        Dataset dataset = findById(datasetId)
                .orElseThrow(() -> new IllegalArgumentException("Dataset non trouvé avec l'ID " + datasetId));
        List<Tache> taches = tacheRepository.findByDataset(dataset);
        return taches.stream()
                .map(Tache::getAnnotateur)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    public List<Annotateur> getAvailableAnnotators(Dataset dataset, Annotateur excludedAnnotator) {
        List<Utilisateur> allUsers = utilisateurService.findAll();
        List<Annotateur> allAnnotators = allUsers.stream()
                .filter(user -> user instanceof Annotateur)
                .map(user -> (Annotateur) user)
                .collect(Collectors.toList());

        List<Annotateur> assignedAnnotators = getAnnotatorsForDataset(dataset.getId());

        return allAnnotators.stream()
                .filter(annotator -> !assignedAnnotators.contains(annotator))
                .filter(annotator -> !annotator.getId().equals(excludedAnnotator.getId()))
                .collect(Collectors.toList());
    }
}