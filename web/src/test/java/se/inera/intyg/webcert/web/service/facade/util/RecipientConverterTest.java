/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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

import org.junit.jupiter.api.Test;

class RecipientConverterTest {

    @Test
    void shallReturnNameForFKASSA() {
        assertEquals("Försäkringskassan", RecipientConverter.getRecipientName("FKASSA"));
    }

    @Test
    void shallReturnNameForTRANSP() {
        assertEquals("Transportstyrelsen", RecipientConverter.getRecipientName("TRANSP"));
    }

    @Test
    void shallReturnNameForSKV() {
        assertEquals("Skatteverket", RecipientConverter.getRecipientName("SKV"));
    }

    @Test
    void shallReturnNameForSOS() {
        assertEquals("Socialstyrelsen", RecipientConverter.getRecipientName("SOS"));
    }

    @Test
    void shallReturnNameForAF() {
        assertEquals("Arbetsförmedlingen", RecipientConverter.getRecipientName("AF"));
    }

    @Test
    void shallReturnNameForAG() {
        assertEquals("Arbetsgivaren", RecipientConverter.getRecipientName("AG"));
    }

    @Test
    void shallReturnUnknownNameIfDoesntExist() {
        assertEquals("okänd mottagare", RecipientConverter.getRecipientName("DOESNTEXISTS"));
    }
}