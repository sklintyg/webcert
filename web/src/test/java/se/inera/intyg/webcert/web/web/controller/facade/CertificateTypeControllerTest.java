/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.schemas.contract.InvalidPersonNummerException;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.facade.GetCertificateTypesFacadeService;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateTypeInfoDTO;

@ExtendWith(MockitoExtension.class)
public class CertificateTypeControllerTest {

    @Mock
    private GetCertificateTypesFacadeService getCertificateTypesFacadeService;

    @InjectMocks
    private CertificateTypeController controller;

    @Nested
    class CertificateTypeControllerTests {

        @Test
        void shallReturnCertificatTypes() throws InvalidPersonNummerException {
            final var type = new CertificateTypeInfoDTO();
            final var types = List.of(
                type
            );

            doReturn(types)
                .when(getCertificateTypesFacadeService)
                .get(any(Personnummer.class));

            final var response = (List<CertificateTypeInfoDTO>) controller.getCertificateTypes("19121212-1212").getEntity();
            assertTrue(response.contains(type));
        }

        @Test
        void shallReturnBadRequestOnException() throws InvalidPersonNummerException {
            final var response = controller.getCertificateTypes("19121212");
            assertEquals(400, response.getStatus());
        }
    }
}
