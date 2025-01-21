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

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.service.underskrift.model.SignMethod;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;

@Slf4j
@Service
public class QRCodeService {

    private static final String MAC_ALGORITHM = "HmacSHA256";
    private static final String FORMAT_STRING = "%064x";
    private static final String BANK_ID = "bankid";
    private static final String DELIMITER = ".";

    public String qrCodeForBankId(SignaturBiljett ticket) {
        final var qrStartSecret = ticket.getQrStartSecret();

        if (ticket.getSignMethod() != SignMethod.GRP) {
            return null;
        }

        try {
            final var ticketInstant = ticket.getSkapad().atZone(ZoneId.systemDefault()).toInstant();
            final var secondsElapsed = Long.toString(ticketInstant.until(Instant.now(), ChronoUnit.SECONDS));
            final var qrStartSecretBytes = qrStartSecret.getBytes(StandardCharsets.US_ASCII);
            final var qrAuthenticationCode = getMessageAuthenticationCode(secondsElapsed, qrStartSecretBytes);

            return String.join(DELIMITER, BANK_ID, ticket.getQrStartToken(), secondsElapsed, qrAuthenticationCode);

        } catch (Exception e) {
            final var message = String.format("Failure creating QR code for eleg signature, ticketId '%s'  certificateId '%s'.",
                ticket.getTicketId(), ticket.getIntygsId());
            throw new IllegalStateException(message, e);
        }
    }

    private String getMessageAuthenticationCode(String secondsElapsed, byte[] qrStartSecret) throws NoSuchAlgorithmException,
        InvalidKeyException {
        final var mac = Mac.getInstance(MAC_ALGORITHM);
        mac.init(new SecretKeySpec(qrStartSecret, MAC_ALGORITHM));
        mac.update(secondsElapsed.getBytes(StandardCharsets.US_ASCII));
        return String.format(FORMAT_STRING, new BigInteger(1, mac.doFinal()));
    }

}
