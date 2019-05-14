/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

import java.util.Arrays;
import java.util.stream.Collectors;

import javax.persistence.OptimisticLockException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;

import com.google.common.base.Strings;

import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.xmldsig.service.FakeSignatureServiceImpl;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.underskrift.UnderskriftService;
import se.inera.intyg.webcert.web.service.underskrift.model.SignMethod;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.api.dto.KlientSignaturRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.SignaturStateDTO;

@Path("/signature")
public class SignatureApiController extends AbstractApiController {

    private static final Logger LOG = LoggerFactory.getLogger(SignatureApiController.class);

    private static final String LAST_SAVED_DRAFT = "lastSavedDraft";

    @Autowired
    private UnderskriftService underskriftService;

    @Autowired
    private MonitoringLogService monitoringLogService;

    @Autowired(required = false)
    private FakeSignatureServiceImpl fakeSignatureService;

    @POST
    @Path("/{intygsTyp}/{intygsId}/{version}/signeringshash/{signMethod}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public SignaturStateDTO signeraUtkast(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId,
            @PathParam("version") long version, @PathParam("signMethod") String signMethodStr) {

        SignMethod signMethod = null;
        try {
            signMethod = SignMethod.valueOf(signMethodStr);
        } catch (IllegalArgumentException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MISSING_PARAMETER,
                    "Parameter signMethod is missing or has illegal value. Allowed values are: "
                            + Arrays.asList(SignMethod.values()).stream()
                                    .map(SignMethod::name)
                                    .collect(Collectors.joining(", ")));
        }

        try {
            SignaturBiljett sb = underskriftService.startSigningProcess(intygsId, intygsTyp, version, signMethod);
            return convertToSignatureStateDTO(sb);
        } catch (OptimisticLockException | OptimisticLockingFailureException e) {
            monitoringLogService.logUtkastConcurrentlyEdited(intygsId, intygsTyp);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.CONCURRENT_MODIFICATION, e.getMessage());
        }
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
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM })
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
