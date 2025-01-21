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
package se.inera.intyg.webcert.web.service.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

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
import se.inera.intyg.webcert.web.service.facade.GetCertificateFacadeService;
import se.inera.intyg.webcert.web.service.utkast.CopyUtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateRenewalCopyRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateRenewalCopyResponse;
import se.inera.intyg.webcert.web.service.utkast.util.CopyUtkastServiceHelper;
import se.inera.intyg.webcert.web.web.controller.api.dto.CopyIntygRequest;

@ExtendWith(MockitoExtension.class)
class RenewCertificateFacadeServiceImplTest {

    @Mock
    private CopyUtkastServiceHelper copyUtkastServiceHelper;

    @Mock
    private CopyUtkastService copyUtkastService;

    @Mock
    private GetCertificateFacadeService getCertificateFacadeService;

    @InjectMocks
    private RenewCertificateFacadeServiceImpl renewCertificateFacadeService;

    private final static String CERTIFICATE_ID = "certificateId";
    private final static String RENEW_CERTIFICATE_ID = "renewCertificateId";
    private final static String CERTIFICATE_TYPE = "certificateType";
    private final static String PATIENT_ID = "191212121212";
    private final static String RESERVE_ID = "19121212-121A";

    @Nested
    class RenewCertificate {

        @BeforeEach
        void before() {
            setup();
        }

        @Test
        void shallIncludeCertificateId() {

            renewCertificateFacadeService.renewCertificate(CERTIFICATE_ID);

            final var certificateIdArgumentCaptor = ArgumentCaptor.forClass(String.class);

            verify(copyUtkastServiceHelper)
                .createRenewalCopyRequest(certificateIdArgumentCaptor.capture(), anyString(), any(CopyIntygRequest.class));

            assertEquals(CERTIFICATE_ID, certificateIdArgumentCaptor.getValue());
        }

        @Test
        void shallIncludeCertificateType() {

            renewCertificateFacadeService.renewCertificate(CERTIFICATE_ID);

            final var certificateTypeArgumentCaptor = ArgumentCaptor.forClass(String.class);

            verify(copyUtkastServiceHelper)
                .createRenewalCopyRequest(anyString(), certificateTypeArgumentCaptor.capture(), any(CopyIntygRequest.class));

            assertEquals(CERTIFICATE_TYPE, certificateTypeArgumentCaptor.getValue());
        }

        @Test
        void shallIncludePatientId() {

            renewCertificateFacadeService.renewCertificate(CERTIFICATE_ID);

            final var renewIntygRequestArgumentCaptor = ArgumentCaptor.forClass(CopyIntygRequest.class);

            verify(copyUtkastServiceHelper).createRenewalCopyRequest(anyString(), anyString(), renewIntygRequestArgumentCaptor.capture());

            assertEquals(PATIENT_ID, renewIntygRequestArgumentCaptor.getValue().getPatientPersonnummer().getPersonnummer());
        }

        @Test
        void shallReturnNewDraftId() {

            final var actualCertificateId = renewCertificateFacadeService.renewCertificate(CERTIFICATE_ID);

            assertEquals(RENEW_CERTIFICATE_ID, actualCertificateId);
        }
    }

    @Nested
    class RenewCertificateWithReserveId {

        @BeforeEach
        void before() {
            final var certificate = setup();

            certificate.getMetadata().setPatient(
                Patient.builder()
                    .personId(
                        PersonId.builder()
                            .id(RESERVE_ID)
                            .build()
                    )
                    .previousPersonId(
                        PersonId.builder()
                            .id(PATIENT_ID)
                            .build()
                    )
                    .reserveId(true)
                    .build()
            );
        }

        @Test
        void shallIncludeCertificateId() {

            renewCertificateFacadeService.renewCertificate(CERTIFICATE_ID);

            final var certificateIdArgumentCaptor = ArgumentCaptor.forClass(String.class);

            verify(copyUtkastServiceHelper)
                .createRenewalCopyRequest(certificateIdArgumentCaptor.capture(), anyString(), any(CopyIntygRequest.class));

            assertEquals(CERTIFICATE_ID, certificateIdArgumentCaptor.getValue());
        }

        @Test
        void shallIncludeCertificateType() {

            renewCertificateFacadeService.renewCertificate(CERTIFICATE_ID);

            final var certificateTypeArgumentCaptor = ArgumentCaptor.forClass(String.class);

            verify(copyUtkastServiceHelper)
                .createRenewalCopyRequest(anyString(), certificateTypeArgumentCaptor.capture(), any(CopyIntygRequest.class));

            assertEquals(CERTIFICATE_TYPE, certificateTypeArgumentCaptor.getValue());
        }

        @Test
        void shallIncludePatientId() {

            renewCertificateFacadeService.renewCertificate(CERTIFICATE_ID);

            final var renewIntygRequestArgumentCaptor = ArgumentCaptor.forClass(CopyIntygRequest.class);

            verify(copyUtkastServiceHelper).createRenewalCopyRequest(anyString(), anyString(), renewIntygRequestArgumentCaptor.capture());

            assertEquals(PATIENT_ID, renewIntygRequestArgumentCaptor.getValue().getPatientPersonnummer().getPersonnummer());
        }

        @Test
        void shallReturnNewDraftId() {

            final var actualCertificateId = renewCertificateFacadeService.renewCertificate(CERTIFICATE_ID);

            assertEquals(RENEW_CERTIFICATE_ID, actualCertificateId);
        }
    }

    private Certificate setup() {
        final var certificate = createCertificate();

        doReturn(certificate)
            .when(getCertificateFacadeService)
            .getCertificate(CERTIFICATE_ID, Boolean.FALSE, true);

        final var serviceRequest = new CreateRenewalCopyRequest(
            CERTIFICATE_ID,
            CERTIFICATE_TYPE,
            null,
            null
        );

        doReturn(serviceRequest)
            .when(copyUtkastServiceHelper)
            .createRenewalCopyRequest(
                eq(CERTIFICATE_ID),
                eq(CERTIFICATE_TYPE),
                any(CopyIntygRequest.class)
            );

        final var serviceResponse = new CreateRenewalCopyResponse(
            CERTIFICATE_TYPE,
            "1.0",
            RENEW_CERTIFICATE_ID,
            CERTIFICATE_ID
        );

        doReturn(serviceResponse)
            .when(copyUtkastService)
            .createRenewalCopy(serviceRequest);

        return certificate;
    }

    private Certificate createCertificate() {
        final var certificate = new Certificate();
        certificate.setMetadata(
            CertificateMetadata.builder()
                .id(CERTIFICATE_ID)
                .type(CERTIFICATE_TYPE)
                .patient(
                    Patient.builder()
                        .personId(
                            PersonId.builder()
                                .id(PATIENT_ID)
                                .type("PERSON_NUMMER")
                                .build()
                        )
                        .build()
                )
                .build()
        );
        return certificate;
    }
}
