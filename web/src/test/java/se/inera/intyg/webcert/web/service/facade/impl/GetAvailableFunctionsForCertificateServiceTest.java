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

package se.inera.intyg.webcert.web.service.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.web.service.facade.internalapi.availablefunction.CertificateCustomizeFunction;
import se.inera.intyg.webcert.web.service.facade.internalapi.service.GetAvailableFunctionsForCertificateService;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.AvailableFunctionDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.AvailableFunctionTypeDTO;

@ExtendWith(MockitoExtension.class)
class GetAvailableFunctionsForCertificateServiceTest {

    @Mock
    private CertificateCustomizeFunction certificateCustomizeFunction;
    @InjectMocks
    private GetAvailableFunctionsForCertificateService getAvailableFunctionsForCertificateService;

    private static final Certificate CERTIFICATE = new Certificate();

    @Test
    void shouldIncludeCertificateCustomizeResourceLink() {
        final var expectedResourceLink = AvailableFunctionDTO.create(
            AvailableFunctionTypeDTO.CUSTOMIZE_PRINT_CERTIFICATE, null, null, null);
        when(certificateCustomizeFunction.get(CERTIFICATE)).thenReturn(
            Optional.of(
                expectedResourceLink
            )
        );
        final var result = getAvailableFunctionsForCertificateService.get(CERTIFICATE);
        assertEquals(expectedResourceLink, result.get(0));
    }

    @Test
    void shouldNotIncludeCertificateCustomizeResourceLink() {
        when(certificateCustomizeFunction.get(CERTIFICATE)).thenReturn(
            Optional.empty()
        );
        final var result = getAvailableFunctionsForCertificateService.get(CERTIFICATE);
        assertTrue(result.isEmpty());
    }
}
