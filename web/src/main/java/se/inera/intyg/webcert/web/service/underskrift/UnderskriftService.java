/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.underskrift;

import se.inera.intyg.webcert.web.service.underskrift.model.SignMethod;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;

public interface UnderskriftService {

    SignaturBiljett startSigningProcess(String intygsId, String intygsTyp, long version, SignMethod signMethod, String ticketID,
        boolean isWc2ClientRequest);

    SignaturBiljett fakeSignature(String intygsId, String intygsTyp, long version, String ticketId);

    SignaturBiljett netidSignature(String biljettId, byte[] signatur, String certifikat);

    SignaturBiljett grpSignature(String biljettId, byte[] signatur);

    SignaturBiljett signeringsStatus(String ticketId);
}
