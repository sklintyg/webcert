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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.service.underskrift.UnderskriftService;
import se.inera.intyg.webcert.web.service.underskrift.model.SignMethod;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;

@Slf4j
@RequiredArgsConstructor
@Service("signatureServiceForCS")
public class SignatureServiceForCS implements UnderskriftService {

    private final CSIntegrationService csIntegrationService;

    @Override
    public SignaturBiljett startSigningProcess(String intygsId, String intygsTyp, long version, SignMethod signMethod, String ticketID,
        boolean isWc2ClientRequest) {
        final var exists = csIntegrationService.certificateExists(intygsId);
        if (Boolean.FALSE.equals(exists)) {
            log.debug("Certificate with id '{}' does not exist in certificate service", intygsId);
            return null;
        }

        // hämta XML från certificate service

        return null;
    }

    @Override
    public SignaturBiljett fakeSignature(String intygsId, String intygsTyp, long version, String ticketId) {
        return null;
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
}
