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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.integration.interactions.createdraftcertificate.v3.CreateDraftCertificate;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.CreateDraftCertificateResponseType;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.Intyg;

@ExtendWith(MockitoExtension.class)
class CreateDraftCertificateAggregatorTest {

    private static final Intyg UTKAST_PARAMS = new Intyg();
    private static final String HSA_ID = "HSA_ID";
    private static final IntygUser USER = new IntygUser(HSA_ID);
    @Mock
    private CreateDraftCertificate createDraftCertificateFromWC;
    @Mock
    private CreateDraftCertificate createDraftCertificateFromCS;
    @Mock
    private CertificateServiceProfile certificateServiceProfile;
    private CreateDraftCertificate createDraftCertificateAggregator;

    @BeforeEach
    void setUp() {
        createDraftCertificateAggregator = new CreateDraftCertificateAggregator(
            createDraftCertificateFromWC,
            createDraftCertificateFromCS,
            certificateServiceProfile
        );
    }

    @Test
    void shouldReturnResponseFromCSIfProfileActiveAndSupportsType() {
        final var expectedResult = new CreateDraftCertificateResponseType();
        when(certificateServiceProfile.active())
            .thenReturn(true);
        when(createDraftCertificateFromCS.create(UTKAST_PARAMS, USER))
            .thenReturn(expectedResult);

        final var response = createDraftCertificateAggregator.create(UTKAST_PARAMS, USER);
        verify(createDraftCertificateFromCS, times(1)).create(UTKAST_PARAMS, USER);

        assertEquals(expectedResult, response);
    }

    @Test
    void shouldReturnCertificateIdFromWCIfCSProfileIsNotActive() {
        final var expectedResult = new CreateDraftCertificateResponseType();
        when(certificateServiceProfile.active())
            .thenReturn(false);
        when(createDraftCertificateFromWC.create(UTKAST_PARAMS, USER))
            .thenReturn(expectedResult);

        final var response = createDraftCertificateAggregator.create(UTKAST_PARAMS, USER);
        verify(createDraftCertificateFromWC, times(1)).create(UTKAST_PARAMS, USER);
        verify(createDraftCertificateFromCS, times(0)).create(UTKAST_PARAMS, USER);

        assertEquals(expectedResult, response);
    }

    @Test
    void shouldReturnCertificateIdFromWCIIfCSProfileIsActiveButReturnsNull() {
        final var expectedResult = new CreateDraftCertificateResponseType();
        when(certificateServiceProfile.active())
            .thenReturn(true);
        when(createDraftCertificateFromWC.create(UTKAST_PARAMS, USER))
            .thenReturn(expectedResult);

        final var response = createDraftCertificateAggregator.create(UTKAST_PARAMS, USER);

        verify(createDraftCertificateFromWC, times(1)).create(UTKAST_PARAMS, USER);
        verify(createDraftCertificateFromCS, times(1)).create(UTKAST_PARAMS, USER);

        assertEquals(expectedResult, response);
    }
}
