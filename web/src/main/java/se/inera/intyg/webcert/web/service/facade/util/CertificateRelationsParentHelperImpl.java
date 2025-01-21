/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
