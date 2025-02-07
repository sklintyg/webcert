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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.service.facade.ValidateSickLeavePeriodFacadeService;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ValidateSickLeavePeriodRequestDTO;

@ExtendWith(MockitoExtension.class)
public class FMBControllerTest {

    @Mock
    private ValidateSickLeavePeriodFacadeService validateSickLeavePeriodFacadeService;

    @InjectMocks
    private FMBController fmbController;

    private static final String CERTIFICATE_ID = "XXXXXX-YYYYYYY-ZZZZZZZ-UUUUUUU";
    private static final long CERTIFICATE_VERSION = 1L;

    @Nested
    class ValidateSickLeavePeriod {

        @BeforeEach
        void setup() {
        }

        @Test
        void shallValidateSickLeavePeriod() {
            ValidateSickLeavePeriodRequestDTO request = new ValidateSickLeavePeriodRequestDTO();
            fmbController.validateSickLeavePeriod(request);
            verify(validateSickLeavePeriodFacadeService).validateSickLeavePeriod(any());
        }
    }
}
