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

package se.inera.intyg.webcert.web.service.facade.internalapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.CertificateText;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;

@ExtendWith(MockitoExtension.class)
class GetTextsForCertificateServiceTest {

    private static final CertificateText TEXT = CertificateText.builder().build();
    public static final String TYPE = "type";
    public static final String TYPE_VERSION = "typeVersion";

    @Mock
    IntygModuleRegistry intygModuleRegistry;

    @Mock
    ModuleApi moduleApi;

    @InjectMocks
    GetTextsForCertificateService getTextsForCertificateService;

    @Test
    void shouldSetPreambleText() throws ModuleNotFoundException {
        when(intygModuleRegistry.getModuleApi(TYPE, TYPE_VERSION))
            .thenReturn(moduleApi);
        when(moduleApi.getPreambleForCitizens()).thenReturn(TEXT);

        final var response = getTextsForCertificateService.get(TYPE, TYPE_VERSION);

        assertEquals(TEXT, response.get(0));
    }

    @Test
    void shouldThrowErrorIfProblemGettingModuleApi() throws ModuleNotFoundException {
        when(intygModuleRegistry.getModuleApi(anyString(), anyString()))
            .thenThrow(new ModuleNotFoundException());

        assertThrows(RuntimeException.class, () -> getTextsForCertificateService.get(TYPE, TYPE_VERSION));
    }
}
