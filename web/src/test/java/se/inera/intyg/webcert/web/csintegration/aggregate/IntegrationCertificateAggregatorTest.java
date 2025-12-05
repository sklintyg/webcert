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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.csintegration.certificate.IntegrationServiceForCS;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.integration.IntegrationService;
import se.inera.intyg.webcert.web.web.controller.integration.IntegrationServiceImpl;
import se.inera.intyg.webcert.web.web.controller.integration.dto.PrepareRedirectToIntyg;

@ExtendWith(MockitoExtension.class)
class IntegrationCertificateAggregatorTest {

    private static final String ID = "ID";
    private static final PrepareRedirectToIntyg PREPARE_REDIRECT_TO_INTYG_FROM_CS = new PrepareRedirectToIntyg();
    private static final PrepareRedirectToIntyg PREPARE_REDIRECT_TO_INTYG_FROM_WC = new PrepareRedirectToIntyg();
    private static final WebCertUser USER = new WebCertUser();
    private static final Personnummer ALTERNATE_SSN = Personnummer.createPersonnummer("191212121212").orElseThrow();

    IntegrationService integrationServiceFromWC;
    IntegrationService integrationServiceFromCS;
    IntegrationCertificateAggregator aggregator;

    @BeforeEach
    void setup() {
        integrationServiceFromWC = mock(IntegrationServiceImpl.class);
        integrationServiceFromCS = mock(IntegrationServiceForCS.class);

        aggregator = new IntegrationCertificateAggregator(
            integrationServiceFromWC,
            integrationServiceFromCS
        );
    }

    @Test
    void shouldReturnPrepareRedirectToIntygFromCSResponseFromCSNotNull() {
        when(integrationServiceFromCS.prepareRedirectToIntyg(ID, USER, ALTERNATE_SSN))
            .thenReturn(PREPARE_REDIRECT_TO_INTYG_FROM_CS);

        final var response = aggregator.prepareRedirectToIntyg(ID, USER, ALTERNATE_SSN);
        verify(integrationServiceFromCS, times(1)).prepareRedirectToIntyg(ID, USER, ALTERNATE_SSN);

        assertEquals(PREPARE_REDIRECT_TO_INTYG_FROM_CS, response);
    }

    @Test
    void shouldReturnPrepareRedirectToIntygFromWCIfResponseFromCSIsNull() {
        when(integrationServiceFromWC.prepareRedirectToIntyg(ID, USER, ALTERNATE_SSN))
            .thenReturn(PREPARE_REDIRECT_TO_INTYG_FROM_CS);

        final var response = aggregator.prepareRedirectToIntyg(ID, USER, ALTERNATE_SSN);
        verify(integrationServiceFromWC, times(1)).prepareRedirectToIntyg(ID, USER, ALTERNATE_SSN);

        assertEquals(PREPARE_REDIRECT_TO_INTYG_FROM_WC, response);
    }

    @Test
    void shouldSetPrepareBeforeAlternateSsnToNull() {
        final var argumentCaptor = ArgumentCaptor.forClass(Personnummer.class);
        when(integrationServiceFromCS.prepareRedirectToIntyg(ID, USER, null))
            .thenReturn(PREPARE_REDIRECT_TO_INTYG_FROM_CS);

        aggregator.prepareRedirectToIntyg(ID, USER);

        verify(integrationServiceFromCS, times(1)).prepareRedirectToIntyg(
            eq(ID),
            eq(USER),
            argumentCaptor.capture()
        );

        assertNull(argumentCaptor.getValue());
    }
}
