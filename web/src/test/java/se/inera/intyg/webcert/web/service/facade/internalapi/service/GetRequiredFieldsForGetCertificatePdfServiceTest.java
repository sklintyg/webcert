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

package se.inera.intyg.webcert.web.service.facade.internalapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.fk7263.model.internal.Fk7263Utlatande;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.infra.intyginfo.dto.IntygInfoEvent;
import se.inera.intyg.infra.intyginfo.dto.IntygInfoEvent.Source;
import se.inera.intyg.infra.intyginfo.dto.IntygInfoEventType;
import se.inera.intyg.infra.intyginfo.dto.ItIntygInfo;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.web.integration.ITIntegrationService;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;

@ExtendWith(MockitoExtension.class)
class GetRequiredFieldsForGetCertificatePdfServiceTest {

    private static final String CERTIFICATE_ID = "certificateId";
    private static final String CERTIFICATE_TYPE = "certificateType";
    private static final String CERTIFICATE_TYPE_VERSION = "certificateTypeVersion";
    private static final String DRAFT_JSON = "draftJson";
    private static final String INTERNAL_JSON_MODEL = "internalJsonModel";
    private static final String EXPECTED_TYPE_VERSION_FK7263 = "1.0";
    private static final String EXPECTED_TYPE_FK_7263 = "fk7263";
    private static final List<Status> EXPECTED_STATUSES = List.of(new Status(CertificateState.RECEIVED, "target", LocalDateTime.now()));
    @Mock
    private UtkastService utkastService;

    @Mock
    private IntygService intygService;

    @Mock
    private ITIntegrationService itIntegrationService;

    @InjectMocks
    private GetRequiredFieldsForCertificatePdfService getRequiredFieldsForCertificatePdfService;

    @Nested
    class UtkastInWebcert {

        private final Utkast draft = createDraft();

        @BeforeEach
        void setupMocks() {
            doReturn(draft)
                .when(utkastService).getDraft(eq(draft.getIntygsId()), anyBoolean());
        }

        @Test
        void shallReturnTypeVersionFromUtkast() {
            final var result = getRequiredFieldsForCertificatePdfService.get(draft.getIntygsId());
            assertEquals(draft.getIntygTypeVersion(), result.getCertificateTypeVersion());
        }

        @Test
        void shallReturnTypeFromUtkast() {
            final var result = getRequiredFieldsForCertificatePdfService.get(draft.getIntygsId());
            assertEquals(draft.getIntygsTyp(), result.getCertificateType());
        }

        @Test
        void shallReturnInternalJsonModelFromUtkast() {
            final var result = getRequiredFieldsForCertificatePdfService.get(draft.getIntygsId());
            assertEquals(draft.getModel(), result.getInternalJsonModel());
        }

        @Test
        void shallReturnStatusesFromUtkast() {
            final var result = getRequiredFieldsForCertificatePdfService.get(draft.getIntygsId());
            assertEquals(CertificateState.SENT, result.getStatuses().get(0).getType());
        }

        @Nested
        class SentFromMinaIntyg {

            private ItIntygInfo itIntygInfo;

            @BeforeEach
            void setUp() {
                draft.setStatus(UtkastStatus.SIGNED);
                draft.setSkickadTillMottagareDatum(null);
                draft.setSkickadTillMottagare(null);

                itIntygInfo = new ItIntygInfo();

                doReturn(itIntygInfo)
                    .when(itIntegrationService)
                    .getCertificateInfo(draft.getIntygsId());
            }

            @Test
            void shallGetSentDateTimeFromITIfSentFromMinaIntyg() {
                final var expectedSentDateTime = LocalDateTime.now();
                itIntygInfo.setSentToRecipient(expectedSentDateTime);

                final var result = getRequiredFieldsForCertificatePdfService.get(draft.getIntygsId());
                assertEquals(expectedSentDateTime, result.getStatuses().get(0).getTimestamp());
            }

            @Test
            void shallGetRecieverFromITIfSentFromMinaIntyg() {
                final var expectedReceiver = "FKASSA";
                itIntygInfo.setSentToRecipient(LocalDateTime.now());
                final var intygInfoEvent = new IntygInfoEvent(Source.INTYGSTJANSTEN, LocalDateTime.now(), IntygInfoEventType.IS006);
                intygInfoEvent.addData("intygsmottagare", expectedReceiver);
                itIntygInfo.setEvents(List.of(intygInfoEvent));

                final var result = getRequiredFieldsForCertificatePdfService.get(draft.getIntygsId());
                assertEquals(expectedReceiver, result.getStatuses().get(0).getTarget());
            }

            @Test
            void shallLeaveSentDateTimeAsNullIfNotSentFromMinaIntyg() {

                final var result = getRequiredFieldsForCertificatePdfService.get(draft.getIntygsId());
                assertTrue(result.getStatuses().isEmpty(), "Sent datetime should be null if not sent");
            }

            @Test
            void shallLeaveSentToReceiverAsNullIfNotSentFromMinaIntyg() {
                final var result = getRequiredFieldsForCertificatePdfService.get(draft.getIntygsId());
                assertTrue(result.getStatuses().isEmpty(), "Sent datetime should be null if not sent");
            }
        }

        @Test
        void shallNotGetUtkastStatusFromITIfSent() {
            draft.setStatus(UtkastStatus.SIGNED);
            getRequiredFieldsForCertificatePdfService.get(draft.getIntygsId());
            verify(itIntegrationService, never())
                .getCertificateInfo(draft.getIntygsId());
        }

        @Test
        void shallNotGetUtkastStatusFromITIfDraftIncomplete() {
            draft.setStatus(UtkastStatus.DRAFT_INCOMPLETE);
            draft.setSkickadTillMottagareDatum(null);
            getRequiredFieldsForCertificatePdfService.get(draft.getIntygsId());
            verify(itIntegrationService, never())
                .getCertificateInfo(draft.getIntygsId());
        }

        @Test
        void shallNotGetUtkastStatusFromITIfDraftComplete() {
            draft.setStatus(UtkastStatus.DRAFT_COMPLETE);
            draft.setSkickadTillMottagareDatum(null);
            getRequiredFieldsForCertificatePdfService.get(draft.getIntygsId());
            verify(itIntegrationService, never())
                .getCertificateInfo(draft.getIntygsId());
        }

        @Test
        void shallNotGetUtkastStatusFromITIfDraftLocked() {
            draft.setStatus(UtkastStatus.DRAFT_LOCKED);
            draft.setSkickadTillMottagareDatum(null);
            getRequiredFieldsForCertificatePdfService.get(draft.getIntygsId());
            verify(itIntegrationService, never())
                .getCertificateInfo(draft.getIntygsId());
        }

        @Nested
        class ValidatePdlLogging {

            @Test
            void shallNotPdlLog() {
                final var actualPdlLogValue = ArgumentCaptor.forClass(Boolean.class);
                getRequiredFieldsForCertificatePdfService.get(draft.getIntygsId());
                verify(utkastService).getDraft(anyString(), actualPdlLogValue.capture());
                assertFalse(actualPdlLogValue.getValue());
            }
        }

        private Utkast createDraft() {
            final var draft = new Utkast();
            draft.setIntygsId(CERTIFICATE_ID);
            draft.setIntygsTyp(CERTIFICATE_TYPE);
            draft.setIntygTypeVersion(CERTIFICATE_TYPE_VERSION);
            draft.setModel(DRAFT_JSON);
            draft.setStatus(UtkastStatus.SIGNED);
            draft.setSkickadTillMottagareDatum(LocalDateTime.now());
            draft.setSkapad(LocalDateTime.now());
            draft.setPatientPersonnummer(Personnummer.createPersonnummer("191212121212").orElseThrow());
            draft.setSkapadAv(new VardpersonReferens("personId", "personName"));
            return draft;
        }
    }

    @Nested
    class UtkastMissingInWebcert {

        private final IntygContentHolder intygContentHolder = IntygContentHolder.builder()
            .revoked(false)
            .deceased(false)
            .utlatande(new Fk7263Utlatande())
            .sekretessmarkering(false)
            .contents(INTERNAL_JSON_MODEL)
            .patientNameChangedInPU(false)
            .patientAddressChangedInPU(false)
            .testIntyg(false)
            .statuses(EXPECTED_STATUSES)
            .build();

        @BeforeEach
        void setupMocks() {
            doThrow(new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, "Missing in Webcert"))
                .when(utkastService).getDraft(eq(CERTIFICATE_ID), anyBoolean());

            doReturn(intygContentHolder)
                .when(intygService).fetchIntygData(eq(CERTIFICATE_ID), eq(null), anyBoolean(), anyBoolean());
        }

        @Test
        void shallReturnTypeVersionFromUtkast() {
            final var result = getRequiredFieldsForCertificatePdfService.get(CERTIFICATE_ID);
            assertEquals(EXPECTED_TYPE_VERSION_FK7263, result.getCertificateTypeVersion());
        }

        @Test
        void shallReturnTypeFromUtkast() {
            final var result = getRequiredFieldsForCertificatePdfService.get(CERTIFICATE_ID);
            assertEquals(EXPECTED_TYPE_FK_7263, result.getCertificateType());
        }

        @Test
        void shallReturnInternalJsonModelFromUtkast() {
            final var result = getRequiredFieldsForCertificatePdfService.get(CERTIFICATE_ID);
            assertEquals(INTERNAL_JSON_MODEL, result.getInternalJsonModel());
        }

        @Test
        void shallReturnStatusesFromUtkast() {
            final var result = getRequiredFieldsForCertificatePdfService.get(CERTIFICATE_ID);
            assertEquals(CertificateState.RECEIVED, result.getStatuses().get(0).getType());
        }

        @Test
        void shallSetUtkastStatusToSigned() {
            final var result = getRequiredFieldsForCertificatePdfService.get(CERTIFICATE_ID);
            assertEquals(UtkastStatus.SIGNED, result.getStatus());
        }

        @Nested
        class ValidatePdlLogging {

            @Test
            void shallNotPdlLog() {
                final var actualPdlLogValue = ArgumentCaptor.forClass(Boolean.class);
                getRequiredFieldsForCertificatePdfService.get(CERTIFICATE_ID);
                verify(intygService).fetchIntygData(anyString(), eq(null), actualPdlLogValue.capture(), anyBoolean());
                assertFalse(actualPdlLogValue.getValue());
            }
        }

        @Nested
        class ValidateAccessLogging {

            @Test
            void shallNotValidateAccess() {
                final var actualValidateAccessLogging = ArgumentCaptor.forClass(Boolean.class);
                getRequiredFieldsForCertificatePdfService.get(CERTIFICATE_ID);
                verify(intygService).fetchIntygData(anyString(), eq(null), anyBoolean(), actualValidateAccessLogging.capture());
                assertFalse(actualValidateAccessLogging.getValue());
            }
        }
    }
}
