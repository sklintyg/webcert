/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.service.facade.ValidateSickLeavePeriodFacadeService;
import se.inera.intyg.webcert.web.service.fmb.FmbDiagnosInformationService;
import se.inera.intyg.webcert.web.web.controller.api.dto.FmbResponse;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ValidateSickLeavePeriodRequestDTO;

@ExtendWith(MockitoExtension.class)
class FMBControllerTest {

  @Mock private ValidateSickLeavePeriodFacadeService validateSickLeavePeriodFacadeService;
  @Mock private FmbDiagnosInformationService fmbDiagnosInformationService;

  @InjectMocks private FMBController fmbController;

  @Nested
  class ValidateSickLeavePeriod {

    @Test
    void shallValidateSickLeavePeriod() {
      ValidateSickLeavePeriodRequestDTO request = new ValidateSickLeavePeriodRequestDTO();
      fmbController.validateSickLeavePeriod(request);
      verify(validateSickLeavePeriodFacadeService).validateSickLeavePeriod(any());
    }
  }

  @Nested
  class GetFmbData {

    @Test
    void shouldReturnBadContentIfIcd10IsNull() {
      final var result = fmbController.getFmbForIcd10(null);
      assertEquals(400, result.getStatusCode().value());
    }

    @Test
    void shouldReturnFmbResponse() {
      final var icd10 = "icd10";
      final var expectedResponse = mock(FmbResponse.class);

      when(fmbDiagnosInformationService.findFmbDiagnosInformationByIcd10Kod(icd10))
          .thenReturn(Optional.of(expectedResponse));
      final var result = fmbController.getFmbForIcd10(icd10);
      assertEquals(200, result.getStatusCode().value());
      assertEquals(expectedResponse, result.getBody());
    }
  }
}
