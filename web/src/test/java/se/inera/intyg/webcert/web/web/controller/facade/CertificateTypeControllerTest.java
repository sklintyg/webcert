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
package se.inera.intyg.webcert.web.web.controller.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.csintegration.aggregate.CertificateTypeInfoAggregator;
import se.inera.intyg.webcert.web.service.facade.GetCertificateTypeInfoModalFacadeService;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateTypeInfoDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateTypeInfoModalDTO;

@ExtendWith(MockitoExtension.class)
class CertificateTypeControllerTest {

    @Mock
    private CertificateTypeInfoAggregator certificateTypeInfoAggregator;

    @Mock
    private GetCertificateTypeInfoModalFacadeService getCertificateTypeInfoModalFacadeService;

    @InjectMocks
    private CertificateTypeController controller;

    @Nested
    class CertificateTypeControllerTests {

        @Test
        void shouldReturnCertificateTypes() {
            final var type = new CertificateTypeInfoDTO();
            final var types = List.of(
                type
            );

            doReturn(types)
                .when(certificateTypeInfoAggregator)
                .get(any(Personnummer.class));

            final var response = (List<CertificateTypeInfoDTO>) controller.getCertificateTypes("19121212-1212").getEntity();
            assertTrue(response.contains(type));
        }

        @Test
        void shouldReturnBadRequestOnException() {
            final var response = controller.getCertificateTypes("19121212");
            assertEquals(400, response.getStatus());
        }

        @Test
        void shouldReturnOkStatusForValidCertificateTypes() {
            final var types = List.of(new CertificateTypeInfoDTO());

            doReturn(types)
                .when(certificateTypeInfoAggregator)
                .get(any(Personnummer.class));

            final var response = controller.getCertificateTypes("19121212-1212");
            assertEquals(200, response.getStatus());
        }
    }
    
    @Nested
    class GetCertificateTypeInfoModalTests {

        @Test
        void shouldReturnCertificateTypeInfoModal() {
            final var modal = CertificateTypeInfoModalDTO.builder()
                .title("Test Title")
                .description("Test Description")
                .build();
            final var certificateType = "lisjp";
            final var patientId = "19121212-1212";

            doReturn(modal)
                .when(getCertificateTypeInfoModalFacadeService)
                .get(eq(certificateType), any(Personnummer.class));

            final var response = controller.getCertificateTypeInfoModal(certificateType, patientId);
            final var result = (CertificateTypeInfoModalDTO) response.getEntity();

            assertEquals(200, response.getStatus());
            assertNotNull(result);
            assertEquals(modal, result);
        }

        @Test
        void shouldReturnNoContentWhenModalIsNull() {
            final var certificateType = "lisjp";
            final var patientId = "19121212-1212";

            doReturn(null)
                .when(getCertificateTypeInfoModalFacadeService)
                .get(eq(certificateType), any(Personnummer.class));

            final var response = controller.getCertificateTypeInfoModal(certificateType, patientId);

            assertEquals(204, response.getStatus());
            assertNull(response.getEntity());
        }

        @Test
        void shouldReturnBadRequestOnInvalidPersonnummer() {
            final var certificateType = "lisjp";
            final var invalidPatientId = "invalid";

            final var response = controller.getCertificateTypeInfoModal(certificateType, invalidPatientId);

            assertEquals(400, response.getStatus());
        }

        @Test
        void shouldCallServiceWithCorrectParameters() {
            final var modal = CertificateTypeInfoModalDTO.builder()
                .title("Test Title")
                .description("Test Description")
                .build();
            final var certificateType = "ag7804";
            final var patientId = "19121212-1212";

            doReturn(modal)
                .when(getCertificateTypeInfoModalFacadeService)
                .get(anyString(), any(Personnummer.class));

            controller.getCertificateTypeInfoModal(certificateType, patientId);

            verify(getCertificateTypeInfoModalFacadeService).get(eq(certificateType), any(Personnummer.class));
        }
    }
}
