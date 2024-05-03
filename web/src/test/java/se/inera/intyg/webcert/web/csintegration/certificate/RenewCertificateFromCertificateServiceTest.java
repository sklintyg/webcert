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
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.facade.model.PersonId;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.RenewCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.util.PDLLogService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;

@ExtendWith(MockitoExtension.class)
class RenewCertificateFromCertificateServiceTest {

    private static final RenewCertificateRequestDTO REQUEST = RenewCertificateRequestDTO.builder().build();
    private static final String EXTERNAL_REFERENCE = "EXTERNAL_REFERENCE";
    private static final String ALTERNATE_SSN = "ALTERNATE_SSN";
    private static final String PATIENT_ID = "PATIENT_ID";
    private static final String ID = "ID";
    private static final String NEW_ID = "NEW_ID";
    private static final Certificate CERTIFICATE = new Certificate();
    private static final Certificate RENEWD_CERTIFICATE = new Certificate();
    private static final String TYPE = "TYPE";

    @Mock
    CSIntegrationService csIntegrationService;

    @Mock
    CSIntegrationRequestFactory csIntegrationRequestFactory;

    @Mock
    PDLLogService pdlLogService;

    @Mock
    IntegratedUnitRegistryHelper integratedUnitRegistryHelper;

    @Mock
    MonitoringLogService monitoringLogService;

    @Mock
    WebCertUserService webCertUserService;

    @Mock
    IntegrationParameters parameters;

    @Mock
    WebCertUser user;

    @Mock
    PublishCertificateStatusUpdateService publishCertificateStatusUpdateService;

    @InjectMocks
    RenewCertificateFromCertificateService renewCertificateFromCertificateService;

    @Test
    void shouldReturnNullIfCertificateDoesNotExistInCS() {
        final var response = renewCertificateFromCertificateService.renewCertificate(ID);

        assertNull(response);
    }

    @Test
    void shouldThrowErrorIfCertificateReturnedFromCSIsNull() {
        when(csIntegrationService.certificateExists(anyString()))
            .thenReturn(true);

        assertThrows(IllegalStateException.class, () -> renewCertificateFromCertificateService.renewCertificate(ID));
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

            RENEWD_CERTIFICATE.setMetadata(CertificateMetadata.builder()
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

            when(csIntegrationService.certificateExists(anyString()))
                .thenReturn(true);

            when(csIntegrationService.getCertificate(anyString(), any()))
                .thenReturn(CERTIFICATE);

            when(csIntegrationRequestFactory.renewCertificateRequest(anyString(), anyString()))
                .thenReturn(REQUEST);
        }

        @Nested
        class CertificateIsRenewedFromCS {

            @BeforeEach
            void setup() {
                when(csIntegrationService.renewCertificate(ID, REQUEST))
                    .thenReturn(RENEWD_CERTIFICATE);

                when(parameters.getReference())
                    .thenReturn(EXTERNAL_REFERENCE);

                when(user.getParameters())
                    .thenReturn(parameters);

                when(webCertUserService.getUser())
                    .thenReturn(user);
            }

            @Test
            void shouldCallRequestFactoryWithExternalReferenceAndPatientIdIfAlternateSsnIsNotSet() {
                renewCertificateFromCertificateService.renewCertificate(ID);
                verify(csIntegrationRequestFactory).renewCertificateRequest(PATIENT_ID, EXTERNAL_REFERENCE);
            }

            @Test
            void shouldCallRequestFactoryWithAlternateSsnIsSet() {
                when(parameters.getAlternateSsn())
                    .thenReturn(ALTERNATE_SSN);
                renewCertificateFromCertificateService.renewCertificate(ID);
                verify(csIntegrationRequestFactory).renewCertificateRequest(ALTERNATE_SSN, EXTERNAL_REFERENCE);
            }

            @Test
            void shouldReturnNullIfCertificateIdIfExistInCS() {
                final var response = renewCertificateFromCertificateService.renewCertificate(ID);

                assertEquals(NEW_ID, response);
            }

            @Test
            void shouldCallRenewWithId() {
                final var captor = ArgumentCaptor.forClass(String.class);
                renewCertificateFromCertificateService.renewCertificate(ID);

                verify(csIntegrationService).renewCertificate(captor.capture(), any(RenewCertificateRequestDTO.class));
                assertEquals(ID, captor.getValue());
            }

            @Test
            void shouldCallRenewWithRequest() {
                final var captor = ArgumentCaptor.forClass(RenewCertificateRequestDTO.class);
                renewCertificateFromCertificateService.renewCertificate(ID);

                verify(csIntegrationService).renewCertificate(anyString(), captor.capture());
                assertEquals(REQUEST, captor.getValue());
            }

            @Test
            void shouldPdlLogCreated() {
                renewCertificateFromCertificateService.renewCertificate(ID);
                verify(pdlLogService).logCreated(RENEWD_CERTIFICATE);

            }

            @Test
            void shouldPublishCreated() {
                renewCertificateFromCertificateService.renewCertificate(ID);
                verify(publishCertificateStatusUpdateService).publish(RENEWD_CERTIFICATE, HandelsekodEnum.SKAPAT);
            }

            @Test
            void shouldMonitorLogRenew() {
                renewCertificateFromCertificateService.renewCertificate(ID);
                verify(monitoringLogService).logIntygCopiedRenewal(NEW_ID, ID);
            }

            @Test
            void shouldRegisterUnit() {
                renewCertificateFromCertificateService.renewCertificate(ID);
                verify(integratedUnitRegistryHelper).addUnitForCopy(CERTIFICATE, RENEWD_CERTIFICATE);
            }
        }
    }
}