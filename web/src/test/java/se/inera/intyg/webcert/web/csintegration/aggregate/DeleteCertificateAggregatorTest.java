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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.facade.DeleteCertificateFacadeService;

@ExtendWith(MockitoExtension.class)
class DeleteCertificateAggregatorTest {

    private static final String ID = "ID";
    private static final int VERSION = 10;

    DeleteCertificateFacadeService deleteCertificateFromWC;
    DeleteCertificateFacadeService deleteCertificateFromCS;
    CertificateServiceProfile certificateServiceProfile;
    DeleteCertificateFacadeService aggregator;
    @Mock
    GetCertificateAggregator getCertificateAggregator;

    @BeforeEach
    void setup() {
        deleteCertificateFromWC = mock(DeleteCertificateFacadeService.class);
        deleteCertificateFromCS = mock(DeleteCertificateFacadeService.class);
        certificateServiceProfile = mock(CertificateServiceProfile.class);

        aggregator = new DeleteCertificateAggregator(
            deleteCertificateFromWC,
            deleteCertificateFromCS,
            certificateServiceProfile,
            getCertificateAggregator);
    }

    @Test
    void shouldDeleteFromWebcertIfProfileIsInactive() {
        aggregator.deleteCertificate(ID, VERSION);

        Mockito.verify(deleteCertificateFromWC).deleteCertificate(ID, VERSION);
    }

    @Nested
    class ActiveProfile {

        @BeforeEach
        void setup() {
            when(certificateServiceProfile.active())
                .thenReturn(true);
        }

        @Test
        void shouldDeleteFromCSIfProfileIsActiveAndCertificateExistsInCS() {
            when(getCertificateAggregator.getCertificate(ID, false, true))
                .thenReturn(new Certificate());
            aggregator.deleteCertificate(ID, VERSION);

            Mockito.verify(deleteCertificateFromCS).deleteCertificate(ID, VERSION);
        }

        @Test
        void shouldDeleteFromWCIfProfileIsInactiveAndCertificateDoesNotExistInCS() {
            aggregator.deleteCertificate(ID, VERSION);

            Mockito.verify(deleteCertificateFromWC).deleteCertificate(ID, VERSION);
        }
    }
}