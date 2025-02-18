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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import io.vavr.collection.Array;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.infra.intyginfo.dto.IntygInfoEvent;
import se.inera.intyg.infra.intyginfo.dto.IntygInfoEvent.Source;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;

@ExtendWith(MockitoExtension.class)
class CertificateRelationsToIntygInfoEventsConverterTest {

    @Mock
    private CertificateRelationToIntygEventInfoConverter certificateRelationToIntygEventInfoConverter;
    @Mock
    private CSIntegrationService csIntegrationService;

    @InjectMocks
    private CertificateRelationsToIntygInfoEventsConverter certificateRelationsToIntygInfoEventsConverter;

    @Test
    void shouldReturnEmptyListIfNoEvents() {
        final var certificate = new Certificate();
        final var metadata = CertificateMetadata.builder()
            .relations(
                CertificateRelations.builder()
                    .parent(null)
                    .children(new CertificateRelation[0])
                    .build()
            ).build();
        certificate.setMetadata(metadata);

        assertEquals(
            Collections.emptyList(),
            certificateRelationsToIntygInfoEventsConverter.convert(certificate)
        );
    }

    @Test
    void shouldReturnEmptyListIfNullEvents() {
        final var certificate = new Certificate();
        final var metadata = CertificateMetadata.builder()
            .relations(null)
            .build();
        certificate.setMetadata(metadata);

        assertEquals(
            Collections.emptyList(),
            certificateRelationsToIntygInfoEventsConverter.convert(certificate)
        );
    }

    @Test
    void shouldReturnEmptyListIfNullChildrenEvents() {
        final var certificate = new Certificate();
        final var metadata = CertificateMetadata.builder()
            .relations(
                CertificateRelations.builder()
                    .parent(null)
                    .children(null)
                    .build()
            )
            .build();
        certificate.setMetadata(metadata);

        assertEquals(
            Collections.emptyList(),
            certificateRelationsToIntygInfoEventsConverter.convert(certificate)
        );
    }

    @Test
    void shouldReturnChildRelation() {
        final var certificate = new Certificate();
        final var relatedCertificate = new Certificate();
        final var childRelation = CertificateRelation.builder().certificateId("ID").build();
        final var childEvent = new IntygInfoEvent(Source.WEBCERT);
        final var metadata = CertificateMetadata.builder()
            .id("ORIGINAL_ID")
            .relations(
                CertificateRelations.builder()
                    .parent(null)
                    .children(
                        Array.of(
                            childRelation
                        ).toJavaArray(CertificateRelation[]::new)
                    )
                    .build()
            )
            .build();
        certificate.setMetadata(metadata);

        when(csIntegrationService.getInternalCertificate("ID"))
            .thenReturn(relatedCertificate);
        when(certificateRelationToIntygEventInfoConverter.convert(childRelation, relatedCertificate, true))
            .thenReturn(childEvent);

        final var response = certificateRelationsToIntygInfoEventsConverter.convert(certificate);

        assertEquals(
            List.of(childEvent),
            response
        );
    }

    @Test
    void shouldReturnParentRelation() {
        final var certificate = new Certificate();
        final var parentRelatedCertificate = new Certificate();
        final var parentRelation = CertificateRelation.builder().certificateId("ID").build();
        final var parentEvent = new IntygInfoEvent(Source.WEBCERT);
        final var metadata = CertificateMetadata.builder()
            .id("ORIGINAL_ID")
            .relations(
                CertificateRelations.builder()
                    .parent(parentRelation)
                    .children(new CertificateRelation[0])
                    .build()
            )
            .build();
        certificate.setMetadata(metadata);
        when(csIntegrationService.getInternalCertificate("ID"))
            .thenReturn(parentRelatedCertificate);
        when(certificateRelationToIntygEventInfoConverter.convert(parentRelation, parentRelatedCertificate, false))
            .thenReturn(parentEvent);

        final var response = certificateRelationsToIntygInfoEventsConverter.convert(certificate);

        assertEquals(
            List.of(parentEvent),
            response
        );
    }

    @Test
    void shouldReturnChildAndParentRelation() {
        final var certificate = new Certificate();
        final var relatedCertificate = new Certificate();
        final var parentRelatedCertificate = new Certificate();
        final var childRelation = CertificateRelation.builder().certificateId("CHILD_ID").build();
        final var parentRelation = CertificateRelation.builder().certificateId("PARENT_ID").build();
        final var childEvent = new IntygInfoEvent(Source.WEBCERT);
        final var parentEvent = new IntygInfoEvent(Source.WEBCERT);
        final var metadata = CertificateMetadata.builder()
            .id("ORIGINAL_ID")
            .relations(
                CertificateRelations.builder()
                    .parent(parentRelation)
                    .children(
                        Array.of(
                            childRelation
                        ).toJavaArray(CertificateRelation[]::new)
                    )
                    .build()
            )
            .build();
        certificate.setMetadata(metadata);

        when(csIntegrationService.getInternalCertificate("CHILD_ID"))
            .thenReturn(relatedCertificate);
        when(csIntegrationService.getInternalCertificate("PARENT_ID"))
            .thenReturn(parentRelatedCertificate);
        when(certificateRelationToIntygEventInfoConverter.convert(childRelation, relatedCertificate, true))
            .thenReturn(childEvent);
        when(certificateRelationToIntygEventInfoConverter.convert(parentRelation, parentRelatedCertificate, false))
            .thenReturn(parentEvent);

        final var response = certificateRelationsToIntygInfoEventsConverter.convert(certificate);

        assertEquals(
            List.of(parentEvent, childEvent),
            response
        );
    }
}