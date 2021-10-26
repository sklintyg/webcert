/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.notification_sender.notifications.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.web.service.access.DraftAccessService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.utkast.UtkastCandidateServiceImpl;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.UtkastCandidateMetaData;
import se.inera.intyg.webcert.web.web.controller.api.dto.CopyIntygRequest;

@ExtendWith(MockitoExtension.class)
class CreateCertificateFromCandidateFacadeServiceImplTest {

    @Mock
    private UtkastService utkastService;

    @Mock
    private IntygTextsService draftAccessServiceHelper;

    @Mock
    private UtkastCandidateServiceImpl utkastCandidateService;

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @Mock
    private PatientDetailsResolver patientDetailsResolver;

    @Mock
    private MonitoringLogService monitoringLogService;

    @Mock
    private DraftAccessService draftAccessService;

    @InjectMocks
    private CreateCertificateFromCandidateFacadeServiceImpl createCertificateFromCandidateFacadeService;

    private final static String CERTIFICATE_ID = "certificateId";
    private final static String CANDIDATE_ID = "candidateId";
    private final static String NEW_CERTIFICATE_ID = "renewCertificateId";
    private final static String CERTIFICATE_TYPE = "ag7804";
    private final static String CANDIDATE_TYPE = "lisjp";
    private final static String PATIENT_ID = "191212121212";
    private final static String LATEST_VERSION = "2.0";

    @BeforeEach
    void setup() {
        doReturn(createCertificate())
            .when(utkastService)
            .getDraft(eq(CERTIFICATE_ID), eq(Boolean.FALSE));

        doReturn(createCandidateCertificate())
            .when(utkastService)
            .getDraft(eq(CANDIDATE_ID), eq(Boolean.FALSE));

        final var patient = new Patient();
        patient.setPersonId(Personnummer.createPersonnummer(PATIENT_ID).get());
        doReturn(patient).when(patientDetailsResolver).resolvePatient(any(), any(), any());

        final var metadata = mock(UtkastCandidateMetaData.class);
        doReturn(CANDIDATE_ID).when(metadata).getIntygId();

        doReturn(metadata).when(utkastCandidateService).getCandidateMetaData(any(), any(), any(), any(), any());
    }

    @Nested
    class CreateCertificateFromCandidate {

        @Test
        void shallIncludeCertificateId() {

            createCertificateFromCandidateFacadeService.createCertificateFromCandidate(CERTIFICATE_ID);

            final var certificateIdArgumentCaptor = ArgumentCaptor.forClass(String.class);

            verify(utkastService).updateDraftFromCandidate(certificateIdArgumentCaptor.capture(), anyString(), any(Utkast.class));

            assertEquals(CERTIFICATE_ID, certificateIdArgumentCaptor.getValue());
        }

        @Test
        void shallIncludeNewCertificateType() {

            createCertificateFromCandidateFacadeService.createCertificateFromCandidate(CERTIFICATE_ID);

            final var certificateTypeArgumentCaptor = ArgumentCaptor.forClass(String.class);

            assertEquals(CERTIFICATE_TYPE, certificateTypeArgumentCaptor.getValue());
        }

        @Test
        void shallIncludePatientId() {

            createCertificateFromCandidateFacadeService.createCertificateFromCandidate(CERTIFICATE_ID);

            final var renewIntygRequestArgumentCaptor = ArgumentCaptor.forClass(CopyIntygRequest.class);

            assertEquals(PATIENT_ID, renewIntygRequestArgumentCaptor.getValue().getPatientPersonnummer().getPersonnummer());
        }

        @Test
        void shallIncludeCorrectLatestVersion() {

            createCertificateFromCandidateFacadeService.createCertificateFromCandidate(CERTIFICATE_ID);

            final var renewIntygRequestArgumentCaptor = ArgumentCaptor.forClass(CopyIntygRequest.class);

            assertEquals(PATIENT_ID, renewIntygRequestArgumentCaptor.getValue().getPatientPersonnummer().getPersonnummer());
        }

        @Test
        void shallReturnNewDraftId() {

            final var actualCertificateId = createCertificateFromCandidateFacadeService.createCertificateFromCandidate(CERTIFICATE_ID);

            assertEquals(NEW_CERTIFICATE_ID, actualCertificateId);
        }
    }

    private Utkast createCertificate() {
        final var draft = new Utkast();
        draft.setIntygsId(CERTIFICATE_ID);
        draft.setIntygsTyp(CERTIFICATE_TYPE);
        draft.setIntygTypeVersion("certificateTypeVersion");
        draft.setModel("draftJson");
        draft.setStatus(UtkastStatus.SIGNED);
        draft.setSkapad(LocalDateTime.now());
        draft.setPatientPersonnummer(Personnummer.createPersonnummer(PATIENT_ID).orElseThrow());
        draft.setSkapadAv(new VardpersonReferens("personId", "personName"));
        return draft;
    }

    private Utkast createCandidateCertificate() {
        final var draft = new Utkast();
        draft.setIntygsId(CANDIDATE_ID);
        draft.setIntygsTyp(CANDIDATE_TYPE);
        draft.setIntygTypeVersion("certificateTypeVersion");
        draft.setModel("draftJson");
        draft.setStatus(UtkastStatus.SIGNED);
        draft.setSkapad(LocalDateTime.now());
        draft.setPatientPersonnummer(Personnummer.createPersonnummer(PATIENT_ID).orElseThrow());
        draft.setSkapadAv(new VardpersonReferens("personId", "personName"));
        return draft;
    }
}