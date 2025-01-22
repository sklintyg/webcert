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
import se.inera.intyg.webcert.web.service.utkast.dto.CreateReplacementCopyRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateReplacementCopyResponse;
import se.inera.intyg.webcert.web.service.utkast.util.CopyUtkastServiceHelper;
import se.inera.intyg.webcert.web.web.controller.api.dto.CopyIntygRequest;

@ExtendWith(MockitoExtension.class)
class ReplaceCertificateFacadeServiceImplTest {

    @Mock
    private CopyUtkastServiceHelper copyUtkastServiceHelper;

    @Mock
    private CopyUtkastService copyUtkastService;

    @Mock
    private GetCertificateFacadeService getCertificateFacadeService;

    @InjectMocks
    private ReplaceCertificateFacadeServiceImpl replaceCertificateFacadeService;

    private final static String CERTIFICATE_ID = "certificateId";
    private final static String REPLACE_CERTIFICATE_ID = "copyCertificateId";
    private final static String CERTIFICATE_TYPE = "certificateType";
    private final static String PATIENT_ID = "191212121212";
    private final static String RESERVE_ID = "19121212-121A";

    @Nested
    class ReplaceCertificateWithPatientId {

        @BeforeEach
        void setup() {
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
                                    .build()
                            )
                            .build()
                    )
                    .build()
            );

            doReturn(certificate)
                .when(getCertificateFacadeService)
                .getCertificate(CERTIFICATE_ID, false, true);

            final var serviceRequest = new CreateReplacementCopyRequest(
                CERTIFICATE_ID,
                CERTIFICATE_TYPE,
                null,
                null
            );

            doReturn(serviceRequest)
                .when(copyUtkastServiceHelper)
                .createReplacementCopyRequest(
                    eq(CERTIFICATE_ID),
                    eq(CERTIFICATE_TYPE),
                    any(CopyIntygRequest.class)
                );

            final var serviceResponse = new CreateReplacementCopyResponse(
                CERTIFICATE_TYPE,
                "1.0",
                REPLACE_CERTIFICATE_ID,
                CERTIFICATE_ID
            );

            doReturn(serviceResponse)
                .when(copyUtkastService)
                .createReplacementCopy(serviceRequest);
        }

        @Test
        void shallIncludePatientId() {

            replaceCertificateFacadeService.replaceCertificate(CERTIFICATE_ID);

            final var replaceIntygRequestArgumentCaptor = ArgumentCaptor.forClass(CopyIntygRequest.class);

            verify(copyUtkastServiceHelper).createReplacementCopyRequest(anyString(), anyString(),
                replaceIntygRequestArgumentCaptor.capture());

            assertEquals(PATIENT_ID, replaceIntygRequestArgumentCaptor.getValue().getPatientPersonnummer().getPersonnummer());
        }

        @Test
        void shallReturnNewDraftId() {

            final var actualCertificateId = replaceCertificateFacadeService.replaceCertificate(CERTIFICATE_ID);

            assertEquals(REPLACE_CERTIFICATE_ID, actualCertificateId);
        }
    }

    @Nested
    class ReplaceCertificateWithReserveId {

        @BeforeEach
        void setup() {
            final var certificate = new Certificate();
            certificate.setMetadata(
                CertificateMetadata.builder()
                    .id(CERTIFICATE_ID)
                    .type(CERTIFICATE_TYPE)
                    .patient(
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
                    )
                    .build()
            );

            doReturn(certificate)
                .when(getCertificateFacadeService)
                .getCertificate(CERTIFICATE_ID, false, true);

            final var serviceRequest = new CreateReplacementCopyRequest(
                CERTIFICATE_ID,
                CERTIFICATE_TYPE,
                null,
                null
            );

            doReturn(serviceRequest)
                .when(copyUtkastServiceHelper)
                .createReplacementCopyRequest(
                    eq(CERTIFICATE_ID),
                    eq(CERTIFICATE_TYPE),
                    any(CopyIntygRequest.class)
                );

            final var serviceResponse = new CreateReplacementCopyResponse(
                CERTIFICATE_TYPE,
                "1.0",
                REPLACE_CERTIFICATE_ID,
                CERTIFICATE_ID
            );

            doReturn(serviceResponse)
                .when(copyUtkastService)
                .createReplacementCopy(serviceRequest);
        }

        @Test
        void shallIncludePatientId() {

            replaceCertificateFacadeService.replaceCertificate(CERTIFICATE_ID);

            final var replaceIntygRequestArgumentCaptor = ArgumentCaptor.forClass(CopyIntygRequest.class);

            verify(copyUtkastServiceHelper).createReplacementCopyRequest(anyString(), anyString(),
                replaceIntygRequestArgumentCaptor.capture());

            assertEquals(PATIENT_ID, replaceIntygRequestArgumentCaptor.getValue().getPatientPersonnummer().getPersonnummer());
        }

        @Test
        void shallReturnNewDraftId() {

            final var actualCertificateId = replaceCertificateFacadeService.replaceCertificate(CERTIFICATE_ID);

            assertEquals(REPLACE_CERTIFICATE_ID, actualCertificateId);
        }
    }
}
