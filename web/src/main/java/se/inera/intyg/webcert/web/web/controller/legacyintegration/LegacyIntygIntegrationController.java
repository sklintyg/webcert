/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

import static se.inera.intyg.common.support.common.enumerations.CertificateTypes.FK7263;
import static se.inera.intyg.webcert.web.auth.authorities.AuthoritiesConstants.ROLE_ADMIN;
import static se.inera.intyg.webcert.web.auth.authorities.AuthoritiesConstants.ROLE_LAKARE;
import static se.inera.intyg.webcert.web.auth.authorities.AuthoritiesConstants.ROLE_TANDLAKARE;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.inera.intyg.webcert.web.security.WebCertUserOriginType;
import se.inera.intyg.webcert.web.web.controller.integration.BaseIntegrationController;
import io.swagger.annotations.Api;

/**
 * Controller to enable an external user to access certificates directly from a
 * link in an external patient care system.
 *
 * @author nikpet
 */
@Path("/certificate")
@Api(value = "webcert web user certificate (Fråga/Svar uthopp)", description = "REST API för fråga/svar via uthoppslänk, landstingspersonal", produces = MediaType.APPLICATION_JSON)
public class LegacyIntygIntegrationController extends BaseIntegrationController {

    private static final Logger LOG = LoggerFactory.getLogger(LegacyIntygIntegrationController.class);

    private static final String PARAM_CERT_TYPE = "certType";
    private static final String PARAM_CERT_ID = "certId";

    private static final String[] GRANTED_ROLES = new String[] { ROLE_ADMIN, ROLE_LAKARE, ROLE_TANDLAKARE };
    private static final WebCertUserOriginType GRANTED_ORIGIN = WebCertUserOriginType.UTHOPP;

    private String urlFragmentTemplate;

    @Override
    protected String[] getGrantedRoles() {
        return GRANTED_ROLES;
    }

    @Override
    protected WebCertUserOriginType getGrantedRequestOrigin() {
        return GRANTED_ORIGIN;
    }

    /**
     * Fetches a certificate from IT and then performs a redirect to the view that displays
     * the certificate. Can be used for all types of certificates.
     *
     * @param intygId
     *            The id of the certificate to view.
     */
    @GET
    @Path("/{intygId}/questions")
    public Response redirectToIntyg(@Context UriInfo uriInfo, @PathParam("intygId") String intygId) {

        super.validateRedirectToIntyg(intygId);

        String intygType = FK7263.toString();
        LOG.debug("Redirecting to view intyg {} of type {}", intygId, intygType);

        return buildRedirectResponse(uriInfo, intygType, intygId);
    }

    public void setUrlFragmentTemplate(String urlFragmentTemplate) {
        this.urlFragmentTemplate = urlFragmentTemplate;
    }

    // - - - - - Private scope - - - - -

    private Response buildRedirectResponse(UriInfo uriInfo, String certificateType, String certificateId) {

        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();

        Map<String, Object> urlParams = new HashMap<>();
        urlParams.put(PARAM_CERT_TYPE, certificateType);
        urlParams.put(PARAM_CERT_ID, certificateId);

        URI location = uriBuilder.replacePath(getUrlBaseTemplate()).fragment(urlFragmentTemplate).buildFromMap(urlParams);

        return Response.status(Status.TEMPORARY_REDIRECT).location(location).build();
    }

}
