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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateRequestDTO;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.integration.dto.PrepareRedirectToIntyg;

@ExtendWith(MockitoExtension.class)
class IntegrationServiceForCSTest {

    private static final String CERTIFICATE_ID = "certificateId";
    private static final String CERTIFICATE_TYPE = "certificateType";
    private static final String TYPE_VERSION = "typeVersion";
    private static final String PERSON_ID = "191212121212";
    private static final Personnummer PERSONAL_NUMBER = Personnummer.createPersonnummer(PERSON_ID).orElseThrow();
    private static final GetCertificateRequestDTO GET_CERTIFICATE_REQUEST_DTO = GetCertificateRequestDTO.builder().build();
    @Mock
    private CSIntegrationRequestFactory csIntegrationRequestFactory;
    @Mock
    private WebCertUser user;
    @Mock
    private LogSjfService logSjfService;
    @Mock
    private CSIntegrationService csIntegrationService;
    @Mock
    private CertificateDetailsUpdateService certificateDetailsUpdateService;
    @InjectMocks
    private IntegrationServiceForCS integrationServiceForCS;

    private Certificate certificate;

    @BeforeEach
    void setUp() {
        certificate = new Certificate();
        certificate.setMetadata(
            CertificateMetadata.builder()
                .id(CERTIFICATE_ID)
                .type(CERTIFICATE_TYPE)
                .typeVersion(TYPE_VERSION)
                .build()
        );
    }

    @Test
    void shallReturnNullIfCertificateDontExistInCS() {
        doReturn(false).when(csIntegrationService).certificateExists(CERTIFICATE_ID);
        assertNull(integrationServiceForCS.prepareRedirectToIntyg(CERTIFICATE_ID, user));
    }

    @Test
    void shallLogSjfIfActive() {
        doReturn(true).when(csIntegrationService).certificateExists(CERTIFICATE_ID);
        doReturn(GET_CERTIFICATE_REQUEST_DTO).when(csIntegrationRequestFactory).getCertificateRequest();
        doReturn(certificate).when(csIntegrationService).getCertificate(CERTIFICATE_ID, GET_CERTIFICATE_REQUEST_DTO);
        doReturn(true).when(user).isSjfActive();

        integrationServiceForCS.prepareRedirectToIntyg(CERTIFICATE_ID, user);
        verify(logSjfService, times(1)).log(certificate, user);
    }

    @Test
    void shallNotLogSjfIfNotActive() {
        doReturn(true).when(csIntegrationService).certificateExists(CERTIFICATE_ID);
        doReturn(GET_CERTIFICATE_REQUEST_DTO).when(csIntegrationRequestFactory).getCertificateRequest();
        doReturn(certificate).when(csIntegrationService).getCertificate(CERTIFICATE_ID, GET_CERTIFICATE_REQUEST_DTO);
        doReturn(false).when(user).isSjfActive();

        integrationServiceForCS.prepareRedirectToIntyg(CERTIFICATE_ID, user);
        verifyNoInteractions(logSjfService);
    }

    @Test
    void shallCallCertificateDetailsUpdateService() {
        doReturn(true).when(csIntegrationService).certificateExists(CERTIFICATE_ID);
        doReturn(GET_CERTIFICATE_REQUEST_DTO).when(csIntegrationRequestFactory).getCertificateRequest();
        doReturn(certificate).when(csIntegrationService).getCertificate(CERTIFICATE_ID, GET_CERTIFICATE_REQUEST_DTO);
        doReturn(false).when(user).isSjfActive();

        integrationServiceForCS.prepareRedirectToIntyg(CERTIFICATE_ID, user, PERSONAL_NUMBER);
        verify(certificateDetailsUpdateService).update(certificate, user, PERSONAL_NUMBER);
    }

    @Test
    void shallReturnPrepareRedirectToIntyg() {
        final var expectedRedirect = new PrepareRedirectToIntyg();
        expectedRedirect.setIntygTyp(CERTIFICATE_TYPE);
        expectedRedirect.setIntygId(CERTIFICATE_ID);
        expectedRedirect.setIntygTypeVersion(TYPE_VERSION);

        doReturn(true).when(csIntegrationService).certificateExists(CERTIFICATE_ID);
        doReturn(GET_CERTIFICATE_REQUEST_DTO).when(csIntegrationRequestFactory).getCertificateRequest();
        doReturn(certificate).when(csIntegrationService).getCertificate(CERTIFICATE_ID, GET_CERTIFICATE_REQUEST_DTO);
        doReturn(false).when(user).isSjfActive();

        final var actualRedirect = integrationServiceForCS.prepareRedirectToIntyg(CERTIFICATE_ID, user, PERSONAL_NUMBER);
        assertEquals(expectedRedirect, actualRedirect);
    }

    @Test
    void shallThrowWebcertServiceExceptionForAuthorizationProblemIfStatusCode403FromCS() {
        doReturn(true).when(csIntegrationService).certificateExists(CERTIFICATE_ID);
        doThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN)).when(csIntegrationRequestFactory).getCertificateRequest();

        final var e = assertThrows(WebCertServiceException.class, () ->
            integrationServiceForCS.prepareRedirectToIntyg(CERTIFICATE_ID, user, PERSONAL_NUMBER));

        assertEquals(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM, e.getErrorCode());
    }

    @Test
    void shallRethrowExceptionIfNotStatusCode403FromCS() {
        doReturn(true).when(csIntegrationService).certificateExists(CERTIFICATE_ID);
        doThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR)).when(csIntegrationRequestFactory).getCertificateRequest();

        final var e = assertThrows(HttpClientErrorException.class, () ->
            integrationServiceForCS.prepareRedirectToIntyg(CERTIFICATE_ID, user, PERSONAL_NUMBER));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatusCode());
    }
}
