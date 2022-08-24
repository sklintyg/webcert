package se.inera.intyg.webcert.web.service.facade.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepositoryCustom;
import se.inera.intyg.webcert.web.service.intyg.IntygService;

@Component
public class CertificateRelationsParentHelperImpl implements CertificateRelationsParentHelper {

    private final UtkastRepositoryCustom utkastRepoCustom;
    private final IntygService intygService;

    @Autowired
    public CertificateRelationsParentHelperImpl(UtkastRepositoryCustom utkastRepoCustom,
        IntygService intygService) {
        this.utkastRepoCustom = utkastRepoCustom;
        this.intygService = intygService;
    }

    @Override
    public WebcertCertificateRelation getParentFromITIfExists(String certificateId) {
        final var parent = getParent(certificateId);
        decorateParentWithDataFromIT(parent);
        return parent;
    }

    private WebcertCertificateRelation getParent(String certificateId) {
        final var relation = utkastRepoCustom.findParentRelationWhenParentMissing(certificateId)
            .stream().findFirst().orElseGet(() -> null);
        if (hasParentRelation(relation)) {
            return relation;
        }
        return null;
    }

    private boolean hasParentRelation(WebcertCertificateRelation parent) {
        return parent != null && parent.getIntygsId() != null;
    }

    private void decorateParentWithDataFromIT(WebcertCertificateRelation parent) {
        if (parent != null) {
            final var certificate = intygService.fetchIntygDataForInternalUse(parent.getIntygsId(), false);
            parent.setStatus(UtkastStatus.SIGNED);
            parent.setMakulerat(certificate.isRevoked());
        }
    }
}
