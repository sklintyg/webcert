/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.inera.intyg.common.support.facade.model.CertificateStatus.REVOKED;
import static se.inera.intyg.common.support.facade.model.CertificateStatus.SIGNED;

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
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistryImpl;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.ApplicationOrigin;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.PdfResponse;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.persistence.utkast.model.Signatur;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.GetUtkastResponse;

@ExtendWith(MockitoExtension.class)
class GetCertificatePrintServiceTest {

    private static final String CERTIFICATE_TYPE_VERSION = "certificateTypeVersion";
    private static final String CERTIFICATE_TYPE = "certificateType";
    private static final String DRAFT_MODEL = "model";
    private static final String CERTIFICATE_ID = "certificateId";
    private static final String INTYG_CONTENT_HOLDER_MODEL = "intygContentHolderModel";
    private static final String WRONG_CUSTOMIZAITON_ID = "wrongCustomizaitonId";
    private static final String CUSTOMIZE_ID = "!diagnoser";
    private final byte[] bytes = new byte[0];
    private final String fileName = "fileName";
    private final PdfResponse pdfResponse = new PdfResponse(bytes, fileName);

    @Mock
    private ModuleApi moduleApi;
    @Mock
    private GetUtkastFacadeService getUtkastFacadeService;
    @Mock
    private IntygModuleRegistryImpl moduleRegistry;
    @InjectMocks
    private GetCertificatePrintService getCertificatePrintService;

    @Test
    void shouldThrowIfCertificateIsRevoked() {
        final Certificate certificate = getCertificate(REVOKED);
        assertThrows(IllegalStateException.class, () -> getCertificatePrintService.get(null, certificate, false, false));
    }

    @Test
    void shouldReturnPdfResponse() throws ModuleNotFoundException, ModuleException {
        final var certificate = getCertificate(SIGNED);
        when(moduleRegistry.getModuleApi(certificate.getMetadata().getTypeVersion(), certificate.getMetadata().getType()))
            .thenReturn(moduleApi);
        when(getUtkastFacadeService.get(certificate.getMetadata().getId(), false, false))
            .thenReturn(GetUtkastResponse.create(getDraft(UtkastStatus.SIGNED)));
        when(moduleApi.pdf(any(), any(), any(), any())).thenReturn(pdfResponse);
        final var result = getCertificatePrintService.get(null, certificate, false, false);
        assertEquals(pdfResponse, result);
    }

    @Nested
    class ModuleRegistryInteraction {

        private final Certificate certificate = getCertificate(SIGNED);
        private final ArgumentCaptor<String> moduleRegistryArgumentCaptor = ArgumentCaptor.forClass(String.class);

        @BeforeEach
        void setUp() {
            when(getUtkastFacadeService.get(certificate.getMetadata().getId(), false, false))
                .thenReturn(GetUtkastResponse.create(getDraft(UtkastStatus.SIGNED)));
        }

        @Test
        void shouldUseTypeVersionFromCertificate() throws ModuleNotFoundException, ModuleException {
            when(moduleRegistry.getModuleApi(moduleRegistryArgumentCaptor.capture(), eq(certificate.getMetadata().getType())))
                .thenReturn(moduleApi);
            getCertificatePrintService.get(null, certificate, false, false);

            assertEquals(CERTIFICATE_TYPE_VERSION, moduleRegistryArgumentCaptor.getValue());
        }

        @Test
        void shouldUseTypeFromCertificate() throws ModuleNotFoundException, ModuleException {
            when(moduleRegistry.getModuleApi(eq(certificate.getMetadata().getTypeVersion()), moduleRegistryArgumentCaptor.capture()))
                .thenReturn(moduleApi);
            getCertificatePrintService.get(null, certificate, false, false);

            assertEquals(CERTIFICATE_TYPE, moduleRegistryArgumentCaptor.getValue());
        }
    }

    @Nested
    class UtkastFacadeServiceInteraction {

        private final Certificate certificate = getCertificate(SIGNED);
        private final ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        private final ArgumentCaptor<Boolean> booleanArgumentCaptor = ArgumentCaptor.forClass(boolean.class);

        @Test
        void shouldUseIdFromCertificate() throws ModuleNotFoundException, ModuleException {
            when(moduleRegistry.getModuleApi(certificate.getMetadata().getTypeVersion(), certificate.getMetadata().getType()))
                .thenReturn(moduleApi);
            when(getUtkastFacadeService.get(stringArgumentCaptor.capture(), eq(false), eq(false)))
                .thenReturn(GetUtkastResponse.create(getDraft(UtkastStatus.SIGNED)));
            getCertificatePrintService.get(null, certificate, false, false);

            assertEquals(CERTIFICATE_ID, stringArgumentCaptor.getValue());
        }

        @Test
        void pdlLogShouldBeSetToFalse() throws ModuleNotFoundException, ModuleException {
            when(moduleRegistry.getModuleApi(certificate.getMetadata().getTypeVersion(), certificate.getMetadata().getType()))
                .thenReturn(moduleApi);
            when(getUtkastFacadeService.get(eq(certificate.getMetadata().getId()), booleanArgumentCaptor.capture(), eq(false)))
                .thenReturn(GetUtkastResponse.create(getDraft(UtkastStatus.SIGNED)));
            getCertificatePrintService.get(null, certificate, false, false);

            assertEquals(false, booleanArgumentCaptor.getValue());
        }

        @Test
        void validateAccessShouldBeSetToFalse() throws ModuleNotFoundException, ModuleException {
            when(moduleRegistry.getModuleApi(certificate.getMetadata().getTypeVersion(), certificate.getMetadata().getType()))
                .thenReturn(moduleApi);
            when(getUtkastFacadeService.get(eq(certificate.getMetadata().getId()), eq(false), booleanArgumentCaptor.capture()))
                .thenReturn(GetUtkastResponse.create(getDraft(UtkastStatus.SIGNED)));
            getCertificatePrintService.get(null, certificate, false, false);

            assertEquals(false, booleanArgumentCaptor.getValue());
        }
    }

    @Nested
    class ModuleApiPdfInteraction {

        private final Certificate certificate = getCertificate(SIGNED);

        @BeforeEach
        void setUp() throws ModuleNotFoundException {
            when(moduleRegistry.getModuleApi(certificate.getMetadata().getTypeVersion(), certificate.getMetadata().getType()))
                .thenReturn(moduleApi);
        }

        @Nested
        class Draft {

            @Test
            void shouldUseJsonModelFromDraft() throws ModuleNotFoundException, ModuleException {
                final var stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
                when(getUtkastFacadeService.get(certificate.getMetadata().getId(), false, false))
                    .thenReturn(GetUtkastResponse.create(getDraft(UtkastStatus.SIGNED)));
                when(moduleApi.pdf(stringArgumentCaptor.capture(), any(), any(), any())).thenReturn(pdfResponse);
                getCertificatePrintService.get(null, certificate, false, false);
                assertEquals(DRAFT_MODEL, stringArgumentCaptor.getValue());
            }

            @Test
            void shouldUseStatusesFromDraft() throws ModuleNotFoundException, ModuleException {
                final var listArgumentCaptor = ArgumentCaptor.forClass(List.class);
                when(getUtkastFacadeService.get(certificate.getMetadata().getId(), false, false))
                    .thenReturn(GetUtkastResponse.create(getDraft(UtkastStatus.SIGNED)));
                when(moduleApi.pdf(any(), listArgumentCaptor.capture(), any(), any())).thenReturn(pdfResponse);
                getCertificatePrintService.get(null, certificate, false, false);
                assertFalse(listArgumentCaptor.getValue().isEmpty());
            }

            @Test
            void shouldUseStatusFromDraft() throws ModuleNotFoundException, ModuleException {
                final var statusArgumentCaptor = ArgumentCaptor.forClass(UtkastStatus.class);
                when(getUtkastFacadeService.get(certificate.getMetadata().getId(), false, false))
                    .thenReturn(GetUtkastResponse.create(getDraft(UtkastStatus.DRAFT_INCOMPLETE)));
                when(moduleApi.pdf(any(), anyList(), any(), statusArgumentCaptor.capture())).thenReturn(pdfResponse);
                getCertificatePrintService.get(null, certificate, false, false);
                assertEquals(UtkastStatus.DRAFT_INCOMPLETE, statusArgumentCaptor.getValue());
            }

            @Test
            void shouldUseSetStatusToSignedIfNoStatusFromDraftWasFound() throws ModuleNotFoundException, ModuleException {
                final var statusArgumentCaptor = ArgumentCaptor.forClass(UtkastStatus.class);
                when(getUtkastFacadeService.get(certificate.getMetadata().getId(), false, false))
                    .thenReturn(GetUtkastResponse.create(getIntygContentHolder()));
                when(moduleApi.pdf(any(), anyList(), any(), statusArgumentCaptor.capture())).thenReturn(pdfResponse);
                getCertificatePrintService.get(null, certificate, false, false);
                assertEquals(UtkastStatus.SIGNED, statusArgumentCaptor.getValue());
            }
        }

        @Nested
        class IntygContentHolderInteraction {

            @Test
            void shouldUseJsonModelFromIntygContentHolder() throws ModuleNotFoundException, ModuleException {
                final var stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
                when(getUtkastFacadeService.get(certificate.getMetadata().getId(), false, false))
                    .thenReturn(GetUtkastResponse.create(getIntygContentHolder()));
                when(moduleApi.pdf(stringArgumentCaptor.capture(), any(), any(), any())).thenReturn(pdfResponse);
                getCertificatePrintService.get(null, certificate, false, false);
                assertEquals(INTYG_CONTENT_HOLDER_MODEL, stringArgumentCaptor.getValue());
            }


            @Test
            void shouldUseStatusesFromIntygContentHolder() throws ModuleNotFoundException, ModuleException {
                final var listArgumentCaptor = ArgumentCaptor.forClass(List.class);
                when(getUtkastFacadeService.get(certificate.getMetadata().getId(), false, false))
                    .thenReturn(GetUtkastResponse.create(getIntygContentHolder()));
                when(moduleApi.pdf(any(), listArgumentCaptor.capture(), any(), any())).thenReturn(pdfResponse);
                getCertificatePrintService.get(null, certificate, false, false);
                assertFalse(listArgumentCaptor.getValue().isEmpty());
            }
        }


        @Test
        void shouldUseOriginMinaIntyg() throws ModuleNotFoundException, ModuleException {
            final var originArgumentCaptor = ArgumentCaptor.forClass(ApplicationOrigin.class);
            when(getUtkastFacadeService.get(certificate.getMetadata().getId(), false, false))
                .thenReturn(GetUtkastResponse.create(getDraft(UtkastStatus.SIGNED)));
            when(moduleApi.pdf(any(), anyList(), originArgumentCaptor.capture(), any())).thenReturn(pdfResponse);
            getCertificatePrintService.get(null, certificate, false, false);
            assertEquals(ApplicationOrigin.MINA_INTYG, originArgumentCaptor.getValue());
        }


        @Test
        void shouldCallPdfEmployerIfCorrectCustomizeIdWasProvidedFromRequest() throws ModuleNotFoundException, ModuleException {
            when(getUtkastFacadeService.get(certificate.getMetadata().getId(), false, false))
                .thenReturn(GetUtkastResponse.create(getIntygContentHolder()));
            getCertificatePrintService.get(CUSTOMIZE_ID, certificate, false, false);
            verify(moduleApi).pdfEmployer(any(), anyList(), any(), anyList(), any());
        }

        @Test
        void shouldCallPdfIfWrongCustomizeIdWasProvidedFromRequest() throws ModuleNotFoundException, ModuleException {
            when(getUtkastFacadeService.get(certificate.getMetadata().getId(), false, false))
                .thenReturn(GetUtkastResponse.create(getIntygContentHolder()));
            getCertificatePrintService.get(WRONG_CUSTOMIZAITON_ID, certificate, false, false);
            verify(moduleApi).pdf(any(), anyList(), any(), any());
        }

        @Test
        void shouldCallPdfIfNoCustomizeIdWasProvidedFromRequest() throws ModuleNotFoundException, ModuleException {
            when(getUtkastFacadeService.get(certificate.getMetadata().getId(), false, false))
                .thenReturn(GetUtkastResponse.create(getIntygContentHolder()));
            getCertificatePrintService.get(null, certificate, false, false);
            verify(moduleApi).pdf(any(), anyList(), any(), any());
        }
    }


    private static Utkast getDraft(UtkastStatus utkastStatus) {
        final var utkast = new Utkast();
        utkast.setModel(DRAFT_MODEL);
        utkast.setStatus(utkastStatus);
        utkast.setSignatur(new Signatur(LocalDateTime.now(), null, null, null, null, null, null));
        return utkast;
    }

    private static IntygContentHolder getIntygContentHolder() {
        return IntygContentHolder.builder()
            .setContents(INTYG_CONTENT_HOLDER_MODEL)
            .setRevoked(false)
            .setDeceased(false)
            .setPatientNameChangedInPU(false)
            .setSekretessmarkering(false)
            .setPatientAddressChangedInPU(false)
            .setTestIntyg(false)
            .setStatuses(List.of(new Status()))
            .build();
    }

    private static Certificate getCertificate(CertificateStatus certificateStatus) {
        final var certificate = new Certificate();
        certificate.setMetadata(
            CertificateMetadata.builder()
                .id(CERTIFICATE_ID)
                .type(CERTIFICATE_TYPE)
                .typeVersion(CERTIFICATE_TYPE_VERSION)
                .status(certificateStatus)
                .build()
        );
        return certificate;
    }
}
