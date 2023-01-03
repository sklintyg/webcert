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

import com.google.common.base.Strings;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.OptimisticLockException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.xmldsig.service.FakeSignatureServiceImpl;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.underskrift.UnderskriftService;
import se.inera.intyg.webcert.web.service.underskrift.dss.DssMetadataService;
import se.inera.intyg.webcert.web.service.underskrift.dss.DssSignMessageService;
import se.inera.intyg.webcert.web.service.underskrift.dss.DssSignRequestDTO;
import se.inera.intyg.webcert.web.service.underskrift.dss.DssSignatureService;
import se.inera.intyg.webcert.web.service.underskrift.model.SignMethod;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
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
    private static final String PARAM_CERT_ID = "certId";

    @Autowired
    private ReactUriFactory reactUriFactory;

    @Autowired
    private UnderskriftService underskriftService;

    @Autowired
    private MonitoringLogService monitoringLogService;

    @Autowired(required = false)
    private FakeSignatureServiceImpl fakeSignatureService;

    @Autowired
    private DssMetadataService dssMetadataService;

    @Autowired
    private DssSignatureService dssSignatureService;

    @Autowired
    private DssSignMessageService dssSignMessageService;

    @Autowired
    private UtkastService utkastService;

    @POST
    @Path("/{intygsTyp}/{intygsId}/{version}/signeringshash/{signMethod}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public SignaturStateDTO signeraUtkast(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId,
        @PathParam("version") long version, @PathParam("signMethod") String signMethodStr, @Context HttpServletRequest request) {

        SignMethod signMethod = null;
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

            SignaturBiljett sb = underskriftService.startSigningProcess(intygsId, intygsTyp, version, signMethod, ticketId,
                isWc2ClientRequest(request));

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

    private boolean isWc2ClientRequest(HttpServletRequest request) {
        final var refererHeader = request.getHeader("referer");
        return refererHeader != null && refererHeader.contains("wc2.");
    }

    private Response getRedirectResponseWithReturnUrl(SignaturBiljett signaturBiljett, UriInfo uriInfo) {
        final var redirectUri = getRedirectUri(signaturBiljett, uriInfo);
        return Response.seeOther(redirectUri).build();
    }

    private URI getRedirectUri(SignaturBiljett signaturBiljett, UriInfo uriInfo) {
        if (signaturBiljett.isWc2ClientRequest()) {
            return reactUriFactory.uriForCertificate(uriInfo, signaturBiljett.getIntygsId());
        }

        return getRedirectUriForAngularClient(signaturBiljett);
    }

    private URI getRedirectUriForAngularClient(SignaturBiljett signaturBiljett) {
        String returnUrl;
        if (SignaturStatus.ERROR.equals(signaturBiljett.getStatus())) {
            returnUrl = dssSignatureService.findReturnErrorUrl(signaturBiljett.getIntygsId(), signaturBiljett.getTicketId());
        } else {
            returnUrl = dssSignatureService.findReturnUrl(signaturBiljett.getIntygsId());
        }
        return URI.create(returnUrl);
    }

    private SignaturStateDTO convertToSignatureStateDTO(SignaturBiljett sb) {
        return SignaturStateDTO.SignaturStateDTOBuilder.aSignaturStateDTO()
            .withId(sb.getTicketId())
            .withIntygsId(sb.getIntygsId())
            .withStatus(sb.getStatus())
            .withVersion(sb.getVersion())
            .withSignaturTyp(sb.getSignaturTyp())
            .withHash(sb.getHash()) // This is what you stuff into NetiD SIGN.
            .build();
    }

    @GET
    @Path(SIGN_SERVICE_METADATA_PATH)
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
    public SignaturStateDTO signeringsStatus(@PathParam("intygsTyp") String intygsTyp, @PathParam("ticketId") String ticketId) {
        authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp).features(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST)
            .orThrow();
        SignaturBiljett sb = underskriftService.signeringsStatus(ticketId);

        return convertToSignatureStateDTO(sb);
    }

    @POST
    @Path("/{intygsTyp}/{biljettId}/signeranetidplugin")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM})
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
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
