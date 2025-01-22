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
package se.inera.intyg.webcert.web.csintegration.certificate;

import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateEncodingException;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.xmldsig.service.FakeSignatureService;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.underskrift.BaseXMLSignatureService;

@Service
@RequiredArgsConstructor
public class FakeSignatureServiceCS extends BaseXMLSignatureService {

    private final FakeSignatureService fakeSignatureService;

    public FinalizedCertificateSignature finalizeFakeSignature(String ticketId) {
        final var ticket = redisTicketTracker.findBiljett(ticketId);
        if (ticket == null) {
            throw new IllegalStateException("No ticket found in Redis for " + ticketId);
        }

        final var base64EncodedSignedInfoXml = Base64.getEncoder()
            .encodeToString(ticket.getIntygSignature().getSigningData().getBytes(StandardCharsets.UTF_8));

        final var fakeSignatureData = fakeSignatureService.createSignature(base64EncodedSignedInfoXml);
        final var x509Certificate = fakeSignatureService.getX509Certificate();

        try {
            final var finalizedCertificateSignature = finalizeXMLDSigSignatureForCS(
                Base64.getEncoder().encodeToString(x509Certificate.getEncoded()),
                ticket,
                Base64.getDecoder().decode(fakeSignatureData)
            );

            redisTicketTracker.updateStatus(
                finalizedCertificateSignature.getSignaturBiljett().getTicketId(),
                finalizedCertificateSignature.getSignaturBiljett().getStatus()
            );

            return FinalizedCertificateSignature.builder()
                .signaturBiljett(finalizedCertificateSignature.getSignaturBiljett())
                .certificate(finalizedCertificateSignature.getCertificate())
                .build();

        } catch (CertificateEncodingException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, e.getMessage());
        }
    }
}
