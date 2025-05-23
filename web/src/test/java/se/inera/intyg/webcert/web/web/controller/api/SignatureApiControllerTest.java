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
package se.inera.intyg.webcert.web.web.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.inera.intyg.webcert.web.service.underskrift.model.SignMethod.SIGN_SERVICE;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.UUID;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import se.inera.intyg.common.support.common.enumerations.SignaturTyp;
import se.inera.intyg.infra.xmldsig.model.ValidationResponse;
import se.inera.intyg.infra.xmldsig.model.ValidationResult;
import se.inera.intyg.infra.xmldsig.service.FakeSignatureService;
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
    private FakeSignatureService fakeSignatureService;
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
    private static final String WC_URI_ERROR = "https://wc.localtest.me/sign";
    private static final String USER_IP_ADDRESS = "127.0.0.1";

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
    public void shouldFetchRedirectUrl() {
        final var signatureTicket = getSignatureTicket(SignaturStatus.SIGNERAD);

        when(dssSignMessageService.validateDssMessageSignature(any(String.class))).thenReturn(getOkValidationResponse());
        when(dssSignatureService.receiveSignResponse(any(String.class), any(String.class))).thenReturn(signatureTicket);
        when(reactUriFactory.uriForCertificate(uriInfo, signatureTicket.getIntygsId())).thenReturn(URI.create(WC_URI));

        final var response = signatureApiController.signServiceResponse(uriInfo, "relayState", "eidSignResponse");

        verify(reactUriFactory, times(1)).uriForCertificate(uriInfo, CERT_ID);
        verify(dssSignatureService, times(0)).findReturnUrl(any());
        assertEquals(HttpStatus.SC_SEE_OTHER, response.getStatus());
        assertEquals(WC_URI, response.getLocation().toString());
    }

    @Test
    public void shouldFetchRedirectUrlWithErrorStatus() {
        final var signatureTicket = getSignatureTicket(SignaturStatus.ERROR);

        when(dssSignMessageService.validateDssMessageSignature(any(String.class))).thenReturn(getOkValidationResponse());
        when(dssSignatureService.receiveSignResponse(any(String.class), any(String.class))).thenReturn(signatureTicket);
        when(
            reactUriFactory.uriForCertificateWithSignError(uriInfo, signatureTicket.getIntygsId(), signatureTicket.getStatus())).thenReturn(
            URI.create(WC_URI_ERROR));

        final var response = signatureApiController.signServiceResponse(uriInfo, "relayState", "eidSignResponse");

        verify(reactUriFactory, times(1)).uriForCertificateWithSignError(uriInfo, CERT_ID, signatureTicket.getStatus());
        verify(dssSignatureService, times(0)).findReturnUrl(any());
        assertEquals(HttpStatus.SC_SEE_OTHER, response.getStatus());
        assertEquals(WC_URI_ERROR, response.getLocation().toString());
    }

    private void setupMocksForSignDraft() {
        final var signatureTicket = getSignatureTicket(SignaturStatus.SIGNERAD);
        when(dssSignatureService.createTransactionID()).thenReturn(TICKET_ID);
        when(dssSignatureService.createSignatureRequestDTO(signatureTicket)).thenReturn(getSignRequestDto());
        when(underskriftService.startSigningProcess(CERT_ID, CERT_TYPE, VERSION, SIGN_SERVICE, TICKET_ID, USER_IP_ADDRESS))
            .thenReturn(signatureTicket);
    }

    private DssSignRequestDTO getSignRequestDto() {
        final var dssSignRequestDto = new DssSignRequestDTO();
        dssSignRequestDto.setTransactionId(TICKET_ID);
        dssSignRequestDto.setSignRequest("signRequest");
        dssSignRequestDto.setActionUrl("actionUrl");
        return dssSignRequestDto;
    }

    private SignaturBiljett getSignatureTicket(SignaturStatus status) {
        return SignaturBiljett.SignaturBiljettBuilder.aSignaturBiljett(TICKET_ID, SignaturTyp.XMLDSIG, SIGN_SERVICE)
            .withIntygsId(CERT_ID)
            .withStatus(status)
            .build();
    }

    private ValidationResponse getOkValidationResponse() {
        return ValidationResponse.ValidationResponseBuilder.aValidationResponse()
            .withReferencesValid(ValidationResult.OK)
            .withSignatureValid(ValidationResult.OK)
            .build();
    }
}
