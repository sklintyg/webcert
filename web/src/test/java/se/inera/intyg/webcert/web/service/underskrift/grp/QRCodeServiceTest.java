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

package se.inera.intyg.webcert.web.service.underskrift.grp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import se.inera.intyg.common.support.common.enumerations.SignaturTyp;
import se.inera.intyg.webcert.web.service.underskrift.model.SignMethod;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;

class QRCodeServiceTest {

    private static final String TICKET_ID = "8ede9b72-fd3c-40c1-8f11-5b1247fba40d";
    private static final String QR_START_SECRET = "57ba556f-6cba-46a4-8c2a-d20922384e72";
    private static final String QR_START_TOKEN = "975536e2-d2a9-4c14-9a3e-6ef59cebb761";
    // CHECKSTYLE:OFF LineLength
    private static final String RESPONSE_ZERO = "bankid.975536e2-d2a9-4c14-9a3e-6ef59cebb761.0.ab439184935deb40fcea78e6d51036ed641152749440cd774871735867b08db8";
    private static final String RESPONSE_FIVE = "bankid.975536e2-d2a9-4c14-9a3e-6ef59cebb761.5.335722d7b446328f471f23c0f618c509b73ef2bd7b0956ae5b87f3be98bb1b4a";
    // CHECKSTYLE:ON LineLength

    private final QRCodeService qrCodeService = new QRCodeService();

    @Test
    void shouldReturnNullIfNotSignMethodGRP() {
        final var ticket = getTicket(LocalDateTime.now());
        ticket.setSignMethod(SignMethod.SIGN_SERVICE);
        assertNull(qrCodeService.qrCodeForBankId(ticket));
    }

    @Test
    void shouldReturnQrCodeForTimeZeroSeconds() {
        final var ticket = getTicket(LocalDateTime.now());
        final var qrCode = qrCodeService.qrCodeForBankId(ticket);
        assertEquals(RESPONSE_ZERO, qrCode);
    }

    @Test
    void shouldReturnQrCodeForTimeFiveSeconds() {
        final var ticket = getTicket(LocalDateTime.now().minusSeconds(5L));
        final var qrCode = qrCodeService.qrCodeForBankId(ticket);
        assertEquals(RESPONSE_FIVE, qrCode);
    }

    @Test
    void shouldThrowIllegalStateExceptionIfMethodFailure() {
        final var ticket = getTicket(LocalDateTime.now().minusSeconds(0L));
        ticket.setQrStartSecret(null);
        assertThrows(IllegalStateException.class, () -> qrCodeService.qrCodeForBankId(ticket));
    }

    private SignaturBiljett getTicket(LocalDateTime time) {
        return SignaturBiljett.SignaturBiljettBuilder
            .aSignaturBiljett(TICKET_ID, SignaturTyp.PKCS7, SignMethod.GRP)
            .withSkapad(time)
            .withQrStartSecret(QR_START_SECRET)
            .withQrStartToken(QR_START_TOKEN)
            .build();
    }

}
