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

package se.inera.intyg.webcert.web.web.controller.integration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.infra.security.authorities.CommonAuthoritiesResolver;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.web.controller.facade.util.ReactPilotUtil;
import se.inera.intyg.webcert.web.web.controller.facade.util.ReactUriFactory;

@Path("/launch")
public class LaunchIntegrationController extends BaseIntegrationController {

    private static final String[] GRANTED_ROLES = new String[]{AuthoritiesConstants.ROLE_ADMIN, AuthoritiesConstants.ROLE_LAKARE,
        AuthoritiesConstants.ROLE_TANDLAKARE};
    @Autowired
    private IntygService intygService;
    @Autowired
    private ReactPilotUtil reactPilotUtil;
    @Autowired
    private ReactUriFactory reactUriFactory;
    @Autowired
    private CommonAuthoritiesResolver commonAuthoritiesResolver;
    private static final Logger LOG = LoggerFactory.getLogger(LaunchIntegrationController.class);
    private static final String PARAM_CERT_TYPE = "certType";
    private static final String PARAM_CERT_TYPE_VERSION = "certTypeVersion";
    private static final String PARAM_CERT_ID = "certId";
    private String urlFragmentTemplate;

    @GET
    @Path("/certificate/{certificateId}")
    @PrometheusTimeMethod
    public Response redirectToCertificate(@Context UriInfo uriInfo, @PathParam("certificateId") String certificateId,
        @QueryParam("origin") String origin) {
        super.validateParameter("certificateId", certificateId);
        super.validateAuthorities();

        final var intygTypeInfo = intygService.getIntygTypeInfo(certificateId);
        final var certificateType = intygTypeInfo.getIntygType();
        final var certificateTypeVersion = intygTypeInfo.getIntygTypeVersion();
        validateAndChangeUnit(certificateId, certificateType);

        webCertUserService.getUser().setLaunchFromOrigin(origin);

        LOG.debug("Redirecting to view intyg {} of type {}", certificateId, certificateType);
        return buildRedirectResponse(uriInfo, certificateType, certificateTypeVersion, certificateId);
    }

    public void setUrlFragmentTemplate(String urlFragmentTemplate) {
        this.urlFragmentTemplate = urlFragmentTemplate;
    }

    private Response buildRedirectResponse(UriInfo uriInfo, String certificateType, String certificateTypeVersion, String certificateId) {
        if (reactPilotUtil.useReactClientFristaende(webCertUserService.getUser(), certificateType)) {
            return getReactRedirectResponse(uriInfo, certificateId);
        }
        return getAngularRedirectResponse(uriInfo, certificateType, certificateTypeVersion, certificateId);
    }

    private Response getAngularRedirectResponse(UriInfo uriInfo, String certificateType, String certificateTypeVersion,
        String certificateId) {
        final var urlParams = getUrlParams(certificateType, certificateTypeVersion, certificateId);
        final var location = uriInfo.getBaseUriBuilder()
            .replacePath(getUrlBaseTemplate())
            .fragment(urlFragmentTemplate)
            .buildFromMap(urlParams);
        return Response.status(Status.TEMPORARY_REDIRECT).location(location).build();
    }

    private static Map<String, Object> getUrlParams(String certificateType, String certificateTypeVersion, String certificateId) {
        Map<String, Object> urlParams = new HashMap<>();
        urlParams.put(PARAM_CERT_TYPE, certificateType);
        urlParams.put(PARAM_CERT_TYPE_VERSION, certificateTypeVersion);
        urlParams.put(PARAM_CERT_ID, certificateId);
        return urlParams;
    }

    private Response getReactRedirectResponse(UriInfo uriInfo, String certificateId) {
        final var uri = reactUriFactory.uriForCertificate(uriInfo, certificateId);
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

    private void validateAndChangeUnit(String certificateId, String certificateType) {
        final var user = webCertUserService.getUser();
        final var unitId = intygService.getIssuingVardenhetHsaId(certificateId, certificateType);
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
