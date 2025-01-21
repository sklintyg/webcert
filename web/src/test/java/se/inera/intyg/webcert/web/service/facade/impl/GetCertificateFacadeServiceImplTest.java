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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import se.inera.intyg.common.support.facade.builder.CertificateBuilder;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.Unit;
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
import se.inera.intyg.webcert.web.service.access.DraftAccessServiceHelper;
import se.inera.intyg.webcert.web.service.facade.CertificateTextVersionFacadeService;
import se.inera.intyg.webcert.web.service.facade.util.IntygToCertificateConverter;
import se.inera.intyg.webcert.web.service.facade.util.UtkastToCertificateConverter;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;

@ExtendWith(MockitoExtension.class)
class GetCertificateFacadeServiceImplTest {

    @Mock
    private UtkastService utkastService;

    @Mock
    private IntygService intygService;

    @Mock
    private UtkastToCertificateConverter utkastToCertificateConverter;

    @Mock
    private IntygToCertificateConverter intygToCertificateConverter;

    @Mock
    private DraftAccessServiceHelper draftAccessServiceHelper;

    @Mock
    private ITIntegrationService itIntegrationService;

    @Mock
    private CertificateTextVersionFacadeService certificateTextVersionFacadeService;

    @InjectMocks
    private GetCertificateFacadeServiceImpl getCertificateService;

    @Nested
    class CertificateInWebcert {

        private final Utkast draft = createDraft();
        private ArgumentCaptor<Utkast> utkastArgumentCaptor;

        @BeforeEach
        void setupMocks() {
            doReturn(draft)
                .when(utkastService).getDraft(eq(draft.getIntygsId()), anyBoolean());

            utkastArgumentCaptor = ArgumentCaptor.forClass(Utkast.class);

            doReturn(createCertificate())
                .when(utkastToCertificateConverter).convert(utkastArgumentCaptor.capture());

            doReturn(draft).when(certificateTextVersionFacadeService).upgradeToLatestMinorTextVersion(draft);
        }

        @Test
        void shallReturnCertificate() {
            final var certificate = getCertificateService.getCertificate(draft.getIntygsId(), false, true);
            assertNotNull(certificate, "Certificate should not be null!");
        }

        @Test
        void shallCheckReadAccess() {
            final var certificate = getCertificateService.getCertificate(draft.getIntygsId(), false, true);
            verify(draftAccessServiceHelper).validateAllowToReadUtkast(draft);
            assertNotNull(certificate, "Certificate should not be null!");
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

                final var certificate = getCertificateService.getCertificate(draft.getIntygsId(), false, true);

                assertNotNull(certificate);
                assertEquals(expectedSentDateTime, utkastArgumentCaptor.getValue().getSkickadTillMottagareDatum());
            }

            @Test
            void shallGetRecieverFromITIfSentFromMinaIntyg() {
                final var expectedReceiver = "FKASSA";
                itIntygInfo.setSentToRecipient(LocalDateTime.now());
                final var intygInfoEvent = new IntygInfoEvent(Source.INTYGSTJANSTEN, LocalDateTime.now(), IntygInfoEventType.IS006);
                intygInfoEvent.addData("intygsmottagare", expectedReceiver);
                itIntygInfo.setEvents(List.of(intygInfoEvent));

                final var certificate = getCertificateService.getCertificate(draft.getIntygsId(), false, true);

                assertNotNull(certificate);
                assertEquals(expectedReceiver, utkastArgumentCaptor.getValue().getSkickadTillMottagare());
            }

            @Test
            void shallLeaveSentDateTimeAsNullIfNotSentFromMinaIntyg() {
                final var certificate = getCertificateService.getCertificate(draft.getIntygsId(), false, true);

                assertNotNull(certificate);
                assertNull(utkastArgumentCaptor.getValue().getSkickadTillMottagareDatum(), "Sent datetime should be null if not sent");
            }

            @Test
            void shallLeaveSentToReceiverAsNullIfNotSentFromMinaIntyg() {
                final var certificate = getCertificateService.getCertificate(draft.getIntygsId(), false, true);

                assertNotNull(certificate);
                assertNull(utkastArgumentCaptor.getValue().getSkickadTillMottagare(), "Receiver should be null if not sent!");
            }
        }

        @Test
        void shallNotGetCertificateStatusFromITIfSent() {
            draft.setStatus(UtkastStatus.SIGNED);
            getCertificateService.getCertificate(draft.getIntygsId(), false, true);
            verify(itIntegrationService, never())
                .getCertificateInfo(draft.getIntygsId());
        }

        @Test
        void shallNotGetCertificateStatusFromITIfDraftIncomplete() {
            draft.setStatus(UtkastStatus.DRAFT_INCOMPLETE);
            draft.setSkickadTillMottagareDatum(null);
            getCertificateService.getCertificate(draft.getIntygsId(), false, true);
            verify(itIntegrationService, never())
                .getCertificateInfo(draft.getIntygsId());
        }

        @Test
        void shallNotGetCertificateStatusFromITIfDraftComplete() {
            draft.setStatus(UtkastStatus.DRAFT_COMPLETE);
            draft.setSkickadTillMottagareDatum(null);
            getCertificateService.getCertificate(draft.getIntygsId(), false, true);
            verify(itIntegrationService, never())
                .getCertificateInfo(draft.getIntygsId());
        }

        @Test
        void shallNotGetCertificateStatusFromITIfDraftLocked() {
            draft.setStatus(UtkastStatus.DRAFT_LOCKED);
            draft.setSkickadTillMottagareDatum(null);
            getCertificateService.getCertificate(draft.getIntygsId(), false, true);
            verify(itIntegrationService, never())
                .getCertificateInfo(draft.getIntygsId());
        }

        @Nested
        class ValidatePdlLogging {

            @Test
            void shallPdlLogIfRequired() {
                final var actualPdlLogValue = ArgumentCaptor.forClass(Boolean.class);
                getCertificateService.getCertificate(draft.getIntygsId(), true, true);
                verify(utkastService).getDraft(anyString(), actualPdlLogValue.capture());
                assertTrue(actualPdlLogValue.getValue(), "Expect true because pdl logging is required");
            }

            @Test
            void shallNotPdlLogIfRequired() {
                final var actualPdlLogValue = ArgumentCaptor.forClass(Boolean.class);
                getCertificateService.getCertificate(draft.getIntygsId(), false, true);
                verify(utkastService).getDraft(anyString(), actualPdlLogValue.capture());
                assertFalse(actualPdlLogValue.getValue(), "Expect false because no pdl logging is required");
            }
        }

        private Utkast createDraft() {
            final var draft = new Utkast();
            draft.setIntygsId("certificateId");
            draft.setIntygsTyp("certificateType");
            draft.setIntygTypeVersion("certificateTypeVersion");
            draft.setModel("draftJson");
            draft.setStatus(UtkastStatus.SIGNED);
            draft.setSkickadTillMottagareDatum(LocalDateTime.now());
            draft.setSkapad(LocalDateTime.now());
            draft.setPatientPersonnummer(Personnummer.createPersonnummer("191212121212").orElseThrow());
            draft.setSkapadAv(new VardpersonReferens("personId", "personName"));
            return draft;
        }
    }

    @Nested
    class CertificateMissingInWebcert {

        private static final String CERTIFICATE_ID = "certificateId";
        private final IntygContentHolder intygContentHolder = IntygContentHolder.builder()
            .revoked(false)
            .deceased(false)
            .sekretessmarkering(false)
            .patientNameChangedInPU(false)
            .patientAddressChangedInPU(false)
            .testIntyg(false)
            .relations(new Relations())
            .latestMajorTextVersion(true)
            .build();

        @BeforeEach
        void setupMocks() {
            doThrow(new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, "Missing in Webcert"))
                .when(utkastService).getDraft(eq(CERTIFICATE_ID), anyBoolean());

            doReturn(intygContentHolder)
                .when(intygService).fetchIntygData(eq(CERTIFICATE_ID), eq(null), anyBoolean(), anyBoolean());

            doReturn(createCertificate())
                .when(intygToCertificateConverter).convert(intygContentHolder);
        }

        @Test
        void shallReturnCertificate() {
            final var certificate = getCertificateService.getCertificate(CERTIFICATE_ID, false, true);
            assertNotNull(certificate, "Certificate should not be null!");
        }

        @Nested
        class ValidatePdlLogging {

            @Test
            void shallPdlLogIfRequired() {
                final var actualPdlLogValue = ArgumentCaptor.forClass(Boolean.class);
                getCertificateService.getCertificate(CERTIFICATE_ID, true, true);
                verify(intygService).fetchIntygData(anyString(), eq(null), actualPdlLogValue.capture(), anyBoolean());
                assertTrue(actualPdlLogValue.getValue(), "Expect true because pdl logging is required");
            }

            @Test
            void shallNotPdlLogIfRequired() {
                final var actualPdlLogValue = ArgumentCaptor.forClass(Boolean.class);
                getCertificateService.getCertificate(CERTIFICATE_ID, false, true);
                verify(intygService).fetchIntygData(anyString(), eq(null), actualPdlLogValue.capture(), anyBoolean());
                assertFalse(actualPdlLogValue.getValue(), "Expect false because no pdl logging is required");
            }
        }

        @Nested
        class ValidateAccess {

            @Test
            void shallValidateAccessIfRequired() {
                final var actualValidateAccess = ArgumentCaptor.forClass(Boolean.class);
                getCertificateService.getCertificate(CERTIFICATE_ID, true, true);
                verify(intygService).fetchIntygData(anyString(), eq(null), anyBoolean(), actualValidateAccess.capture());
                assertTrue(actualValidateAccess.getValue(), "Expect true because validate access is required");
            }

            @Test
            void shallNotValidateAccessWhenNotRequired() {
                final var actualValidateAccess = ArgumentCaptor.forClass(Boolean.class);
                getCertificateService.getCertificate(CERTIFICATE_ID, true, false);
                verify(intygService).fetchIntygData(anyString(), eq(null), anyBoolean(), actualValidateAccess.capture());
                assertFalse(actualValidateAccess.getValue(), "Expect false because no access validation is required");
            }
        }
    }

    private Certificate createCertificate() {
        return CertificateBuilder.create()
            .metadata(
                CertificateMetadata.builder()
                    .id("certificateId")
                    .type("certificateType")
                    .typeVersion("certificateTypeVersion")
                    .unit(
                        Unit.builder()
                            .unitId("unitId")
                            .unitName("unitName")
                            .address("address")
                            .zipCode("zipCode")
                            .city("city")
                            .email("email")
                            .phoneNumber("phoneNumber")
                            .build()
                    )
                    .build()
            )
            .build();
    }
}
