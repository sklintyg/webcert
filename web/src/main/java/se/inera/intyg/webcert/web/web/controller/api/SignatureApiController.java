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
package se.inera.intyg.webcert.web.web.controller.api;

import com.google.common.base.Strings;
import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.underskrift.UnderskriftService;
import se.inera.intyg.webcert.web.service.underskrift.dss.DssMetadataService;
import se.inera.intyg.webcert.web.service.underskrift.dss.DssSignMessageService;
import se.inera.intyg.webcert.web.service.underskrift.dss.DssSignRequestDTO;
import se.inera.intyg.webcert.web.service.underskrift.dss.DssSignatureService;
import se.inera.intyg.webcert.web.service.underskrift.grp.QRCodeService;
import se.inera.intyg.webcert.web.service.underskrift.model.SignMethod;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.api.dto.KlientSignaturRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.SignaturStateDTO;
import se.inera.intyg.webcert.web.web.controller.api.dto.SignaturStateDTO.SignaturStateDTOBuilder;
import se.inera.intyg.webcert.web.web.controller.facade.util.ReactUriFactory;

@Path("/signature")
public class SignatureApiController extends AbstractApiController {

    public static final String SIGNATUR_API_CONTEXT_PATH = "/api/signature";
    public static final String SIGN_SERVICE_RESPONSE_PATH = "/signservice/v1/response";
    public static final String SIGN_SERVICE_METADATA_PATH = "/signservice/v1/metadata";
    private static final Logger LOG = LoggerFactory.getLogger(SignatureApiController.class);
    private static final String LAST_SAVED_DRAFT = "lastSavedDraft";

    @Autowired
    private ReactUriFactory reactUriFactory;

    @Autowired
    @Qualifier("signAggregator")
    private UnderskriftService underskriftService;

    @Autowired
    private MonitoringLogService monitoringLogService;

    @Autowired
    private DssMetadataService dssMetadataService;

    @Autowired
    private DssSignatureService dssSignatureService;

    @Autowired
    private DssSignMessageService dssSignMessageService;

    @Autowired
    private QRCodeService qrCodeService;

    @POST
    @Path("/{intygsTyp}/{intygsId}/{version}/signeringshash/{signMethod}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "signature-sign-draft", eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
    public SignaturStateDTO signeraUtkast(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId,
        @PathParam("version") long version, @PathParam("signMethod") String signMethodStr, @Context HttpServletRequest request) {

        SignMethod signMethod;
        try {
            signMethod = SignMethod.valueOf(signMethodStr);
        } catch (IllegalArgumentException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MISSING_PARAMETER,
                "Parameter signMethod is missing or has illegal value. Allowed values are: "
                    + Arrays.stream(SignMethod.values())
                    .map(SignMethod::name)
                    .collect(Collectors.joining(", ")));
        }

        try {
            String ticketId;

            if (SignMethod.SIGN_SERVICE.equals(signMethod)) {
                ticketId = dssSignatureService.createTransactionID();
            } else {
                ticketId = UUID.randomUUID().toString();
            }

            SignaturBiljett sb = underskriftService.startSigningProcess(intygsId, intygsTyp, version, signMethod, ticketId);

            if (SignMethod.SIGN_SERVICE.equals(signMethod)) {
                DssSignRequestDTO signRequestDTO = dssSignatureService.createSignatureRequestDTO(sb);

                monitoringLogService.logSignRequestCreated(signRequestDTO.getTransactionId(), intygsId);

                return SignaturStateDTOBuilder.aSignaturStateDTO().withId(signRequestDTO.getTransactionId())
                    .withActionUrl(signRequestDTO.getActionUrl())
                    .withSignRequest(signRequestDTO.getSignRequest()).build();

            } else {
                return convertToSignatureStateDTO(sb);
            }
        } catch (OptimisticLockException | OptimisticLockingFailureException e) {
            monitoringLogService.logUtkastConcurrentlyEdited(intygsId, intygsTyp);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.CONCURRENT_MODIFICATION, e.getMessage());
        }
    }

    @POST
    @Path(SIGN_SERVICE_RESPONSE_PATH)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "signature-sign-service-response", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public Response signServiceResponse(@Context UriInfo uriInfo, @FormParam("RelayState") String relayState,
        @FormParam("EidSignResponse") String eidSignResponse) {
        SignaturBiljett signaturBiljett;
        monitoringLogService.logSignResponseReceived(relayState);

        String signResponseString = "";
        try {
            signResponseString = new String(Base64.getDecoder().decode(eidSignResponse), StandardCharsets.UTF_8);
        } catch (Exception e) {
            signaturBiljett = dssSignatureService.updateSignatureTicketWithError(relayState);
            monitoringLogService.logSignResponseInvalid(relayState, signaturBiljett.getIntygsId(),
                "Could not decode sign response: " + e.getMessage());
            return getRedirectResponseWithReturnUrl(signaturBiljett, uriInfo);
        }

        var validationResponse = dssSignMessageService.validateDssMessageSignature(signResponseString);

        if (!validationResponse.isValid()) {
            signaturBiljett = dssSignatureService.updateSignatureTicketWithError(relayState);
            monitoringLogService.logSignResponseInvalid(relayState, signaturBiljett.getIntygsId(),
                "Validation of sign response signature failed!");
            return getRedirectResponseWithReturnUrl(signaturBiljett, uriInfo);
        }

        signaturBiljett = dssSignatureService.receiveSignResponse(relayState, signResponseString);

        logIfSuccess(relayState, signaturBiljett);

        return getRedirectResponseWithReturnUrl(signaturBiljett, uriInfo);
    }

    private void logIfSuccess(String relayState, SignaturBiljett signaturBiljett) {
        if (signaturBiljett.getStatus() == SignaturStatus.SIGNERAD) {
            monitoringLogService.logSignResponseSuccess(relayState, signaturBiljett.getIntygsId());
        }
    }

    private Response getRedirectResponseWithReturnUrl(SignaturBiljett signaturBiljett, UriInfo uriInfo) {
        final var redirectUri = getRedirectUri(signaturBiljett, uriInfo);
        return Response.seeOther(redirectUri).build();
    }

    private URI getRedirectUri(SignaturBiljett signaturBiljett, UriInfo uriInfo) {
        return signaturBiljett.getStatus().equals(SignaturStatus.ERROR)
            ? reactUriFactory.uriForCertificateWithSignError(uriInfo, signaturBiljett.getIntygsId(), signaturBiljett.getStatus())
            : reactUriFactory.uriForCertificate(uriInfo, signaturBiljett.getIntygsId());
    }

    private SignaturStateDTO convertToSignatureStateDTO(SignaturBiljett sb) {
        return SignaturStateDTO.SignaturStateDTOBuilder.aSignaturStateDTO()
            .withId(sb.getTicketId())
            .withIntygsId(sb.getIntygsId())
            .withStatus(sb.getStatus())
            .withVersion(sb.getVersion())
            .withSignaturTyp(sb.getSignaturTyp())
            .withAutoStartToken(sb.getAutoStartToken())
            .withQrCode(qrCodeService.qrCodeForBankId(sb))
            .withHash(sb.getHash()) // This is what you stuff into NetiD SIGN.
            .build();
    }

    @GET
    @Path(SIGN_SERVICE_METADATA_PATH)
    @PerformanceLogging(eventAction = "signature-sign-service-client-metadata", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public Response signServiceClientMetadata() {

        String clientMetadataAsString = dssMetadataService.getClientMetadataAsString();

        ResponseBuilder responseBuilder = Response.ok(clientMetadataAsString)
            .header(HttpHeaders.CONTENT_TYPE, "application/samlmetadata+xml")
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"wc_dss_client_metadata.xml\"");

        return responseBuilder.build();
    }

    @GET
    @Path("/{intygsTyp}/{ticketId}/signeringsstatus")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PerformanceLogging(eventAction = "signature-sign-status", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public SignaturStateDTO signeringsStatus(@PathParam("intygsTyp") String intygsTyp, @PathParam("ticketId") String ticketId) {
        final var sb = underskriftService.signeringsStatus(ticketId);
        return convertToSignatureStateDTO(sb);
    }

    @POST
    @Path("/{intygsTyp}/{biljettId}/signeranetidplugin")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM})
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PerformanceLogging(eventAction = "signature-client-sign-draft", eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
    public SignaturStateDTO klientSigneraUtkast(@PathParam("intygsTyp") String intygsTyp, @PathParam("biljettId") String biljettId,
        @Context HttpServletRequest request, KlientSignaturRequest signaturRequest) {

        LOG.debug("Signerar intyg med biljettId {}", biljettId);

        if (signaturRequest.getSignatur() == null) {
            LOG.error("Inkommande signaturRequest saknar signatur");
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, "Signatur saknas");
        }
        if (Strings.isNullOrEmpty(signaturRequest.getCertifikat())) {
            LOG.error("Inkommande signaturRequest saknar x509 certifikat.");
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, "Certifikat saknas");
        }

        SignaturBiljett sb = null;
        try {
            sb = underskriftService.netidSignature(biljettId, signaturRequest.getSignatur(), signaturRequest.getCertifikat());
        } catch (OptimisticLockException | OptimisticLockingFailureException e) {
            sb = underskriftService.signeringsStatus(biljettId);
            monitoringLogService.logUtkastConcurrentlyEdited(sb.getIntygsId(), intygsTyp);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.CONCURRENT_MODIFICATION, e.getMessage());
        }

        request.getSession(true).removeAttribute(LAST_SAVED_DRAFT);

        return convertToSignatureStateDTO(sb);
    }
}
