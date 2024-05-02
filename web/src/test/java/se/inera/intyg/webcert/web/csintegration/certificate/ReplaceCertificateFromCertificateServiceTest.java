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

package se.inera.intyg.webcert.web.csintegration.certificate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.facade.model.PersonId;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.ReplaceCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.util.PDLLogService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;

@ExtendWith(MockitoExtension.class)
class ReplaceCertificateFromCertificateServiceTest {

    private static final ReplaceCertificateRequestDTO REQUEST = ReplaceCertificateRequestDTO.builder().build();
    private static final String PATIENT_ID = "PATIENT_ID";
    private static final String ID = "ID";
    private static final String NEW_ID = "NEW_ID";
    private static final Certificate CERTIFICATE = new Certificate();
    private static final Certificate REPLACED_CERTIFICATE = new Certificate();
    private static final String TYPE = "TYPE";

    @Mock
    CSIntegrationService csIntegrationService;

    @Mock
    CSIntegrationRequestFactory csIntegrationRequestFactory;

    @Mock
    PDLLogService pdlLogService;

    @Mock
    MonitoringLogService monitoringLogService;

    @InjectMocks
    ReplaceCertificateFromCertificateService replaceCertificateFromCertificateService;

    @Test
    void shouldReturnNullIfCertificateDoesNotExistInCS() {
        final var response = replaceCertificateFromCertificateService.replaceCertificate(ID);

        assertNull(response);
    }

    @Nested
    class CertificateExistsInCS {

        @BeforeEach
        void setup() {
            CERTIFICATE.setMetadata(CertificateMetadata.builder()
                .id(ID)
                .type(TYPE)
                .patient(Patient.builder()
                    .personId(
                        PersonId.builder()
                            .id(PATIENT_ID)
                            .build()
                    )
                    .build()
                )
                .build());

            REPLACED_CERTIFICATE.setMetadata(CertificateMetadata.builder()
                .id(NEW_ID)
                .type(TYPE)
                .patient(Patient.builder()
                    .personId(
                        PersonId.builder()
                            .id(PATIENT_ID)
                            .build()
                    )
                    .build()
                )
                .build());

            when(csIntegrationService.getCertificate(anyString(), any()))
                .thenReturn(CERTIFICATE);

            when(csIntegrationRequestFactory.replaceCertificateRequest(PATIENT_ID))
                .thenReturn(REQUEST);
        }

        @Nested
        class CertificateIsReplacedFromCS {

            @BeforeEach
            void setup() {
                when(csIntegrationService.replaceCertificate(ID, REQUEST))
                    .thenReturn(REPLACED_CERTIFICATE);
            }

            @Test
            void shouldReturnNullIfCertificateIdIfExistInCS() {
                final var response = replaceCertificateFromCertificateService.replaceCertificate(ID);

                assertEquals(NEW_ID, response);
            }

            @Test
            void shouldCallReplaceWithId() {
                final var captor = ArgumentCaptor.forClass(String.class);
                replaceCertificateFromCertificateService.replaceCertificate(ID);

                verify(csIntegrationService).replaceCertificate(captor.capture(), any(ReplaceCertificateRequestDTO.class));
                assertEquals(ID, captor.getValue());
            }

            @Test
            void shouldCallReplaceWithRequest() {
                final var captor = ArgumentCaptor.forClass(ReplaceCertificateRequestDTO.class);
                replaceCertificateFromCertificateService.replaceCertificate(ID);

                verify(csIntegrationService).replaceCertificate(anyString(), captor.capture());
                assertEquals(REQUEST, captor.getValue());
            }

            @Test
            void shouldPdlLogCreated() {
                replaceCertificateFromCertificateService.replaceCertificate(ID);
                verify(pdlLogService).logCreated(REPLACED_CERTIFICATE);

            }

            @Test
            void shouldMonitorLogReplace() {
                replaceCertificateFromCertificateService.replaceCertificate(ID);
                verify(monitoringLogService).logIntygCopiedReplacement(NEW_ID, ID);
            }
        }
    }

}