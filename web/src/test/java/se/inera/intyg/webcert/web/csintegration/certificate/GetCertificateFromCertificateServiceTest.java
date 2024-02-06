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
import static org.mockito.Mockito.times;
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
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.unit.CertificateServiceUnitDTO;
import se.inera.intyg.webcert.web.csintegration.unit.CertificateServiceUnitHelper;
import se.inera.intyg.webcert.web.csintegration.user.CertificateServiceUserDTO;
import se.inera.intyg.webcert.web.csintegration.user.CertificateServiceUserHelper;
import se.inera.intyg.webcert.web.csintegration.util.PDLLogService;

@ExtendWith(MockitoExtension.class)
class GetCertificateFromCertificateServiceTest {

    private static final CertificateServiceUserDTO USER = CertificateServiceUserDTO.builder().build();
    private static final CertificateServiceUnitDTO UNIT = CertificateServiceUnitDTO.builder().build();
    private static final String PATIENT_ID = "191212121212";
    public static final String ID = "ID";

    private static Certificate certificate;

    @Mock
    CertificateServiceUnitHelper certificateServiceUnitHelper;

    @Mock
    CertificateServiceUserHelper certificateServiceUserHelper;

    @Mock
    CSIntegrationService csIntegrationService;

    @Mock
    PDLLogService pdlLogService;

    @InjectMocks
    GetCertificateFromCertificateService getCertificateFromCertificateService;

    @Test
    void shouldReturnNullIfCertificateDoesNotExistInCS() {
        final var response = getCertificateFromCertificateService.getCertificate(ID, true, true);

        assertNull(response);
    }

    @Test
    void shouldNotPerformPDLLogIfTypeWasNotRetrievedFromCS() {
        getCertificateFromCertificateService.getCertificate(ID, true, true);

        verify(pdlLogService, times(0)).logRead(PATIENT_ID, ID);
    }

    @Nested
    class CertificateServiceHasCertificate {

        @BeforeEach
        void setup() {
            certificate = new Certificate();
            final var metadata = CertificateMetadata.builder()
                .id(ID)
                .patient(
                    Patient.builder()
                        .personId(
                            PersonId.builder()
                                .id(PATIENT_ID)
                                .build()
                        )
                        .build()
                )
                .build();
            certificate.setMetadata(metadata);

            when(csIntegrationService.certificateExists(ID))
                .thenReturn(true);
            when(csIntegrationService.getCertificate(anyString(), any(GetCertificateRequestDTO.class)))
                .thenReturn(certificate);
        }

        @Test
        void shouldReturnCertificate() {
            final var response = getCertificateFromCertificateService.getCertificate(ID, true, true);

            assertEquals(certificate, response);
        }

        @Test
        void shouldPerformPDLForCreateCertificateUsingPatientIdFromResponse() {
            getCertificateFromCertificateService.getCertificate(ID, true, true);

            verify(pdlLogService, times(1)).logRead(PATIENT_ID, ID);
        }

        @Test
        void shouldNotPerformPDLIfBooleanIsFalse() {
            getCertificateFromCertificateService.getCertificate(ID, false, true);

            verify(pdlLogService, times(0)).logRead(PATIENT_ID, ID);
        }

        @Nested
        class Request {

            @BeforeEach
            void setup() {
                when(certificateServiceUserHelper.get())
                    .thenReturn(USER);
            }

            @Test
            void shouldSendId() {
                final var captor = ArgumentCaptor.forClass(String.class);

                getCertificateFromCertificateService.getCertificate(ID, true, true);
                verify(csIntegrationService).getCertificate(captor.capture(), any(GetCertificateRequestDTO.class));

                assertEquals(ID, captor.getValue());
            }

            @Test
            void shouldSetUser() {
                final var captor = ArgumentCaptor.forClass(GetCertificateRequestDTO.class);

                getCertificateFromCertificateService.getCertificate(ID, true, true);
                verify(csIntegrationService).getCertificate(anyString(), captor.capture());

                assertEquals(USER, captor.getValue().getUser());
            }

            @Test
            void shouldSetUnit() {
                final var captor = ArgumentCaptor.forClass(GetCertificateRequestDTO.class);

                when(certificateServiceUnitHelper.getUnit())
                    .thenReturn(UNIT);

                getCertificateFromCertificateService.getCertificate(ID, true, true);
                verify(csIntegrationService).getCertificate(anyString(), captor.capture());

                assertEquals(UNIT, captor.getValue().getUnit());
            }

            @Test
            void shouldSetCareUnit() {
                final var captor = ArgumentCaptor.forClass(GetCertificateRequestDTO.class);
                when(certificateServiceUnitHelper.getCareUnit())
                    .thenReturn(UNIT);

                getCertificateFromCertificateService.getCertificate(ID, true, true);
                verify(csIntegrationService).getCertificate(anyString(), captor.capture());

                assertEquals(UNIT, captor.getValue().getCareUnit());
            }

            @Test
            void shouldSetCareProvider() {
                final var captor = ArgumentCaptor.forClass(GetCertificateRequestDTO.class);
                when(certificateServiceUnitHelper.getCareProvider())
                    .thenReturn(UNIT);

                getCertificateFromCertificateService.getCertificate(ID, true, true);
                verify(csIntegrationService).getCertificate(anyString(), captor.capture());

                assertEquals(UNIT, captor.getValue().getCareProvider());
            }
        }
    }
}