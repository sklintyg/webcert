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
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.xmldsig.model.IntygXMLDSignature;
import se.inera.intyg.infra.xmldsig.service.PrepareSignatureService;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.service.underskrift.BaseSignatureService;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus;

@Service
@RequiredArgsConstructor
public class SignCertificateService extends BaseSignatureService {

    private final PrepareSignatureService prepareSignatureService;
    private final CSIntegrationService csIntegrationService;
    private final CSIntegrationRequestFactory csIntegrationRequestFactory;


    public SignaturBiljett sign(SignaturBiljett ticket, String certificateXml, String signatureXml) {
        validateSignature(ticket, certificateXml);

        csIntegrationService.signCertificate(
            csIntegrationRequestFactory.signCertificateRequest(certificateXml, signatureXml, ticket.getVersion()),
            ticket.getIntygsId()
        );

        ticket.setStatus(SignaturStatus.SIGNERAD);
        return ticket;
    }

    private void validateSignature(SignaturBiljett ticket, String certificateXml) {
        final var intygXmldSignature = (IntygXMLDSignature) ticket.getIntygSignature();
        final var signingXmlHash = Base64.getEncoder()
            .encodeToString(intygXmldSignature.getSignatureType().getSignedInfo().getReference().get(0).getDigestValue());

        final var transformAndDigestResponse = prepareSignatureService
            .transformAndGenerateDigest(certificateXml, ticket.getIntygsId());

        checkDigests(ticket.getIntygsId(), signingXmlHash, new String(transformAndDigestResponse.getDigest(), StandardCharsets.UTF_8));
    }
}
