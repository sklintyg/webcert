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

package se.inera.intyg.webcert.web.csintegration.aggregate;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.underskrift.UnderskriftService;
import se.inera.intyg.webcert.web.service.underskrift.model.SignMethod;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;

@Service("signatureAggregator")
public class SignatureAggregator implements UnderskriftService {

    private final CertificateServiceProfile certificateServiceProfile;
    private final UnderskriftService signatureServiceForWC;
    private final UnderskriftService signatureServiceForCS;

    public SignatureAggregator(CertificateServiceProfile certificateServiceProfile,
        @Qualifier("signatureServiceForWC") UnderskriftService signatureServiceForWC,
        @Qualifier("signatureServiceForCS") UnderskriftService signatureServiceForCS) {
        this.certificateServiceProfile = certificateServiceProfile;
        this.signatureServiceForWC = signatureServiceForWC;
        this.signatureServiceForCS = signatureServiceForCS;
    }

    @Override
    public SignaturBiljett startSigningProcess(String intygsId, String intygsTyp, long version, SignMethod signMethod, String ticketID,
        boolean isWc2ClientRequest) {
        if (!certificateServiceProfile.active()) {
            return signatureServiceForWC.startSigningProcess(intygsId, intygsTyp, version, signMethod,
                ticketID, isWc2ClientRequest);
        }

        final var signaturBiljett = signatureServiceForCS.startSigningProcess(intygsId, intygsTyp, version, signMethod, ticketID,
            isWc2ClientRequest);

        return signaturBiljett != null ? signaturBiljett
            : signatureServiceForWC.startSigningProcess(intygsId, intygsTyp, version, signMethod, ticketID,
                isWc2ClientRequest);
    }

    @Override
    public SignaturBiljett fakeSignature(String intygsId, String intygsTyp, long version, String ticketId) {
        if (!certificateServiceProfile.active()) {
            return signatureServiceForWC.fakeSignature(intygsId, intygsTyp, version, ticketId);
        }

        final var signaturBiljett = signatureServiceForCS.fakeSignature(intygsId, intygsTyp, version, ticketId);

        return signaturBiljett != null ? signaturBiljett
            : signatureServiceForWC.fakeSignature(intygsId, intygsTyp, version, ticketId);
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
