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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.service.facade.IcfFacadeService;
import se.inera.intyg.webcert.web.web.controller.facade.dto.IcfRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.IcfResponseDTO;

@ExtendWith(MockitoExtension.class)
public class IcfControllerTest {

    @Mock
    private IcfFacadeService icfFacadeService;

    @InjectMocks
    private IcfController icfController;

    @Nested
    class ValidateSickLeavePeriod {

        @BeforeEach
        void setup() {
        }

        @Test
        void shallGetIcfInformation() {
            IcfRequestDTO request = new IcfRequestDTO();
            icfController.getIcf(request);
            verify(icfFacadeService).getIcfInformation(any());
        }

        @Test
        void shallReturnIcfInformation() {
            when(icfFacadeService.getIcfInformation(any())).thenReturn(new IcfResponseDTO());
            IcfRequestDTO request = new IcfRequestDTO();
            final var response = icfController.getIcf(request);
            assertEquals(response.getStatus(), 200);
            assertTrue(response.getEntity() instanceof IcfResponseDTO);
        }
    }
}
