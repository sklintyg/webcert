package se.inera.intyg.webcert.web.service.relation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepositoryCustom;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by eriklupander on 2017-05-15.
 */
@Service
public class CertificateRelationServiceImpl implements CertificateRelationService {

    @Autowired
    private UtkastRepositoryCustom utkastRepoCustom;

    @Override
    public Relations getRelations(String intygsId) {
        Relations relations = new Relations();
        Optional<WebcertCertificateRelation> parentRelation = findParentRelation(intygsId);
        parentRelation.ifPresent(relations::setParent);

        List<WebcertCertificateRelation> childRelations = findChildRelations(intygsId);
        relations.getChildren().addAll(childRelations);

        return relations;
    }

    /**
     * Implementation detail: Spring JPA Repository @Query doesn't really work with getSingleResult()-style queries,
     * thus this method gets a list of "parentRelations" even though there can never be more than one parent.
     */
    @Override
    public Optional<WebcertCertificateRelation> findParentRelation(String intygsId) {
        List<WebcertCertificateRelation> parentRelations = utkastRepoCustom.findParentRelation(intygsId);
        return parentRelations.stream().findFirst();
    }

    @Override
    public List<WebcertCertificateRelation> findChildRelations(String intygsId) {
        return utkastRepoCustom.findChildRelations(intygsId)
                .stream()
                .sorted((r1, r2) -> r2.getSkapad().compareTo(r1.getSkapad()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<WebcertCertificateRelation> getRelationOfType(String intygsId, RelationKod relationKod) {
        return findChildRelations(intygsId).stream()
                .filter(cr -> cr.getRelationKod() == relationKod)
                .sorted((cr1, cr2) -> cr2.getSkapad().compareTo(cr1.getSkapad()))
                .findFirst();
    }
}
