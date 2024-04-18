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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.webcert.web.csintegration.certificate.CreateDraftCertificateFromCS;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.integration.interactions.createdraftcertificate.v3.CreateDraftCertificateFromWC;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.CreateDraftCertificateResponseType;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.TypAvIntyg;

@ExtendWith(MockitoExtension.class)
class CreateDraftCertificateAggregatorTest {

    private static final String CERTIFICATE_TYPE = "CERTIFICATE_TYPE";
    private Intyg intyg;
    private static final String HSA_ID = "HSA_ID";
    private static final IntygUser USER = new IntygUser(HSA_ID);
    @Mock
    private CreateDraftCertificateFromWC createDraftCertificateFromWC;
    @Mock
    private CreateDraftCertificateFromCS createDraftCertificateFromCS;
    @Mock
    private CertificateServiceProfile certificateServiceProfile;
    @InjectMocks
    private CreateDraftCertificateAggregator createDraftCertificateAggregator;

    @BeforeEach
    void setUp() {
        intyg = new Intyg();
        intyg.setTypAvIntyg(new TypAvIntyg());
        intyg.getTypAvIntyg().setCode(CERTIFICATE_TYPE);
    }

    @Test
    void shouldReturnResponseFromCSIfProfileActiveAndSupportsType() {
        final var expectedResult = new CreateDraftCertificateResponseType();
        when(certificateServiceProfile.activeAndSupportsType(CERTIFICATE_TYPE))
            .thenReturn(true);
        when(createDraftCertificateFromCS.create(intyg, USER))
            .thenReturn(expectedResult);

        final var response = createDraftCertificateAggregator.create(intyg, USER);
        verify(createDraftCertificateFromCS, times(1)).create(intyg, USER);

        assertEquals(expectedResult, response);
    }

    @Test
    void shouldReturnResponseFromWCIfProfileNotActiveAndNotSupportsType() {
        final var expectedResult = new CreateDraftCertificateResponseType();
        when(certificateServiceProfile.activeAndSupportsType(CERTIFICATE_TYPE))
            .thenReturn(false);
        when(createDraftCertificateFromWC.create(intyg, USER))
            .thenReturn(expectedResult);

        final var response = createDraftCertificateAggregator.create(intyg, USER);
        verify(createDraftCertificateFromWC, times(1)).create(intyg, USER);
        verify(createDraftCertificateFromCS, times(0)).create(intyg, USER);

        assertEquals(expectedResult, response);
    }
}
