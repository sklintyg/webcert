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

package se.inera.intyg.webcert.web.csintegration.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateModelIdDTO;

@ExtendWith(MockitoExtension.class)
class CertificateServiceProfileActiveTest {

    private static final String SUPPORTED_TYPE = "supportedType";
    private static final String SUPPORTED_VERSION = "supportedVersion";
    private static final String NOT_SUPPORTED_VERSION = "notSupportedVersion";
    private static final String NOT_SUPPORTED_TYPE = "notSupportedType";
    @Mock
    private CSIntegrationService csIntegrationService;
    @InjectMocks
    private CertificateServiceProfileActive certificateServiceProfileActive;

    @Test
    void shallReturnTrue() {
        assertTrue(certificateServiceProfileActive.active());
    }

    @Test
    void shouldReturnTrueIfProfileIsActiveAndTypeIsSupportedWithCorrectVersion() {
        final var certificateModelId = CertificateModelIdDTO.builder()
            .version(SUPPORTED_VERSION)
            .build();
        final var modelIdDTO = Optional.of(certificateModelId);
        doReturn(modelIdDTO).when(csIntegrationService).certificateTypeExists(SUPPORTED_TYPE);
        assertTrue(certificateServiceProfileActive.activeAndSupportsType(SUPPORTED_TYPE, SUPPORTED_VERSION));
    }

    @Test
    void shouldReturnFalseIfProfileIsActiveAndTypeIsSupportedWithIncorrectVersion() {
        final var certificateModelId = CertificateModelIdDTO.builder()
            .version(NOT_SUPPORTED_VERSION)
            .build();
        final var modelIdDTO = Optional.of(certificateModelId);
        doReturn(modelIdDTO).when(csIntegrationService).certificateTypeExists(SUPPORTED_TYPE);
        assertFalse(certificateServiceProfileActive.activeAndSupportsType(SUPPORTED_TYPE, SUPPORTED_VERSION));
    }

    @Test
    void shouldReturnFalseIfProfileIsActiveAndTypeIsNotSupported() {
        final var modelIdDTO = Optional.empty();
        doReturn(modelIdDTO).when(csIntegrationService).certificateTypeExists(NOT_SUPPORTED_TYPE);
        assertFalse(certificateServiceProfileActive.activeAndSupportsType(NOT_SUPPORTED_TYPE, SUPPORTED_VERSION));
    }
}