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

package se.inera.intyg.webcert.web.csintegration.aggregate;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.service.underskrift.UnderskriftService;
import se.inera.intyg.webcert.web.service.underskrift.model.SignMethod;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;

@Service("signAggregator")
public class SignAggregator implements UnderskriftService {

    private final UnderskriftService signServiceForWC;
    private final UnderskriftService signServiceForCS;

    public SignAggregator(
        @Qualifier("signServiceForWC") UnderskriftService signServiceForWC,
        @Qualifier("signServiceForCS") UnderskriftService signServiceForCS) {
        this.signServiceForWC = signServiceForWC;
        this.signServiceForCS = signServiceForCS;
    }

    @Override
    public SignaturBiljett startSigningProcess(String intygsId, String intygsTyp, long version, SignMethod signMethod, String ticketID,
        String userIpAddress) {
        final var signaturBiljett = signServiceForCS.startSigningProcess(intygsId, intygsTyp, version, signMethod, ticketID, userIpAddress);

        return signaturBiljett != null ? signaturBiljett
            : signServiceForWC.startSigningProcess(intygsId, intygsTyp, version, signMethod, ticketID, userIpAddress);
    }

    @Override
    public SignaturBiljett fakeSignature(String intygsId, String intygsTyp, long version, String ticketId) {
        final var signaturBiljett = signServiceForCS.fakeSignature(intygsId, intygsTyp, version, ticketId);

        return signaturBiljett != null ? signaturBiljett
            : signServiceForWC.fakeSignature(intygsId, intygsTyp, version, ticketId);
    }

    @Override
    public SignaturBiljett netidSignature(String biljettId, byte[] signatur, String certifikat) {
        final var signaturBiljett = signServiceForCS.netidSignature(biljettId, signatur, certifikat);

        return signaturBiljett != null ? signaturBiljett
            : signServiceForWC.netidSignature(biljettId, signatur, certifikat);
    }

    @Override
    public SignaturBiljett grpSignature(String biljettId, byte[] signatur) {
        final var signaturBiljett = signServiceForCS.grpSignature(biljettId, signatur);

        return signaturBiljett != null ? signaturBiljett
            : signServiceForWC.grpSignature(biljettId, signatur);
    }

    @Override
    public SignaturBiljett signeringsStatus(String ticketId) {
        return signServiceForWC.signeringsStatus(ticketId);
    }
}
