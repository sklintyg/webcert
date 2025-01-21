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
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.facade.GetCertificateFacadeService;
import se.inera.intyg.webcert.web.service.utkast.CopyUtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateCompletionCopyRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateCompletionCopyResponse;
import se.inera.intyg.webcert.web.service.utkast.util.CopyUtkastServiceHelper;
import se.inera.intyg.webcert.web.web.controller.api.dto.CopyIntygRequest;

@ExtendWith(MockitoExtension.class)
class ComplementCertificateFacadeServiceImplTest {

    @Mock
    private GetCertificateFacadeService getCertificateFacadeService;

    @Mock
    private ArendeService arendeService;

    @Mock
    private CopyUtkastServiceHelper copyUtkastServiceHelper;

    @Mock
    private CopyUtkastService copyUtkastService;

    @InjectMocks
    private ComplementCertificateFacadeServiceImpl complementCertificateFacadeService;

    @Nested
    class ComplementWithNewCertificate {

        private final String ORIGINAL_CERTIFICATE_ID = "ORIGINAL_CERTIFICATE_ID";
        private final String MESSAGE = "MESSAGE";
        private final String NEW_CERTIFICATE_ID = "NEW_CERTIFICATE_ID";
        private final String CERTIFICATE_TYPE = "CERTIFICATE_TYPE";
        private final String PERSON_ID = "191212121212";
        private final String LATEST_COMPLEMENT_QUESTION_ID = "LATEST_COMPLEMENT_QUESTION_ID";
        private ArgumentCaptor<CopyIntygRequest> copyIntygRequestArgumentCaptor;

        @BeforeEach
        void setUp() {
            final var originalCertificate = new Certificate();
            originalCertificate.setMetadata(
                CertificateMetadata.builder()
                    .id(ORIGINAL_CERTIFICATE_ID)
                    .type(CERTIFICATE_TYPE)
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

            doReturn(originalCertificate)
                .when(getCertificateFacadeService)
                .getCertificate(ORIGINAL_CERTIFICATE_ID, false, true); // Don't create PDL-LOG when retrieving orginal certificate

            doReturn(LATEST_COMPLEMENT_QUESTION_ID)
                .when(arendeService)
                .getLatestMeddelandeIdForCurrentCareUnit(ORIGINAL_CERTIFICATE_ID);

            copyIntygRequestArgumentCaptor = ArgumentCaptor.forClass(CopyIntygRequest.class);
            final var createCompletionCopyRequest = new CreateCompletionCopyRequest();
            doReturn(createCompletionCopyRequest)
                .when(copyUtkastServiceHelper)
                .createCompletionCopyRequest(
                    eq(ORIGINAL_CERTIFICATE_ID),
                    eq(CERTIFICATE_TYPE),
                    eq(LATEST_COMPLEMENT_QUESTION_ID),
                    copyIntygRequestArgumentCaptor.capture()
                );

            final var createCompletionCopyResponse = new CreateCompletionCopyResponse(
                CERTIFICATE_TYPE,
                CERTIFICATE_TYPE,
                NEW_CERTIFICATE_ID,
                ORIGINAL_CERTIFICATE_ID
            );

            doReturn(createCompletionCopyResponse)
                .when(copyUtkastService)
                .createCompletion(createCompletionCopyRequest);

            final var newCertificate = new Certificate();
            doReturn(newCertificate)
                .when(getCertificateFacadeService)
                .getCertificate(NEW_CERTIFICATE_ID, true, true); // Create PDL-log when retrieving new certificate
        }

        @Test
        void shallReturnNewCertificate() {

            final var complement = complementCertificateFacadeService.complement(ORIGINAL_CERTIFICATE_ID, MESSAGE);
            assertNotNull(complement, "Expect the complement certificate to be returned");
        }

        @Test
        void shallComplementWithExpectedPatientId() {
            final var complement = complementCertificateFacadeService.complement(ORIGINAL_CERTIFICATE_ID, MESSAGE);
            assertEquals(PERSON_ID, copyIntygRequestArgumentCaptor.getValue().getPatientPersonnummer().getPersonnummer());
        }

        @Test
        void shallComplementMessage() {
            final var complement = complementCertificateFacadeService.complement(ORIGINAL_CERTIFICATE_ID, MESSAGE);
            assertEquals(MESSAGE, copyIntygRequestArgumentCaptor.getValue().getKommentar());
        }
    }

    @Nested
    class ComplementWithAnswer {

        private final String CERTIFICATE_ID = "CERTIFICATE_ID";
        private final String MESSAGE = "MESSAGE";
        private ArgumentCaptor<String> certificateIdArgumentCaptor = ArgumentCaptor.forClass(String.class);
        private ArgumentCaptor<String> messageArgumentCaptor = ArgumentCaptor.forClass(String.class);

        @BeforeEach
        void setUp() {
            final var newCertificate = new Certificate();
            doReturn(newCertificate)
                .when(getCertificateFacadeService)
                .getCertificate(CERTIFICATE_ID, false, true); // No PDL-log when reading the same certificate that was complemented
        }

        @Test
        void shallReturnNewCertificate() {
            final var complement = complementCertificateFacadeService.answerComplement(CERTIFICATE_ID, MESSAGE);
            assertNotNull(complement, "Expect the complement certificate to be returned");
        }

        @Test
        void shallAnswerComplementWithCertificateId() {
            final var complement = complementCertificateFacadeService.answerComplement(CERTIFICATE_ID, MESSAGE);
            verify(arendeService).answerKomplettering(certificateIdArgumentCaptor.capture(), anyString());
            assertEquals(CERTIFICATE_ID, certificateIdArgumentCaptor.getValue());
        }

        @Test
        void shallAnswerComplementWithMessage() {
            final var complement = complementCertificateFacadeService.answerComplement(CERTIFICATE_ID, MESSAGE);
            verify(arendeService).answerKomplettering(anyString(), messageArgumentCaptor.capture());
            assertEquals(MESSAGE, messageArgumentCaptor.getValue());
        }
    }

    @Nested
    class ComplementWithReserveId {

        private final String ORIGINAL_CERTIFICATE_ID = "ORIGINAL_CERTIFICATE_ID";
        private final String MESSAGE = "MESSAGE";
        private final String NEW_CERTIFICATE_ID = "NEW_CERTIFICATE_ID";
        private final String CERTIFICATE_TYPE = "CERTIFICATE_TYPE";
        private final String PERSON_ID = "191212121212";
        private final String RESERVE_ID = "19121212-121A";
        private final String LATEST_COMPLEMENT_QUESTION_ID = "LATEST_COMPLEMENT_QUESTION_ID";
        private ArgumentCaptor<CopyIntygRequest> copyIntygRequestArgumentCaptor;

        @BeforeEach
        void setUp() {
            final var originalCertificate = new Certificate();
            originalCertificate.setMetadata(
                CertificateMetadata.builder()
                    .id(ORIGINAL_CERTIFICATE_ID)
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
                                    .id(PERSON_ID)
                                    .build()
                            )
                            .reserveId(true)
                            .build()
                    )
                    .build()
            );

            doReturn(originalCertificate)
                .when(getCertificateFacadeService)
                .getCertificate(ORIGINAL_CERTIFICATE_ID, false, true); // Don't create PDL-LOG when retrieving orginal certificate

            doReturn(LATEST_COMPLEMENT_QUESTION_ID)
                .when(arendeService)
                .getLatestMeddelandeIdForCurrentCareUnit(ORIGINAL_CERTIFICATE_ID);

            copyIntygRequestArgumentCaptor = ArgumentCaptor.forClass(CopyIntygRequest.class);
            final var createCompletionCopyRequest = new CreateCompletionCopyRequest();
            doReturn(createCompletionCopyRequest)
                .when(copyUtkastServiceHelper)
                .createCompletionCopyRequest(
                    eq(ORIGINAL_CERTIFICATE_ID),
                    eq(CERTIFICATE_TYPE),
                    eq(LATEST_COMPLEMENT_QUESTION_ID),
                    copyIntygRequestArgumentCaptor.capture()
                );

            final var createCompletionCopyResponse = new CreateCompletionCopyResponse(
                CERTIFICATE_TYPE,
                CERTIFICATE_TYPE,
                NEW_CERTIFICATE_ID,
                ORIGINAL_CERTIFICATE_ID
            );

            doReturn(createCompletionCopyResponse)
                .when(copyUtkastService)
                .createCompletion(createCompletionCopyRequest);

            final var newCertificate = new Certificate();
            doReturn(newCertificate)
                .when(getCertificateFacadeService)
                .getCertificate(NEW_CERTIFICATE_ID, true, true); // Create PDL-log when retrieving new certificate
        }

        @Test
        void shallComplementWithExpectedPatientId() {
            final var complement = complementCertificateFacadeService.complement(ORIGINAL_CERTIFICATE_ID, MESSAGE);
            assertEquals(PERSON_ID, copyIntygRequestArgumentCaptor.getValue().getPatientPersonnummer().getPersonnummer());
        }
    }
}
