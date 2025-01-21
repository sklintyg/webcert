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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.builder.CertificateBuilder;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.SaveDraftResponse;

@ExtendWith(MockitoExtension.class)
class SaveCertificateFacadeServiceImplTest {

    @Mock
    private UtkastService utkastService;

    @Mock
    private IntygModuleRegistry intygModuleRegistry;

    @InjectMocks
    private SaveCertificateFacadeServiceImpl saveCertificateFacadeService;

    private final static String CERTIFICATE_ID = "certificateId";
    private final static String CERTIFICATE_TYPE = "certificateType";
    private final static String CERTIFICATE_TYPE_VERSION = "certificateTypeVersion";
    private final static long VERSION = 100;
    private final static long NEW_VERSION = 101;
    private final static String UPDATE_JSON = "UpdatedJson";

    private Certificate certificate;

    @BeforeEach
    void setup() throws Exception {
        certificate = CertificateBuilder.create()
            .metadata(
                CertificateMetadata.builder()
                    .id(CERTIFICATE_ID)
                    .type(CERTIFICATE_TYPE)
                    .typeVersion(CERTIFICATE_TYPE_VERSION)
                    .build()
            )
            .build();

        certificate.getMetadata().setVersion(VERSION);

        final var currentCertificate = new Utkast();
        currentCertificate.setModel("JsonModel");

        doReturn(currentCertificate)
            .when(utkastService)
            .getDraft(CERTIFICATE_ID, false);

        final var moduleApi = mock(ModuleApi.class);

        doReturn(moduleApi)
            .when(intygModuleRegistry)
            .getModuleApi(CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION);

        doReturn(UPDATE_JSON)
            .when(moduleApi)
            .getJsonFromCertificate(certificate, currentCertificate.getModel());

        final var saveDraftResponse = new SaveDraftResponse(NEW_VERSION, UtkastStatus.DRAFT_COMPLETE);

        doReturn(saveDraftResponse)
            .when(utkastService)
            .saveDraft(
                eq(CERTIFICATE_ID),
                eq(VERSION),
                eq(UPDATE_JSON),
                anyBoolean()
            );
    }

    @Test
    void shallSaveCertificate() {
        final var actualVersion = saveCertificateFacadeService.saveCertificate(certificate, true);
        assertEquals(NEW_VERSION, actualVersion);
    }

    @Test
    void shallPdlLogWhenSaving() {
        final var actualPdlLogValue = ArgumentCaptor.forClass(Boolean.class);
        saveCertificateFacadeService.saveCertificate(certificate, true);
        verify(utkastService).saveDraft(anyString(), anyLong(), anyString(), actualPdlLogValue.capture());
        assertTrue(actualPdlLogValue.getValue(), "Expect true because pdl logging is required");
    }

    @Test
    void shallNotPdlLogWhenSaving() {
        final var actualPdlLogValue = ArgumentCaptor.forClass(Boolean.class);
        saveCertificateFacadeService.saveCertificate(certificate, false);
        verify(utkastService).saveDraft(anyString(), anyLong(), anyString(), actualPdlLogValue.capture());
        assertFalse(actualPdlLogValue.getValue(), "Expect false because no pdl logging is required");
    }
}
