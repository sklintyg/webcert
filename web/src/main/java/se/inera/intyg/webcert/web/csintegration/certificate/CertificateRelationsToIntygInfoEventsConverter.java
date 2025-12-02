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

package se.inera.intyg.webcert.web.csintegration.certificate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.infra.intyginfo.dto.IntygInfoEvent;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.web.controller.internalapi.GetCertificateInternalServiceFromWC;

@RequiredArgsConstructor
@Component
public class CertificateRelationsToIntygInfoEventsConverter {

    private final CertificateRelationToIntygEventInfoConverter certificateRelationToIntygEventInfoConverter;
    private final CSIntegrationService csIntegrationService;
    private final GetCertificateInternalServiceFromWC getCertificateInternalServiceFromWC;

    public List<IntygInfoEvent> convert(Certificate certificate) {
        if (certificate.getMetadata().getRelations() == null) {
            return Collections.emptyList();
        }

        List<IntygInfoEvent> events = new ArrayList<>();

        if (certificate.getMetadata().getRelations().getParent() != null) {
            final var parentId = certificate.getMetadata().getRelations().getParent().getCertificateId();
            final var relatedCertificate = getRelatedCertificate(parentId);
            final var parentRelation = certificateRelationToIntygEventInfoConverter
                .convert(certificate.getMetadata().getRelations().getParent(), relatedCertificate, false);
            events.add(parentRelation);
        }

        final var childRelations = certificate.getMetadata().getRelations().getChildren();
        if (childRelations != null) {
            final var childEvents = Arrays.stream(childRelations)
                .map(this::convertChildRelation)
                .filter(Objects::nonNull)
                .toList();
            events.addAll(childEvents);
        }

        return events;
    }

    private Certificate getRelatedCertificate(String parentId) {
        if (Boolean.TRUE.equals(csIntegrationService.certificateExists(parentId))) {
            return csIntegrationService.getInternalCertificate(parentId);
        }
        return getCertificateInternalServiceFromWC.get(parentId, null).getCertificate();
    }

    private IntygInfoEvent convertChildRelation(CertificateRelation childRelation) {
        final var relatedCertificate = csIntegrationService.getInternalCertificate(
            childRelation.getCertificateId()
        );
        return certificateRelationToIntygEventInfoConverter.convert(childRelation, relatedCertificate, true);
    }
}
