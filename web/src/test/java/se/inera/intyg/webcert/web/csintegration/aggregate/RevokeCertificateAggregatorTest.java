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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.web.service.facade.RevokeCertificateFacadeService;

@ExtendWith(MockitoExtension.class)
class RevokeCertificateAggregatorTest {

    private static final Certificate CERTIFICATE = new Certificate();
    private static final String ID = "ID";
    private static final String MESSAGE = "MESSAGE";
    private static final String REASON = "REASON";

    RevokeCertificateFacadeService revokeCertificateFromWC;
    RevokeCertificateFacadeService revokeCertificateFromCS;
    RevokeCertificateFacadeService aggregator;

    @BeforeEach
    void setup() {
        revokeCertificateFromWC = mock(RevokeCertificateFacadeService.class);
        revokeCertificateFromCS = mock(RevokeCertificateFacadeService.class);

        aggregator = new RevokeCertificateAggregator(
            revokeCertificateFromWC,
            revokeCertificateFromCS);
    }

    @Test
    void shouldRevokeFromCSIfExists() {
        when(revokeCertificateFromCS.revokeCertificate(ID, REASON, MESSAGE))
            .thenReturn(CERTIFICATE);
        aggregator.revokeCertificate(ID, REASON, MESSAGE);

        Mockito.verify(revokeCertificateFromWC, times(0)).revokeCertificate(ID, REASON, MESSAGE);
    }

    @Test
    void shouldRevokeFromWCIfCertificateDoesNotExistInCS() {
        aggregator.revokeCertificate(ID, REASON, MESSAGE);

        Mockito.verify(revokeCertificateFromWC, times(1)).revokeCertificate(ID, REASON, MESSAGE);
    }
}
