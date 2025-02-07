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
package se.inera.intyg.webcert.web.web.controller.internalapi;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.webcert.web.service.privatlakaravtal.AvtalService;

@RunWith(MockitoJUnitRunner.class)
public class TermsApiControllerTest {

    @Mock
    private AvtalService avtalService;

    @InjectMocks
    private TermsApiController termsApiController;

    @Test
    public void shouldReturnFetchedValue() {
        when(avtalService.userHasApprovedLatestAvtal(any(String.class))).thenReturn(true);

        final var response = termsApiController.getWebcertTermsApproved("HSA_ID");

        assertTrue(response);
    }

}
