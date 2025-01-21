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

package se.inera.intyg.webcert.web.web.controller.internalapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateText;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.webcert.web.service.facade.GetCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.internalapi.service.GetAvailableFunctionsForCertificateService;
import se.inera.intyg.webcert.web.service.facade.internalapi.service.GetTextsForCertificateService;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.AvailableFunctionDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.AvailableFunctionTypeDTO;

@ExtendWith(MockitoExtension.class)
class CertificateInternalServiceFromWCTest {

    private static final Certificate EXPECTED_CERTIFICATE = new Certificate();
    private static final List<AvailableFunctionDTO> EXPECTED_AVAILABLE_FUNCTIONS = List.of(
        AvailableFunctionDTO.create(AvailableFunctionTypeDTO.CUSTOMIZE_PRINT_CERTIFICATE, null,
            null, null, true)
    );
    private static final List<CertificateText> EXPECTED_TEXTS = List.of(
        CertificateText.builder().build()
    );
    private static final String CERTIFICATE_ID = "certificateId";
    private static final boolean SHOULD_NOT_PDL_LOG = false;
    private static final boolean SHOULD_NOT_VALIDATE_ACCESS = false;
    private static final String TYPE = "TYPE";
    private static final String TYPE_VERSION = "TYPE_VERSION";
    private static final String PERSON_ID = "personId";
    @Mock
    private GetAvailableFunctionsForCertificateService getAvailableFunctionsForCertificateService;

    @Mock
    private GetTextsForCertificateService getTextsForCertificateService;
    @Mock
    private GetCertificateFacadeService getCertificateFacadeService;

    @InjectMocks
    private GetCertificateInternalServiceFromWC certificateInternalServiceFromWC;

    @BeforeEach
    void setUp() {
        EXPECTED_CERTIFICATE.setMetadata(CertificateMetadata.builder()
            .type(TYPE)
            .typeVersion(TYPE_VERSION)
            .build()
        );
        doReturn(EXPECTED_CERTIFICATE)
            .when(getCertificateFacadeService).getCertificate(CERTIFICATE_ID, SHOULD_NOT_PDL_LOG, SHOULD_NOT_VALIDATE_ACCESS);
        doReturn(EXPECTED_AVAILABLE_FUNCTIONS)
            .when(getAvailableFunctionsForCertificateService).get(EXPECTED_CERTIFICATE);
    }

    @Test
    void shallReturnCertificate() {
        final var actualCertificateResponse = certificateInternalServiceFromWC.get(CERTIFICATE_ID, PERSON_ID);
        assertEquals(EXPECTED_CERTIFICATE, actualCertificateResponse.getCertificate());
    }

    @Test
    void shallReturnAvailableFunctions() {
        final var actualCertificateResponse = certificateInternalServiceFromWC.get(CERTIFICATE_ID, PERSON_ID);
        assertEquals(EXPECTED_AVAILABLE_FUNCTIONS, actualCertificateResponse.getAvailableFunctions());
    }

    @Test
    void shallReturnCertificateTexts() {
        when(getTextsForCertificateService.get(TYPE, TYPE_VERSION))
            .thenReturn(EXPECTED_TEXTS);

        final var actualCertificateResponse = certificateInternalServiceFromWC.get(CERTIFICATE_ID, PERSON_ID);

        assertEquals(EXPECTED_TEXTS, actualCertificateResponse.getTexts());
    }

    @Test
    void shallNotPdlLogWhenRetrievingCertificate() {
        final var booleanArgumentCaptor = ArgumentCaptor.forClass(Boolean.class);
        certificateInternalServiceFromWC.get(CERTIFICATE_ID, PERSON_ID);
        verify(getCertificateFacadeService).getCertificate(
            anyString(),
            booleanArgumentCaptor.capture(),
            anyBoolean()
        );
        assertEquals(SHOULD_NOT_PDL_LOG, booleanArgumentCaptor.getValue());
    }

    @Test
    void shallNotValidateAccessWhenRetrievingCertificate() {
        final var booleanArgumentCaptor = ArgumentCaptor.forClass(Boolean.class);
        certificateInternalServiceFromWC.get(CERTIFICATE_ID, PERSON_ID);
        verify(getCertificateFacadeService).getCertificate(
            anyString(),
            anyBoolean(),
            booleanArgumentCaptor.capture()
        );
        assertEquals(SHOULD_NOT_VALIDATE_ACCESS, booleanArgumentCaptor.getValue());
    }
}
