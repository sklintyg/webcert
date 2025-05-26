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
import java.util.Base64;
import java.util.ConcurrentModificationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateXmlResponseDTO;
import se.inera.intyg.webcert.web.service.underskrift.UnderskriftService;
import se.inera.intyg.webcert.web.service.underskrift.grp.GrpSignatureService;
import se.inera.intyg.webcert.web.service.underskrift.model.SignMethod;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.underskrift.tracker.RedisTicketTracker;
import se.inera.intyg.webcert.web.service.underskrift.xmldsig.XmlUnderskriftServiceImpl;

@Slf4j
@RequiredArgsConstructor
@Service("signServiceForCS")
public class SignServiceForCS implements UnderskriftService {

    private static final String CERTIFICATE_WITH_ID_DOES_NOT_EXIST_IN_CERTIFICATE_SERVICE =
        "Certificate with id '{}' does not exist in certificate service";
    private final CSIntegrationService csIntegrationService;
    private final CSIntegrationRequestFactory csIntegrationRequestFactory;
    private final XmlUnderskriftServiceImpl xmlUnderskriftService;
    private final GrpSignatureService grpUnderskriftService;
    private final RedisTicketTracker redisTicketTracker;
    private final FakeSignatureServiceCS fakeSignatureServiceCS;
    private final CreateSignatureTicketService createSignatureTicketService;
    private final FinalizeCertificateSignService finalizeCertificateSignService;

    @Override
    public SignaturBiljett startSigningProcess(String certificateId, String certificateType, long version, SignMethod signMethod,
        String ticketID, String userIpAddress) {
        final var exists = csIntegrationService.certificateExists(certificateId);
        if (Boolean.FALSE.equals(exists)) {
            log.debug(CERTIFICATE_WITH_ID_DOES_NOT_EXIST_IN_CERTIFICATE_SERVICE, certificateId);
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
            userIpAddress,
            getDecodedXmlData(certificateXml)
        );
    }

    @Override
    public SignaturBiljett fakeSignature(String certificateId, String certificateType, long version, String ticketId) {
        final var exists = csIntegrationService.certificateExists(certificateId);
        if (Boolean.FALSE.equals(exists)) {
            log.debug(CERTIFICATE_WITH_ID_DOES_NOT_EXIST_IN_CERTIFICATE_SERVICE, certificateId);
            return null;
        }
        final var finalizedCertificateSignature = fakeSignatureServiceCS.finalizeFakeSignature(ticketId);
        finalizeCertificateSignService.finalizeSign(finalizedCertificateSignature.getCertificate());
        return finalizedCertificateSignature.getSignaturBiljett();
    }


    @Override
    public SignaturBiljett netidSignature(String biljettId, byte[] signatur, String certifikat) {
        final var ticket = getTicket(biljettId);
        final var exists = csIntegrationService.certificateExists(ticket.getIntygsId());
        if (Boolean.FALSE.equals(exists)) {
            log.debug(CERTIFICATE_WITH_ID_DOES_NOT_EXIST_IN_CERTIFICATE_SERVICE, ticket.getIntygsId());
            return null;
        }

        final var finalizedCertificateSignature = xmlUnderskriftService.finalizeSignatureForCS(ticket, signatur, certifikat);
        finalizeCertificateSignService.finalizeSign(finalizedCertificateSignature.getCertificate());
        return finalizedCertificateSignature.getSignaturBiljett();
    }

    @Override
    public SignaturBiljett grpSignature(String biljettId, byte[] signatur) {
        final var ticket = getTicket(biljettId);
        final var exists = csIntegrationService.certificateExists(ticket.getIntygsId());
        if (Boolean.FALSE.equals(exists)) {
            log.debug(CERTIFICATE_WITH_ID_DOES_NOT_EXIST_IN_CERTIFICATE_SERVICE, ticket.getIntygsId());
            return null;
        }

        final var finalizedCertificateSignature = grpUnderskriftService.finalizeSignatureForCS(ticket, signatur, null);
        finalizeCertificateSignService.finalizeSign(finalizedCertificateSignature.getCertificate());
        return finalizedCertificateSignature.getSignaturBiljett();
    }

    @Override
    public SignaturBiljett signeringsStatus(String ticketId) {
        final var ticket = redisTicketTracker.findBiljett(ticketId);
        if (ticket == null) {
            log.error("No SignaturBiljett found for ticketId '{}'", ticketId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND,
                "No SignaturBiljett found for ticketId '" + ticketId + "'");
        }
        return ticket;
    }

    private static String getDecodedXmlData(GetCertificateXmlResponseDTO certificateXml) {
        return new String(Base64.getDecoder().decode(certificateXml.getXml()), StandardCharsets.UTF_8);
    }

    private SignaturBiljett getTicket(String biljettId) {
        final var ticket = redisTicketTracker.findBiljett(biljettId);
        if (ticket == null) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE,
                String.format(
                    "No SignaturBiljett found for ticketId '%s' when finalizing signature.", biljettId
                )
            );
        }
        return ticket;
    }
}
