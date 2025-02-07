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
package se.inera.intyg.webcert.web.service.facade.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.modules.support.facade.TypeAheadEnum;
import se.inera.intyg.infra.integration.postnummer.service.PostnummerService;

@ExtendWith(MockitoExtension.class)
class DefaultTypeAheadProviderTest {

    @Mock
    private PostnummerService postnummerService;

    @InjectMocks
    private DefaultTypeAheadProvider defaultTypeAheadProvider;

    @Test
    void shallReturnMunicipalitiesWhenAskedFor() {
        final var expectedValues = List.of("Östersund", "Strömsund", "Stockholm");
        doReturn(expectedValues).when(postnummerService).getKommunList();
        final var actualValues = defaultTypeAheadProvider.getValues(TypeAheadEnum.MUNICIPALITIES);
        assertEquals(expectedValues, actualValues);
    }

    @Test
    void shallThrowExceptionWhenGivenNullParameter() {
        assertThrows(IllegalArgumentException.class, () -> defaultTypeAheadProvider.getValues(null));
    }
}
