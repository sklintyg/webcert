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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.facade.model.PersonId;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SaveCertificateRequestDTO;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;

@ExtendWith(MockitoExtension.class)
class CertificateDetailsUpdateServiceTest {

    private static final String CERTIFICATE_ID = "certificateId";
    private static final String CERTIFICATE_TYPE = "certificateType";
    private static final String TYPE_VERSION = "typeVersion";
    private static final String PERSON_ID = "191212121212";
    private static final String ALTERNATE_SSN = "alternateSsn";
    private static final String EXTERNAL_REFERENCE = "externalReference";
    private static final SaveCertificateRequestDTO SAVE_CERTIFICATE_REQUEST_DTO = SaveCertificateRequestDTO.builder().build();
    private Certificate certificate;
    private Certificate savedCertificate;
    private static final WebCertUser WEBCERT_USER = new WebCertUser();
    private static final Personnummer PERSONAL_NUMBER = Personnummer.createPersonnummer(PERSON_ID).orElseThrow();
    @Mock
    private PublishCertificateStatusUpdateService publishCertificateStatusUpdateService;
    @Mock
    private IntegrationParameters integrationParameters;
    @Mock
    private AlternateSsnEvaluator alternateSsnEvaluator;
    @Mock
    private MonitoringLogService monitoringLogService;
    @Mock
    private CSIntegrationService csIntegrationService;
    @Mock
    private CSIntegrationRequestFactory csIntegrationRequestFactory;
    @InjectMocks
    private CertificateDetailsUpdateService certificateDetailsUpdateService;


    @BeforeEach
    void setUp() {
        certificate = new Certificate();
        certificate.setMetadata(
            CertificateMetadata.builder()
                .id(CERTIFICATE_ID)
                .type(CERTIFICATE_TYPE)
                .typeVersion(TYPE_VERSION)
                .patient(
                    Patient.builder()
                        .personId(
                            PersonId.builder()
                                .id(PERSON_ID)
                                .build()
                        )
                        .build()
                )
                .build()
        );
        savedCertificate = new Certificate();
        savedCertificate.setMetadata(
            CertificateMetadata.builder()
                .id(CERTIFICATE_ID)
                .type(CERTIFICATE_TYPE)
                .typeVersion(TYPE_VERSION)
                .patient(
                    Patient.builder()
                        .personId(
                            PersonId.builder()
                                .id(ALTERNATE_SSN)
                                .build()
                        )
                        .build()
                )
                .build()
        );
        WEBCERT_USER.setParameters(integrationParameters);
    }

    @Nested
    class MonitorlogTest {

        @Test
        void shallMonitorlogIfPatientDetailsAreUpdated() {
            doReturn(true).when(alternateSsnEvaluator).shouldUpdate(certificate, WEBCERT_USER);
            doReturn(ALTERNATE_SSN).when(integrationParameters).getAlternateSsn();
            doReturn(SAVE_CERTIFICATE_REQUEST_DTO).when(csIntegrationRequestFactory).saveRequest(certificate, ALTERNATE_SSN, null);
            doReturn(savedCertificate).when(csIntegrationService).saveCertificate(SAVE_CERTIFICATE_REQUEST_DTO);

            certificateDetailsUpdateService.update(certificate, WEBCERT_USER, PERSONAL_NUMBER);

            verify(monitoringLogService).logUtkastPatientDetailsUpdated(CERTIFICATE_ID, CERTIFICATE_TYPE);
        }

        @Test
        void shallNotMonitorlogIfPatientDetailsAreNotUpdated() {
            doReturn(false).when(alternateSsnEvaluator).shouldUpdate(certificate, WEBCERT_USER);
            certificateDetailsUpdateService.update(certificate, WEBCERT_USER, PERSONAL_NUMBER);
            verifyNoInteractions(monitoringLogService);
        }

        @Test
        void shallSetBeforeAlternateSsnOnUser() {
            final var webCertUser = new WebCertUser();
            webCertUser.setParameters(new IntegrationParameters("", "", ALTERNATE_SSN,
                "", "", "", "", "", "", false,
                false, false, true, null, null));

            doReturn(true).when(alternateSsnEvaluator).shouldUpdate(certificate, webCertUser);
            doReturn(SAVE_CERTIFICATE_REQUEST_DTO).when(csIntegrationRequestFactory)
                .saveRequest(certificate, ALTERNATE_SSN, null);
            doReturn(savedCertificate).when(csIntegrationService).saveCertificate(SAVE_CERTIFICATE_REQUEST_DTO);

            certificateDetailsUpdateService.update(certificate, webCertUser, PERSONAL_NUMBER);
            assertEquals(PERSONAL_NUMBER.getOriginalPnr(), webCertUser.getParameters().getBeforeAlternateSsn());
        }

        @Test
        void shallSetBeforeAlternateSsnOnUserFromCertificate() {
            final var webCertUser = new WebCertUser();
            webCertUser.setParameters(new IntegrationParameters("", "", ALTERNATE_SSN,
                "", "", "", "", "", "", false,
                false, false, true, null, null));

            doReturn(true).when(alternateSsnEvaluator).shouldUpdate(certificate, webCertUser);
            doReturn(SAVE_CERTIFICATE_REQUEST_DTO).when(csIntegrationRequestFactory)
                .saveRequest(certificate, ALTERNATE_SSN, null);
            doReturn(savedCertificate).when(csIntegrationService).saveCertificate(SAVE_CERTIFICATE_REQUEST_DTO);

            certificateDetailsUpdateService.update(certificate, webCertUser, null);
            assertEquals(certificate.getMetadata().getPatient().getPersonId().getId(), webCertUser.getParameters().getBeforeAlternateSsn());
        }
    }

    @Nested
    class PublishEventTest {

        @Test
        void shallPublishStatusUpdate() {
            doReturn(true).when(alternateSsnEvaluator).shouldUpdate(certificate, WEBCERT_USER);
            doReturn(ALTERNATE_SSN).when(integrationParameters).getAlternateSsn();
            doReturn(SAVE_CERTIFICATE_REQUEST_DTO).when(csIntegrationRequestFactory).saveRequest(certificate, ALTERNATE_SSN, null);
            doReturn(savedCertificate).when(csIntegrationService).saveCertificate(SAVE_CERTIFICATE_REQUEST_DTO);

            certificateDetailsUpdateService.update(certificate, WEBCERT_USER, PERSONAL_NUMBER);
            verify(publishCertificateStatusUpdateService).publish(savedCertificate, HandelsekodEnum.ANDRAT);
        }

        @Test
        void shallNotPublishStatusUpdate() {
            doReturn(false).when(alternateSsnEvaluator).shouldUpdate(certificate, WEBCERT_USER);

            certificateDetailsUpdateService.update(certificate, WEBCERT_USER, PERSONAL_NUMBER);

            verifyNoInteractions(publishCertificateStatusUpdateService);
        }
    }

    @Nested
    class SaveCertificateTest {

        @Test
        void shallSaveCertificateIfPatientDetailsAreUpdated() {
            doReturn(ALTERNATE_SSN).when(integrationParameters).getAlternateSsn();
            doReturn(SAVE_CERTIFICATE_REQUEST_DTO).when(csIntegrationRequestFactory).saveRequest(certificate, ALTERNATE_SSN, null);
            doReturn(savedCertificate).when(csIntegrationService).saveCertificate(SAVE_CERTIFICATE_REQUEST_DTO);
            doReturn(true).when(alternateSsnEvaluator).shouldUpdate(certificate, WEBCERT_USER);

            certificateDetailsUpdateService.update(certificate, WEBCERT_USER, PERSONAL_NUMBER);
            verify(csIntegrationService).saveCertificate(SAVE_CERTIFICATE_REQUEST_DTO);
        }

        @Test
        void shallSaveCertificateIfExternalReferenceIsMissingOnCertificateAndProvidedInIntegrationParameters() {
            certificate.getMetadata().setExternalReference(null);
            final var saveCertificateRequestDTO = SaveCertificateRequestDTO.builder().build();

            doReturn(EXTERNAL_REFERENCE).when(integrationParameters).getReference();
            doReturn(saveCertificateRequestDTO).when(csIntegrationRequestFactory).saveRequest(certificate, PERSON_ID, EXTERNAL_REFERENCE);
            doReturn(false).when(alternateSsnEvaluator).shouldUpdate(certificate, WEBCERT_USER);

            certificateDetailsUpdateService.update(certificate, WEBCERT_USER, PERSONAL_NUMBER);
            verify(csIntegrationService).saveCertificate(saveCertificateRequestDTO);
        }

        @Test
        void shallNotSaveCertificateIfExternalReferenceIsNotMissingOnCertificate() {
            certificate.getMetadata().setExternalReference(EXTERNAL_REFERENCE);
            doReturn(false).when(alternateSsnEvaluator).shouldUpdate(certificate, WEBCERT_USER);

            certificateDetailsUpdateService.update(certificate, WEBCERT_USER, PERSONAL_NUMBER);
            verifyNoInteractions(csIntegrationService);
        }


        @Test
        void shallNotSaveCertificateIfExternalReferenceIsNull() {
            certificate.getMetadata().setExternalReference(null);
            doReturn(null).when(integrationParameters).getReference();
            doReturn(false).when(alternateSsnEvaluator).shouldUpdate(certificate, WEBCERT_USER);

            certificateDetailsUpdateService.update(certificate, WEBCERT_USER, PERSONAL_NUMBER);
            verifyNoInteractions(csIntegrationService);
        }

        @Test
        void shallNotSaveCertificateIfExternalReferenceIsEmpty() {
            certificate.getMetadata().setExternalReference(null);
            doReturn("").when(integrationParameters).getReference();
            doReturn(false).when(alternateSsnEvaluator).shouldUpdate(certificate, WEBCERT_USER);

            certificateDetailsUpdateService.update(certificate, WEBCERT_USER, PERSONAL_NUMBER);
            verifyNoInteractions(csIntegrationService);
        }
    }
}
