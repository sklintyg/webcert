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

package se.inera.intyg.webcert.web.csintegration.aggregate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.modules.support.facade.dto.ValidationErrorDTO;
import se.inera.intyg.webcert.web.service.facade.ValidateCertificateFacadeService;

@ExtendWith(MockitoExtension.class)
class ValidateCertificateAggregatorTest {

    private static final Certificate CERTIFICATE = new Certificate();
    private static final ValidationErrorDTO[] VALIDATION_ERRORS_WC = {new ValidationErrorDTO()};
    private static final ValidationErrorDTO[] VALIDATION_ERRORS_CS = {new ValidationErrorDTO()};

    ValidateCertificateFacadeService fromWC;
    ValidateCertificateFacadeService fromCS;
    ValidateCertificateFacadeService aggregator;

    @BeforeEach
    void setup() {
        fromWC = mock(ValidateCertificateFacadeService.class);
        fromCS = mock(ValidateCertificateFacadeService.class);

        aggregator = new ValidateCertificateAggregator(
            fromWC,
            fromCS
        );
    }

    @Test
    void shouldReturnValidationErrorsFromCSIfExists() {
        when(fromCS.validate(CERTIFICATE))
            .thenReturn(VALIDATION_ERRORS_CS);

        final var response = aggregator.validate(CERTIFICATE);
        verify(fromCS, times(1)).validate(CERTIFICATE);

        assertEquals(VALIDATION_ERRORS_CS, response);
    }

    @Test
    void shouldReturnValidationErrorsFromWCIfCertificateDoesNotExistInCS() {
        when(fromWC.validate(CERTIFICATE))
            .thenReturn(VALIDATION_ERRORS_WC);

        final var response = aggregator.validate(CERTIFICATE);
        verify(fromWC, times(1)).validate(CERTIFICATE);

        assertEquals(VALIDATION_ERRORS_WC, response);
    }
}
