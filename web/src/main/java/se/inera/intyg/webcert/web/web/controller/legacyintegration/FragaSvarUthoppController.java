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
package se.inera.intyg.webcert.web.web.controller.legacyintegration;

import com.google.common.base.Strings;
import io.swagger.annotations.Api;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
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
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygTypeInfo;
import se.inera.intyg.webcert.web.web.controller.facade.util.ReactPilotUtil;
import se.inera.intyg.webcert.web.web.controller.facade.util.ReactUriFactory;
import se.inera.intyg.webcert.web.web.controller.integration.BaseIntegrationController;

/**
 * Controller to enable an external user to access certificates directly from a
 * link in an external patient care system.
 *
 * @author nikpet
 */
// CHECKSTYLE:OFF LineLength
@Path("/certificate")
@Api(value = "webcert web user certificate (Fråga/Svar uthopp)", description = "REST API för fråga/svar via uthoppslänk, landstingspersonal", produces = MediaType.APPLICATION_JSON)
public class FragaSvarUthoppController extends BaseIntegrationController {
// CHECKSTYLE:ON LineLength

    private static final Logger LOG = LoggerFactory.getLogger(FragaSvarUthoppController.class);

    private static final String PARAM_CERT_TYPE = "certType";
    private static final String PARAM_CERT_TYPE_VERSION = "certTypeVersion";
    private static final String PARAM_CERT_ID = "certId";
    private static final String PARAM_ORIGIN = "origin";
    private static final String FROM_RS = "rs";
    private static final String NORMAL = "normal";

    private static final String[] GRANTED_ROLES = new String[]{AuthoritiesConstants.ROLE_ADMIN, AuthoritiesConstants.ROLE_LAKARE,
        AuthoritiesConstants.ROLE_TANDLAKARE};
    private static final UserOriginType GRANTED_ORIGIN = UserOriginType.UTHOPP;

    private String urlFragmentTemplate;
    private String urlFragmentTemplateWithOrigin;

    @Autowired
    private IntygService intygService;

    @Autowired
    private ReactUriFactory reactUriFactory;

    @Autowired
    private ReactPilotUtil reactPilotUtil;

    @Autowired
    private CommonAuthoritiesResolver commonAuthoritiesResolver;

    // api

    /**
     * Fetches a certificate from IT and then performs a redirect to the view that displays
     * the certificate. Can be used for all types of certificates.
     *
     * @param intygId The id of the certificate to view.
     */
    @GET
    @Path("/{type}/{intygId}/questions")
    @PrometheusTimeMethod
    public Response redirectToIntyg(@Context UriInfo uriInfo,
        @PathParam("type") String type,
        @PathParam("intygId") String intygId,
        @QueryParam("enhet") String enhetHsaId) {

        super.validateParameter("type", type);
        super.validateParameter("intygId", intygId);
        super.validateAuthorities();
        this.validateAndChangeEnhet(intygId, type, enhetHsaId);

        LOG.debug("Redirecting to view intyg {} of type {}", intygId, type);
        final IntygTypeInfo intygTypeInfo = intygService.getIntygTypeInfo(intygId);
        return buildRedirectResponse(uriInfo, type, intygTypeInfo.getIntygTypeVersion(), intygId, false);
    }

    @GET
    @Path("/{intygId}/questions")
    @PrometheusTimeMethod
    public Response redirectToIntyg(@Context UriInfo uriInfo,
        @PathParam("intygId") String intygId,
        @QueryParam("enhet") String enhetHsaId,
        @QueryParam("fromRs") Boolean fromRs) {

        super.validateParameter("intygId", intygId);
        super.validateAuthorities();

        final var intygTypeInfo = intygService.getIntygTypeInfo(intygId);
        final var intygType = intygTypeInfo.getIntygType();
        final var intygTypeVersion = intygTypeInfo.getIntygTypeVersion();
        this.validateAndChangeEnhet(intygId, intygType, enhetHsaId);

        LOG.debug("Redirecting to view intyg {} of type {}", intygId, intygType);
        return buildRedirectResponse(uriInfo, intygType, intygTypeVersion, intygId, fromRs);
    }

    public void setUrlFragmentTemplate(String urlFragmentTemplate) {
        this.urlFragmentTemplate = urlFragmentTemplate;
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

    /**
     * Makes sure we change (if possible) the current vardEnhet to the one either specified in the URL or to the one
     * the intyg was issued on.
     */
    private void validateAndChangeEnhet(String intygsId, String intygsTyp, String enhetHsaId) {
        WebCertUser user = webCertUserService.getUser();
        if (user == null) {
            LOG.error("No user in session, cannot continue");
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM,
                "No user session, cannot view questions for intyg " + intygsId);
        }

        if (!Strings.nullToEmpty(enhetHsaId).trim().isEmpty()) {
            // Link contained not empty ?enhet= query param, try to set on user!
            if (!user.changeValdVardenhet(enhetHsaId)) {
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM,
                    "User does not have access to enhet " + enhetHsaId);
            }
        } else {
            // No enhet on link (legacy fallback for pre WC 5.0 links)
            String enhet = intygService.getIssuingVardenhetHsaId(intygsId, intygsTyp);
            if (!user.changeValdVardenhet(enhet)) {
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM,
                    "User does not have access to enhet " + enhetHsaId);
            }
        }

        user.setFeatures(
            commonAuthoritiesResolver.getFeatures(
                Arrays.asList(user.getValdVardenhet().getId(), user.getValdVardgivare().getId())
            )
        );
    }

    private Response buildRedirectResponse(UriInfo uriInfo, String certificateType, String intygTypeVersion, String certificateId,
        Boolean fromRs) {
        if (reactPilotUtil.useReactClientFristaende(webCertUserService.getUser(), certificateType)) {
            return getReactRedirectResponse(uriInfo, certificateId);
        }

        return getAngularRedirectResponse(uriInfo, certificateType, intygTypeVersion, certificateId, fromRs);
    }

    private Response getReactRedirectResponse(UriInfo uriInfo, String intygId) {
        final var uri = reactUriFactory.uriForCertificate(uriInfo, intygId);
        return Response.status(Status.TEMPORARY_REDIRECT).location(uri).build();
    }

    private Response getAngularRedirectResponse(UriInfo uriInfo, String certificateType, String intygTypeVersion, String certificateId,
        Boolean fromRs) {
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();

        Map<String, Object> urlParams = new HashMap<>();
        urlParams.put(PARAM_CERT_TYPE, certificateType);
        urlParams.put(PARAM_CERT_TYPE_VERSION, intygTypeVersion);
        urlParams.put(PARAM_CERT_ID, certificateId);
        urlParams.put(PARAM_ORIGIN, fromRs != null && fromRs ? FROM_RS : NORMAL);

        URI location = uriBuilder.replacePath(getUrlBaseTemplate()).fragment(urlFragmentTemplateWithOrigin).buildFromMap(urlParams);

        return Response.status(Status.TEMPORARY_REDIRECT).location(location).build();
    }

    public void setUrlFragmentTemplateWithOrigin(String urlFragmentTemplateWithOrigin) {
        this.urlFragmentTemplateWithOrigin = urlFragmentTemplateWithOrigin;
    }
}
