/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.csintegration.certificate;

import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateEncodingException;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.xmldsig.service.FakeSignatureServiceImpl;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.underskrift.BaseXMLSignatureService;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@Service
@RequiredArgsConstructor
public class FakeSignatureServiceCS extends BaseXMLSignatureService {

    private final FakeSignatureServiceImpl fakeSignatureService;

    public SignaturBiljett finalizeFakeSignature(String ticketId, WebCertUser user, String certificateXml) {
        SignaturBiljett biljett = redisTicketTracker.findBiljett(ticketId);
        if (biljett == null) {
            throw new IllegalStateException("No ticket found in Redis for " + ticketId);
        }

        final var base64EncodedSignedInfoXml = Base64.getEncoder()
            .encodeToString(biljett.getIntygSignature().getSigningData().getBytes(StandardCharsets.UTF_8));

        final var fakeSignatureData = fakeSignatureService.createSignature(base64EncodedSignedInfoXml);
        final var x509Certificate = fakeSignatureService.getX509Certificate();

        try {
            biljett = finalizeXMLDSigSignature(Base64.getEncoder().encodeToString(x509Certificate.getEncoded()), user, biljett,
                Base64.getDecoder().decode(fakeSignatureData), certificateXml);
            return redisTicketTracker.updateStatus(biljett.getTicketId(), biljett.getStatus());
        } catch (CertificateEncodingException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, e.getMessage());
        }
    }
}
