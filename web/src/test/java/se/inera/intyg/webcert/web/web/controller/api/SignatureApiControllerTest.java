/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.inera.intyg.webcert.web.service.underskrift.model.SignMethod.SIGN_SERVICE;

import java.net.URI;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import se.inera.intyg.common.support.common.enumerations.SignaturTyp;
import se.inera.intyg.infra.xmldsig.model.ValidationResponse;
import se.inera.intyg.infra.xmldsig.model.ValidationResult;
import se.inera.intyg.infra.xmldsig.service.FakeSignatureServiceImpl;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.underskrift.UnderskriftService;
import se.inera.intyg.webcert.web.service.underskrift.dss.DssMetadataService;
import se.inera.intyg.webcert.web.service.underskrift.dss.DssSignMessageService;
import se.inera.intyg.webcert.web.service.underskrift.dss.DssSignRequestDTO;
import se.inera.intyg.webcert.web.service.underskrift.dss.DssSignatureService;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus;
import se.inera.intyg.webcert.web.web.controller.facade.util.ReactUriFactory;

@ExtendWith(MockitoExtension.class)
public class SignatureApiControllerTest {

    @Mock
    private MonitoringLogService mockMonitoringService;
    @Mock
    private FakeSignatureServiceImpl mockFakeSignatureServiceImpl;
    @Mock
    private DssMetadataService dssMetadataService;
    @Mock
    private UriInfo uriInfo;
    @Mock
    private DssSignMessageService dssSignMessageService;
    @Mock
    private DssSignatureService dssSignatureService;
    @Mock
    private ReactUriFactory reactUriFactory;
    @Mock
    private UnderskriftService underskriftService;

    @InjectMocks
    private SignatureApiController signatureApiController;

    @Captor
    private ArgumentCaptor<Boolean> clientCaptor;

    private static final String CERT_ID = UUID.randomUUID().toString();
    private static final String CERT_TYPE = "lisjp";
    private static final String TICKET_ID = "ticketId";
    private static final String WC_URI = "https://wc.localtest.me";
    private static final String WC2_URI = "https://wc2.wc.localtest.me";

    private static final Boolean WC2_CLIENT_TRUE = true;
    private static final Boolean WC2_CLIENT_FALSE = false;

    private static final long VERSION = 3L;

    @Test
    public void signServiceResponse() {
        // TODO
    }

    @Test
    public void signServiceClientMetadata() {
        when(dssMetadataService.getClientMetadataAsString()).thenReturn("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Test>Inera AB</Test>");

        Response response = signatureApiController.signServiceClientMetadata();

        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals("application/samlmetadata+xml", response.getMediaType().toString());
        assertEquals("attachment; filename=\"wc_dss_client_metadata.xml\"", response.getHeaderString(HttpHeaders.CONTENT_DISPOSITION));
    }

    @Test
    public void shouldSetWc2ClientTrueWhenWc2InRefererHeader() {
        final var request = getHttpServletRequest(WC2_URI);
        setupMocksForSignDraft(WC2_CLIENT_TRUE);

        signatureApiController.signeraUtkast(CERT_TYPE, CERT_ID, VERSION, SIGN_SERVICE.name(), request);

        verify(underskriftService, times(1)).startSigningProcess(eq(CERT_ID), eq(CERT_TYPE), eq(VERSION), eq(SIGN_SERVICE),
            eq(TICKET_ID), clientCaptor.capture());
        assertEquals(WC2_CLIENT_TRUE, clientCaptor.getValue());
    }

    @Test
    public void shouldSetWc2ClientFalseWhenNotWc2InRefererHeader() {
        final var request = getHttpServletRequest(WC_URI);
        setupMocksForSignDraft(WC2_CLIENT_FALSE);

        signatureApiController.signeraUtkast(CERT_TYPE, CERT_ID, VERSION, SIGN_SERVICE.name(), request);

        verify(underskriftService, times(1)).startSigningProcess(eq(CERT_ID), eq(CERT_TYPE), eq(VERSION), eq(SIGN_SERVICE),
            eq(TICKET_ID), clientCaptor.capture());
        assertEquals(WC2_CLIENT_FALSE, clientCaptor.getValue());
    }

    @Test
    public void shouldSetWc2ClientFalseWhenRefererHeaderMissing() {
        final var request = new MockHttpServletRequest();
        setupMocksForSignDraft(WC2_CLIENT_FALSE);

        signatureApiController.signeraUtkast(CERT_TYPE, CERT_ID, VERSION, SIGN_SERVICE.name(), request);

        verify(underskriftService, times(1)).startSigningProcess(eq(CERT_ID), eq(CERT_TYPE), eq(VERSION), eq(SIGN_SERVICE),
            eq(TICKET_ID), clientCaptor.capture());
        assertEquals(WC2_CLIENT_FALSE, clientCaptor.getValue());
    }

    @Test
    public void shouldFetchRedirectUrlForReactClient() {
        final var signatureTicket = getSignatureTicket(WC2_CLIENT_TRUE);

        when(dssSignMessageService.validateDssMessageSignature(any(String.class))).thenReturn(getOkValidationResponse());
        when(dssSignatureService.receiveSignResponse(any(String.class), any(String.class))).thenReturn(signatureTicket);
        when(reactUriFactory.uriForCertificate(uriInfo, signatureTicket.getIntygsId())).thenReturn(URI.create(WC2_URI));

        final var response = signatureApiController.signServiceResponse(uriInfo, "relayState", "eidSignResponse");

        verify(reactUriFactory, times(1)).uriForCertificate(uriInfo, CERT_ID);
        verify(dssSignatureService, times(0)).findReturnUrl(any());
        assertEquals(HttpStatus.SC_SEE_OTHER, response.getStatus());
        assertEquals(WC2_URI, response.getLocation().toString());
    }

    @Test
    public void shouldFetchRedirectUrlForAngularClient() {
        final var signatureTicket = getSignatureTicket(WC2_CLIENT_FALSE);

        when(dssSignMessageService.validateDssMessageSignature(any(String.class))).thenReturn(getOkValidationResponse());
        when(dssSignatureService.receiveSignResponse(any(String.class), any(String.class))).thenReturn(signatureTicket);
        when(dssSignatureService.findReturnUrl(any(String.class))).thenReturn(WC_URI);

        final var response = signatureApiController.signServiceResponse(uriInfo, "relayState", "eidSignResponse");

        verify(reactUriFactory, times(0)).uriForCertificate(uriInfo, CERT_ID);
        verify(dssSignatureService, times(1)).findReturnUrl(signatureTicket.getIntygsId());
        assertEquals(HttpStatus.SC_SEE_OTHER, response.getStatus());
        assertEquals(WC_URI, response.getLocation().toString());
    }

    private void setupMocksForSignDraft(boolean isWc2ClientRequest) {
        final var signatureTicket = getSignatureTicket(isWc2ClientRequest);
        when(dssSignatureService.createTransactionID()).thenReturn(TICKET_ID);
        when(dssSignatureService.createSignatureRequestDTO(signatureTicket)).thenReturn(getSignRequestDto());
        when(underskriftService.startSigningProcess(CERT_ID, CERT_TYPE, VERSION, SIGN_SERVICE, TICKET_ID, isWc2ClientRequest))
            .thenReturn(signatureTicket);
    }

    private DssSignRequestDTO getSignRequestDto() {
        final var dssSignRequestDto = new DssSignRequestDTO();
        dssSignRequestDto.setTransactionId(TICKET_ID);
        dssSignRequestDto.setSignRequest("signRequest");
        dssSignRequestDto.setActionUrl("actionUrl");
        return dssSignRequestDto;
    }

    private HttpServletRequest getHttpServletRequest(String refererHeader) {
        final var request = new MockHttpServletRequest();
        request.addHeader("referer", refererHeader);
        return request;
    }

    private SignaturBiljett getSignatureTicket(boolean isWc2ClientRequest) {
        return SignaturBiljett.SignaturBiljettBuilder.aSignaturBiljett(TICKET_ID, SignaturTyp.XMLDSIG, SIGN_SERVICE)
            .withIntygsId(CERT_ID)
            .withStatus(SignaturStatus.SIGNERAD)
            .withWc2ClientRequest(isWc2ClientRequest)
            .build();
    }

    private ValidationResponse getOkValidationResponse() {
        return ValidationResponse.ValidationResponseBuilder.aValidationResponse()
            .withReferencesValid(ValidationResult.OK)
            .withSignatureValid(ValidationResult.OK)
            .build();
    }
}
