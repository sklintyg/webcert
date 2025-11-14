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

package se.inera.intyg.webcert.web.web.controller.api;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.integration.postnummer.model.Omrade;
import se.inera.intyg.infra.integration.postnummer.service.PostnummerService;

@ExtendWith(MockitoExtension.class)
class ConfigApiControllerTest {

    private static final String ZIP_CODE = "12345";

    @Mock
    private PostnummerService postnummerService;

    @InjectMocks
    private ConfigApiController configApiController;

    @Test
    void shouldGetAreaByZipCodeIfExists() {

        when(postnummerService.getOmradeByPostnummer(ZIP_CODE)).thenReturn(List.of(
            new Omrade(ZIP_CODE, "City", "Municipality", "County")
        ));

        final var response = configApiController.getAreaByZipCode(ZIP_CODE);

        assertAll(
            () -> assertEquals(ZIP_CODE, response.getFirst().zipCode()),
            () -> assertEquals("City", response.getFirst().city()),
            () -> assertEquals("Municipality", response.getFirst().municipality()),
            () -> assertEquals("County", response.getFirst().county())
        );
    }

    @Test
    void shouldReturnEmptyListIfAreaByZipCodeNotFound() {
        when(postnummerService.getOmradeByPostnummer(ZIP_CODE)).thenReturn(List.of());

        final var response = configApiController.getAreaByZipCode(ZIP_CODE);

        assertEquals(0, response.size());

    }

}