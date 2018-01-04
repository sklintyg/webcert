/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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

import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import se.inera.intyg.infra.security.authorities.CommonAuthoritiesResolver;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.integration.dto.PrepareRedirectToIntyg;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller to enable an external user to access certificates directly from a
 * link in an external patient care system.
 *
 * @author bensam
 */
@Path("/intyg")
@Api(value = "intyg (Djupintegration)", description = "REST API för Djupintegration", produces = MediaType.APPLICATION_JSON)
// CHECKSTYLE:OFF ParameterNumber
public class ViewIntegrationController extends BaseIntegrationController {

    public static final String PARAM_CERT_ID = "certId";
    public static final String PARAM_CERT_TYPE = "certType";
    public static final String PARAM_ENHET_ID = "enhet";

    private static final Logger LOG = LoggerFactory.getLogger(ViewIntegrationController.class);

    private static final UserOriginType GRANTED_ORIGIN = UserOriginType.READONLY;

    private static final String[] GRANTED_ROLES = new String[] {
            AuthoritiesConstants.ROLE_LAKARE, AuthoritiesConstants.ROLE_ADMIN
    };

    private IntegrationService integrationService;

    private String urlIntygFragmentTemplate;

    @Autowired
    private CommonAuthoritiesResolver commonAuthoritiesResolver;

    /**
     * Fetches a certificate from IT or webcert and then performs a redirect to the view that displays
     * the certificate. Can be used for all types of certificates.
     *
     * @param intygId The id of the certificate to view.
     */
    @GET
    @Path("/{intygId}/readonly")
    public Response getRedirectToIntyg(@Context UriInfo uriInfo,
            @PathParam("intygId") String intygId,
            @DefaultValue("") @QueryParam(PARAM_ENHET_ID) String enhetId) {

        validateRequest(intygId, enhetId);
        return handleRedirectToIntyg(uriInfo, intygId, enhetId, getWebCertUser());
    }

    @Autowired
    @Qualifier("viewIntegrationServiceImpl")
    public void setIntegrationService(IntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    public void setUrlIntygFragmentTemplate(String urlIntygFragmentTemplate) {
        this.urlIntygFragmentTemplate = urlIntygFragmentTemplate;
    }

    // protected scope

    @Override
    protected String[] getGrantedRoles() {
        return GRANTED_ROLES;
    }

    @Override
    protected UserOriginType getGrantedRequestOrigin() {
        return GRANTED_ORIGIN;
    }

    // private stuff

    private Response handleRedirectToIntyg(UriInfo uriInfo, String intygId, String enhetId, WebCertUser user) {
        // Call service
        PrepareRedirectToIntyg prepareRedirectToIntyg =
                integrationService.prepareRedirectToIntyg(null, intygId, user);

        // Update user with health care unit
        boolean isUpdated = user.changeValdVardenhet(enhetId);
        if (!isUpdated) {
            LOG.warn("Validation failed for request because user {} is not authorized for enhet {}", user.getHsaId(), enhetId);
            return buildErrorResponse(uriInfo);
        }

        // Update user with current active features
        updateUserWithActiveFeatures(user);

        LOG.debug("Redirecting to view intyg {} of type {}", prepareRedirectToIntyg.getIntygId(), prepareRedirectToIntyg.getIntygTyp());
        return buildRedirectResponse(uriInfo, prepareRedirectToIntyg);
    }

    private Response buildErrorResponse(UriInfo uriInfo) {
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();

        Map<String, Object> urlParams = new HashMap<>();
        urlParams.put("reason", "login.medarbetaruppdrag");

        URI location = uriBuilder.replacePath(getUrlBaseTemplate() + "/error.jsp").buildFromMap(urlParams);
        return Response.status(Response.Status.UNAUTHORIZED).location(location).build();
    }

    private Response buildRedirectResponse(UriInfo uriInfo, PrepareRedirectToIntyg prepareRedirectToIntyg) {
        String intygId = prepareRedirectToIntyg.getIntygId();
        String intygTyp = prepareRedirectToIntyg.getIntygTyp();

        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();

        Map<String, Object> urlParams = new HashMap<>();
        urlParams.put(PARAM_CERT_TYPE, intygTyp);
        urlParams.put(PARAM_CERT_ID, intygId);

        URI location = uriBuilder.replacePath(getUrlBaseTemplate()).fragment(urlIntygFragmentTemplate).buildFromMap(urlParams);

        return Response.status(Response.Status.TEMPORARY_REDIRECT).location(location).build();
    }

    private WebCertUser getWebCertUser() {
        WebCertUser user = getWebCertUserService().getUser();

        // Throw an exception if user already has the integration parameters set
        if (user.getParameters() != null && !user.getParameters().getState().hasUserBeenRedirectedToEnhetsval()) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM,
                    "This user session is already active and using Webcert. Please use a new user session for each deep integration link.");
        }

        return user;
    }

    private void updateUserWithActiveFeatures(WebCertUser webCertUser) {
        webCertUser.setFeatures(commonAuthoritiesResolver
                .getFeatures(Arrays.asList(webCertUser.getValdVardenhet().getId(), webCertUser.getValdVardgivare().getId())));
    }

    private void validateRequest(String intygId, String enhetId) {
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_CERT_ID, intygId);
        params.put(PARAM_ENHET_ID, enhetId);

        super.validateParameters(params);
        super.validateAuthorities();
    }

}
// CHECKSTYLE:ON ParameterNumber
