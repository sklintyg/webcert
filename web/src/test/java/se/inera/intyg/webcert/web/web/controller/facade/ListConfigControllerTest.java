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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.service.facade.list.config.ListDraftsConfigFacadeServiceImpl;
import se.inera.intyg.webcert.web.service.facade.list.config.ListSignedCertificatesConfigFacadeServiceImpl;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
public class ListConfigControllerTest {

    @Mock
    private ListDraftsConfigFacadeServiceImpl listDraftsConfigFacadeService;
    @Mock
    private ListSignedCertificatesConfigFacadeServiceImpl listSignedCertificatesConfigFacadeService;
    @InjectMocks
    private ListConfigController listConfigController;

    @Nested
    class ListDraftsConfig {

        final ListConfig config = new ListConfig();

        @BeforeEach
        void setup() {
            doReturn(config)
                .when(listDraftsConfigFacadeService)
                .get();
        }

        @Test
        void shallIncludeConfigInResponse() {
            final var response = listConfigController.getListOfDraftsConfig().getEntity();
            assertEquals(config, response);
        }
    }

    @Nested
    class ListSignedCertificatesConfig {

        final ListConfig config = new ListConfig();

        @BeforeEach
        void setup() {
            doReturn(config)
                .when(listSignedCertificatesConfigFacadeService)
                .get();
        }

        @Test
        void shallIncludeConfigInResponse() {
            final var response = listConfigController.getListOfSignedCertificatesConfig().getEntity();
            assertEquals(config, response);
        }
    }
}
