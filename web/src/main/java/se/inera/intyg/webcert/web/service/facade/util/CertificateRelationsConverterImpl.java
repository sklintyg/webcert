package se.inera.intyg.webcert.web.service.facade.util;

import static se.inera.intyg.webcert.web.service.facade.util.CertificateStatusConverter.getStatus;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
import se.inera.intyg.webcert.web.service.relation.CertificateRelationService;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;

@Component
public class CertificateRelationsConverterImpl implements CertificateRelationsConverter {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateRelationsConverterImpl.class);

    private final CertificateRelationService certificateRelationService;

    @Autowired
    public CertificateRelationsConverterImpl(CertificateRelationService certificateRelationService) {
        this.certificateRelationService = certificateRelationService;
    }

    @Override
    public CertificateRelations convert(String certificateId) {
        LOG.debug("Retrieving relations for certificate");
        final var relations = certificateRelationService.getRelations(certificateId);

        LOG.debug("Converting relations for certificate");
        return CertificateRelations.builder()
            .parent(
                getRelation(relations.getParent())
            )
            .children(
                getChildRelations(relations.getLatestChildRelations())
            )
            .build();
    }

    private CertificateRelation[] getChildRelations(Relations.FrontendRelations latestChildRelations) {
        final List<CertificateRelation> childRelations = new ArrayList<>();

        addRelationToListIfExists(childRelations, latestChildRelations.getReplacedByIntyg(), CertificateRelationType.REPLACED);
        addRelationToListIfExists(childRelations, latestChildRelations.getReplacedByUtkast(), CertificateRelationType.REPLACED);
        addRelationToListIfExists(childRelations, latestChildRelations.getComplementedByIntyg(), CertificateRelationType.COMPLEMENTED);
        addRelationToListIfExists(childRelations, latestChildRelations.getComplementedByUtkast(), CertificateRelationType.COMPLEMENTED);
        addRelationToListIfExists(childRelations, latestChildRelations.getUtkastCopy(), CertificateRelationType.COPIED);

        return childRelations.toArray(new CertificateRelation[0]);
    }

    private void addRelationToListIfExists(List<CertificateRelation> childRelations, WebcertCertificateRelation relation,
        CertificateRelationType relationType) {
        final var childRelation = getRelation(relation, relationType);

        if (childRelation != null) {
            childRelations.add(childRelation);
        }
    }

    private CertificateRelation getRelation(WebcertCertificateRelation relation) {
        if (relation == null) {
            return null;
        }
        return getRelation(relation, getType(relation.getRelationKod()));
    }

    private CertificateRelation getRelation(WebcertCertificateRelation relation, CertificateRelationType type) {
        if (relation == null) {
            return null;
        }

        return CertificateRelation.builder()
            .certificateId(relation.getIntygsId())
            .created(relation.getSkapad())
            .status(
                getStatus(relation.isMakulerat(), relation.getStatus())
            )
            .type(type)
            .build();
    }

    private CertificateRelationType getType(RelationKod relationCode) {
        switch (relationCode) {
            case ERSATT:
                return CertificateRelationType.REPLACED;
            case KOPIA:
                return CertificateRelationType.COPIED;
            case KOMPLT:
                return CertificateRelationType.COMPLEMENTED;
            case FRLANG:
                return CertificateRelationType.EXTENDED;
            default:
                throw new IllegalArgumentException("Cannot map the relation code: " + relationCode);
        }
    }
}
