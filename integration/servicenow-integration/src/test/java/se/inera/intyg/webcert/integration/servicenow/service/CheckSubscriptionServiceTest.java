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

package se.inera.intyg.webcert.integration.servicenow.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import se.inera.intyg.webcert.integration.api.subscription.AuthenticationMethodEnum;

class CheckSubscriptionServiceTest {

    private CheckSubscriptionService checkSubscriptionService;

    private static final List<String> ELEG_SERVICE_CODES = List.of("Webcert fristående med e-legitimation");
    private static final List<String> SITHS_SERVICE_CODES = List.of("Webcert fristående med SITHS-kort", "Webcert Integrerad - via agent",
        "Webcert Integrerad - via region", "Webcert integrerad - direktanslutning");

    @BeforeEach
    public void setup() {
        checkSubscriptionService = new CheckSubscriptionService();
        ReflectionTestUtils.setField(checkSubscriptionService, CheckSubscriptionService.class, "elegServiceCodes",
            ELEG_SERVICE_CODES, List.class);
        ReflectionTestUtils.setField(checkSubscriptionService, CheckSubscriptionService.class, "sithsServiceCodes",
            SITHS_SERVICE_CODES, List.class);
    }

    @Test
    void shouldReturnTrueIfServiceCodesIsEmpty() {
        assertTrue(checkSubscriptionService.isMissing(Collections.emptyList(), AuthenticationMethodEnum.ELEG));
    }

    @Test
    void shouldReturnTrueIfNoneMatchForEleg() {
        assertTrue(checkSubscriptionService.isMissing(List.of("SITHS-kort"), AuthenticationMethodEnum.ELEG));
    }

    @Test
    void shouldReturnFalseIfMatchForEleg() {
        assertTrue(checkSubscriptionService.isMissing(List.of("e-legitimation"), AuthenticationMethodEnum.ELEG));
    }

    @Test
    void shouldReturnTrueIfNoneMatchForSiths() {
        assertTrue(checkSubscriptionService.isMissing(List.of("e-legitimation"), AuthenticationMethodEnum.SITHS));
    }

    @Test
    void shouldReturnFalseIfMatchForSiths() {
        assertTrue(checkSubscriptionService.isMissing(List.of("SITHS-kort"), AuthenticationMethodEnum.SITHS));
    }
}
