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
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.notification_sender.notifications.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.access.DraftAccessServiceHelper;
import se.inera.intyg.webcert.web.service.facade.util.CandidateDataHelper;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.UtkastCandidateMetaData;

@ExtendWith(MockitoExtension.class)
class CreateCertificateFromCandidateFacadeServiceImplTest {

    @Mock
    private UtkastService utkastService;

    @Mock
    private DraftAccessServiceHelper draftAccessServiceHelper;

    @Mock
    private MonitoringLogService monitoringLogService;

    @Mock
    private CandidateDataHelper candidateDataHelper;

    @InjectMocks
    private CreateCertificateFromCandidateFacadeServiceImpl createCertificateFromCandidateFacadeService;

    private final static String CERTIFICATE_ID = "certificateId";
    private final static String CANDIDATE_ID = "candidateId";
    private final static String CERTIFICATE_TYPE = "ag7804";
    private final static String CANDIDATE_TYPE = "lisjp";
    private final static String PATIENT_ID = "191212121212";
    private final static String LATEST_VERSION = "2.0";

    @BeforeEach
    void setup() {
        doReturn(createCertificate())
            .when(utkastService)
            .getDraft(eq(CERTIFICATE_ID), eq(Boolean.FALSE));

        when(candidateDataHelper.getCandidateMetadata(anyString(), anyString(), any(Personnummer.class)))
            .thenReturn(Optional.of(createCandidateMetaData(CANDIDATE_ID, CANDIDATE_TYPE, LATEST_VERSION)));
    }

    @Nested
    class CreateCertificateFromCandidate {

        @Test
        void shallIncludeCertificateId() {

            createCertificateFromCandidateFacadeService.createCertificateFromCandidate(CERTIFICATE_ID);

            final var certificateIdArgumentCaptor = ArgumentCaptor.forClass(String.class);

            verify(utkastService).updateDraftFromCandidate(certificateIdArgumentCaptor.capture(), anyString(), any(Utkast.class));

            assertEquals(CANDIDATE_ID, certificateIdArgumentCaptor.getValue());
        }

        @Test
        void shallIncludeNewCertificateType() {

            createCertificateFromCandidateFacadeService.createCertificateFromCandidate(CERTIFICATE_ID);

            final var certificateTypeArgumentCaptor = ArgumentCaptor.forClass(String.class);

            verify(utkastService).updateDraftFromCandidate(anyString(), certificateTypeArgumentCaptor.capture(), any(Utkast.class));

            assertEquals(CANDIDATE_TYPE, certificateTypeArgumentCaptor.getValue());
        }

        @Test
        void shallIncludePatientId() {

            createCertificateFromCandidateFacadeService.createCertificateFromCandidate(CERTIFICATE_ID);

            final var certificateArgumentCaptor = ArgumentCaptor.forClass(Utkast.class);

            verify(utkastService).updateDraftFromCandidate(anyString(), anyString(), certificateArgumentCaptor.capture());

            assertEquals(PATIENT_ID, certificateArgumentCaptor.getValue().getPatientPersonnummer().getPersonnummer());
        }

        @Test
        void shallReturnDraftId() {

            final var actualCertificateId = createCertificateFromCandidateFacadeService.createCertificateFromCandidate(CERTIFICATE_ID);

            assertEquals(CERTIFICATE_ID, actualCertificateId);
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
        draft.setEnhetsId("enhetsId");
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
        draft.setEnhetsId("enhetsId");
        return draft;
    }

    private UtkastCandidateMetaData createCandidateMetaData(String intygId, String intygType, String intygTypeVersion) {
        return new UtkastCandidateMetaData.Builder()
            .with(builder -> {
                builder.intygId = intygId;
                builder.intygType = intygType;
                builder.intygTypeVersion = intygTypeVersion;
            })
            .create();
    }
}
