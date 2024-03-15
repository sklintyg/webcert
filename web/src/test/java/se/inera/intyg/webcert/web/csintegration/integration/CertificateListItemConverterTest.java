/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.csintegration.integration;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.facade.model.PersonId;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;

@ExtendWith(MockitoExtension.class)
class CertificateListItemConverterTest {

    private static final Certificate CERTIFICATE = new Certificate();

    private static final CertificateMetadata CERTIFICATE_METADATA = CertificateMetadata.builder()
        .typeName("certificate type name")
        .status(CertificateStatus.SIGNED)
        .created(LocalDateTime.now())
        .patient(Patient.builder()
            .personId(PersonId.builder()
                .id("191212121212")
                .build())
            .build())
        .id("certificateId")
        .build();

    @InjectMocks
    CertificateListItemConverter certificateListItemConverter;

    @BeforeEach
    void setUp() {
        CERTIFICATE.setMetadata(CERTIFICATE_METADATA);

    }

    @Test
    void listItemsShouldContainTypeName() {
        final var response = certificateListItemConverter.convert(CERTIFICATE);
        assertTrue(response.getValues().containsValue(CERTIFICATE.getMetadata().getTypeName()));
    }

    @Test
    void listItemsShouldContainStatus() {
        final var response = certificateListItemConverter.convert(CERTIFICATE);
        assertTrue(response.getValues().containsValue(CERTIFICATE.getMetadata().getStatus()));
    }

    @Test
    void listItemsShouldContainSigned() {
        final var response = certificateListItemConverter.convert(CERTIFICATE);
        assertTrue(response.getValues().containsValue(CERTIFICATE.getMetadata().getCreated()));
    }

    @Test
    void listItemsShouldContainPatientId() {
        final var response = certificateListItemConverter.convert(CERTIFICATE);
        assertTrue(response.getValues().containsValue(CERTIFICATE.getMetadata().getPatient().getPersonId()));
    }

    @Test
    void listItemsShouldContainCertificateId() {
        final var response = certificateListItemConverter.convert(CERTIFICATE);
        assertTrue(response.getValues().containsValue(CERTIFICATE.getMetadata().getId()));
    }

}