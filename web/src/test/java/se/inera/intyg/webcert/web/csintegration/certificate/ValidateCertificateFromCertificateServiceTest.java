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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.modules.support.facade.dto.ValidationErrorDTO;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.ValidateCertificateRequestDTO;

@ExtendWith(MockitoExtension.class)
class ValidateCertificateFromCertificateServiceTest {

    @Mock
    CSIntegrationService csIntegrationService;
    @Mock
    CSIntegrationRequestFactory csIntegrationRequestFactory;
    @InjectMocks
    ValidateCertificateFromCertificateService validateCertificateService;

    private static final String CERTIFICATE_ID = "ID";
    private static final Certificate CERTIFICATE = new Certificate();
    private static final ValidationErrorDTO[] VALIDATION_ERRORS = {new ValidationErrorDTO()};
    private static final ValidateCertificateRequestDTO REQUEST = ValidateCertificateRequestDTO.builder().build();

    static {
        CERTIFICATE.setMetadata(
            CertificateMetadata.builder()
                .id(CERTIFICATE_ID)
                .build()
        );
    }

    @Test
    void shouldReturnNullIfCertificateDoesNotExistInCS() {
        assertNull(
            validateCertificateService.validate(CERTIFICATE)
        );
    }

    @Nested
    class CertificateServiceHasCertificate {

        @BeforeEach
        void setup() {
            when(csIntegrationService.certificateExists(CERTIFICATE_ID))
                .thenReturn(true);
            when(csIntegrationService.validateCertificate(REQUEST))
                .thenReturn(VALIDATION_ERRORS);
            when(csIntegrationRequestFactory.getValidateCertificateRequest(CERTIFICATE))
                .thenReturn(REQUEST);
        }

        @Test
        void shouldReturnValidationErrors() {
            assertEquals(VALIDATION_ERRORS, validateCertificateService.validate(CERTIFICATE)
            );
        }
    }

}
