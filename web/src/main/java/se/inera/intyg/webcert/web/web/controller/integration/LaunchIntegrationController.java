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

package se.inera.intyg.webcert.web.web.controller.integration;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.infra.security.authorities.CommonAuthoritiesResolver;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.csintegration.aggregate.GetIssuingUnitIdAggregator;
import se.inera.intyg.webcert.web.web.controller.facade.util.ReactUriFactory;

@Path("/launch")
public class LaunchIntegrationController extends BaseIntegrationController {

    private static final String[] GRANTED_ROLES = new String[]{AuthoritiesConstants.ROLE_ADMIN, AuthoritiesConstants.ROLE_LAKARE,
        AuthoritiesConstants.ROLE_TANDLAKARE, AuthoritiesConstants.ROLE_SJUKSKOTERSKA, AuthoritiesConstants.ROLE_BARNMORSKA};
    @Autowired
    private GetIssuingUnitIdAggregator getIssuingUnitIdAggregator;
    @Autowired
    private ReactUriFactory reactUriFactory;
    @Autowired
    private CommonAuthoritiesResolver commonAuthoritiesResolver;
    private static final Logger LOG = LoggerFactory.getLogger(LaunchIntegrationController.class);

    @GET
    @Path("/certificate/{certificateId}")
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "launch-integration-get-redirect-to-certificate", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public Response redirectToCertificate(@Context UriInfo uriInfo, @PathParam("certificateId") String certificateId,
        @QueryParam("origin") String origin) {
        webCertUserService.getUser().setLaunchFromOrigin(origin);
        LOG.debug("Redirecting to view intyg {}", certificateId);
        return buildRedirectResponse(uriInfo, certificateId, false);
    }

    @GET
    @Path("/certificate/{certificateId}/questions")
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "launch-integration-direct-to-certificate-questions", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public Response directToCertificateQuestions(@Context UriInfo uriInfo, @PathParam("certificateId") String certificateId) {
        LOG.debug("Directing to to view questions on intyg {}", certificateId);
        return buildRedirectResponse(uriInfo, certificateId, true);
    }

    private Response buildRedirectResponse(UriInfo uriInfo, String certificateId, boolean redirectToQuestion) {
        super.validateParameter("certificateId", certificateId);
        super.validateAuthorities();

        final var unitId = getIssuingUnitIdAggregator.get(certificateId);
        validateAndChangeUnit(unitId);

        return redirectToQuestion
            ? getReactRedirectResponseForQuestions(uriInfo, certificateId)
            : getReactRedirectResponseForCertificate(uriInfo, certificateId);
    }

    private Response getReactRedirectResponseForCertificate(UriInfo uriInfo, String certificateId) {
        final var uri = reactUriFactory.uriForCertificate(uriInfo, certificateId);
        return Response.status(Status.TEMPORARY_REDIRECT).location(uri).build();
    }

    private Response getReactRedirectResponseForQuestions(UriInfo uriInfo, String certificateId) {
        final var uri = reactUriFactory.uriForCertificateQuestions(uriInfo, certificateId);
        return Response.status(Status.TEMPORARY_REDIRECT).location(uri).build();
    }

    @Override
    protected String[] getGrantedRoles() {
        return GRANTED_ROLES;
    }

    @Override
    protected UserOriginType getGrantedRequestOrigin() {
        return UserOriginType.NORMAL;
    }

    private void validateAndChangeUnit(String unitId) {
        final var user = webCertUserService.getUser();
        if (!user.changeValdVardenhet(unitId)) {
            throw new WebCertServiceException(
                WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM,
                String.format("User does not have access to unitId '%s'", unitId)
            );
        }

        user.setFeatures(
            commonAuthoritiesResolver.getFeatures(
                Arrays.asList(user.getValdVardenhet().getId(), user.getValdVardgivare().getId())
            )
        );
    }
}
