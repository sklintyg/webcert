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

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.security.common.model.AuthenticationMethod;
import se.inera.intyg.webcert.web.service.underskrift.grp.GrpUnderskriftServiceImpl;
import se.inera.intyg.webcert.web.service.underskrift.model.SignMethod;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.underskrift.xmldsig.XmlUnderskriftServiceImpl;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;

@Service
@RequiredArgsConstructor
public class CreateSignatureTicketService {

    private final XmlUnderskriftServiceImpl xmlUnderskriftService;
    private final GrpUnderskriftServiceImpl grpUnderskriftService;
    private final WebCertUserService webCertUserService;

    public SignaturBiljett create(String certificateId, String certificateType, long version, SignMethod signMethod,
        String ticketID, String certificateXml) {
        final var user = webCertUserService.getUser();
        final var ticket = getTicket(user.getAuthenticationMethod(), certificateId, certificateType, version,
            signMethod, ticketID, certificateXml);

        if (ticket == null) {
            throw new IllegalStateException("Unhandled authentication method, could not create SignaturBiljett");
        }

        if (ticket.getSignMethod() == SignMethod.GRP) {
            grpUnderskriftService.startGrpCollectPoller(user.getPersonId(), ticket);
        }

        return ticket;
    }

    private SignaturBiljett getTicket(AuthenticationMethod authenticationMethod, String certificateId, String certificateType, long version,
        SignMethod signMethod, String ticketID, String certificateXml) {
        switch (authenticationMethod) {
            case FAKE:
            case SITHS:
            case NET_ID:
                return xmlUnderskriftService.skapaSigneringsBiljettMedDigest(
                    certificateId, certificateType, version, Optional.empty(), signMethod, ticketID, certificateXml);
            case BANK_ID:
            case MOBILT_BANK_ID: {
                return grpUnderskriftService.skapaSigneringsBiljettMedDigest(certificateId, certificateType, version,
                    Optional.empty(), signMethod, ticketID, null);
            }
            default:
                throw new IllegalStateException(
                    String.format("AuthenticationMethod not supported '%s'", authenticationMethod)
                );
        }
    }
}
