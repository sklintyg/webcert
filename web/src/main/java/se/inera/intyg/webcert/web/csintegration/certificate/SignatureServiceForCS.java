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
import java.util.ConcurrentModificationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateXmlResponseDTO;
import se.inera.intyg.webcert.web.csintegration.util.PDLLogService;
import se.inera.intyg.webcert.web.service.underskrift.UnderskriftService;
import se.inera.intyg.webcert.web.service.underskrift.model.SignMethod;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;

@Slf4j
@RequiredArgsConstructor
@Service("signatureServiceForCS")
public class SignatureServiceForCS implements UnderskriftService {

    private final CSIntegrationService csIntegrationService;
    private final CSIntegrationRequestFactory csIntegrationRequestFactory;
    private final FakeSignatureServiceCS fakeSignatureServiceCS;
    private final CreateSignatureTicketService createSignatureTicketService;
    private final PDLLogService pdlLogService;


    @Override
    public SignaturBiljett startSigningProcess(String certificateId, String certificateType, long version, SignMethod signMethod,
        String ticketID, boolean isWc2ClientRequest) {
        final var exists = csIntegrationService.certificateExists(certificateId);
        if (Boolean.FALSE.equals(exists)) {
            log.debug("Certificate with id '{}' does not exist in certificate service", certificateId);
            return null;
        }

        final var certificateXml = csIntegrationService.getCertificateXml(
            csIntegrationRequestFactory.getCertificateXmlRequest(),
            certificateId
        );

        if (version != certificateXml.getVersion()) {
            throw new ConcurrentModificationException(
                String.format("Version '%s' did not match version '%s'", version, certificateXml.getVersion())
            );
        }

        return createSignatureTicketService.create(
            certificateId,
            certificateType,
            version,
            signMethod,
            ticketID,
            isWc2ClientRequest,
            getDecodedXmlData(certificateXml)
        );
    }

    @Override
    public SignaturBiljett fakeSignature(String certificateId, String certificateType, long version, String ticketId) {
        final var exists = csIntegrationService.certificateExists(certificateId);
        if (Boolean.FALSE.equals(exists)) {
            log.debug("Certificate with id '{}' does not exist in certificate service", certificateId);
            return null;
        }

        final var finalizedCertificateSignature = fakeSignatureServiceCS.finalizeFakeSignature(ticketId);

        pdlLogService.logSign(
            finalizedCertificateSignature.getCertificate()
        );

        return finalizedCertificateSignature.getSignaturBiljett();
    }


    @Override
    public SignaturBiljett netidSignature(String biljettId, byte[] signatur, String certifikat) {
        return null;
    }

    @Override
    public SignaturBiljett grpSignature(String biljettId, byte[] signatur) {
        return null;
    }

    @Override
    public SignaturBiljett signeringsStatus(String ticketId) {
        return null;
    }

    private static String getDecodedXmlData(GetCertificateXmlResponseDTO certificateXml) {
        return new String(Base64.getDecoder().decode(certificateXml.getXml()), StandardCharsets.UTF_8);
    }
}
