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

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateRequestDTO;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@ExtendWith(MockitoExtension.class)
class IntegrationServiceForCSTest {

    private static final String CERTIFICATE_ID = "certificateId";
    private static final String CERTIFICATE_TYPE = "certificateType";
    @Mock
    private CSIntegrationRequestFactory csIntegrationRequestFactory;
    @Mock
    private WebCertUser user;
    @Mock
    private LogSjfService logSjfService;
    @Mock
    private CSIntegrationService csIntegrationService;
    @InjectMocks
    private IntegrationServiceForCS integrationServiceForCS;

    @Test
    void shallReturnNullIfCertificateDontExistInCS() {
        doReturn(false).when(csIntegrationService).certificateExists(CERTIFICATE_ID);
        assertNull(integrationServiceForCS.prepareRedirectToIntyg(CERTIFICATE_TYPE, CERTIFICATE_ID, user));
    }

    @Test
    void shallLogSjfIfActive() {
        final var getCertificateRequestDTO = GetCertificateRequestDTO.builder().build();
        final var certificate = new Certificate();
        doReturn(true).when(csIntegrationService).certificateExists(CERTIFICATE_ID);
        doReturn(getCertificateRequestDTO).when(csIntegrationRequestFactory).getCertificateRequest();
        doReturn(certificate).when(csIntegrationService).getCertificate(CERTIFICATE_ID, getCertificateRequestDTO);
        doReturn(true).when(user).isSjfActive();

        integrationServiceForCS.prepareRedirectToIntyg(CERTIFICATE_TYPE, CERTIFICATE_ID, user);
        verify(logSjfService, times(1)).log(certificate, user);
    }

    @Test
    void shallNotLogSjfIfNotActive() {
        final var getCertificateRequestDTO = GetCertificateRequestDTO.builder().build();
        final var certificate = new Certificate();
        doReturn(true).when(csIntegrationService).certificateExists(CERTIFICATE_ID);
        doReturn(getCertificateRequestDTO).when(csIntegrationRequestFactory).getCertificateRequest();
        doReturn(certificate).when(csIntegrationService).getCertificate(CERTIFICATE_ID, getCertificateRequestDTO);
        doReturn(false).when(user).isSjfActive();

        integrationServiceForCS.prepareRedirectToIntyg(CERTIFICATE_TYPE, CERTIFICATE_ID, user);
        verifyNoInteractions(logSjfService);
    }
}
