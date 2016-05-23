/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.intyg.common.security.common.model.UserOriginType;
import se.inera.intyg.webcert.web.web.controller.integration.BaseIntegrationController;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static se.inera.intyg.common.security.common.model.AuthoritiesConstants.ROLE_PRIVATLAKARE;
import static se.inera.intyg.webcert.web.web.controller.util.CertificateTypes.FK7263;

/**
 * Created by eriklupander on 2015-10-08.
 */
@Path("/pp-certificate")
@Api(value = "webcert web user pp-certificate (Fråga/Svar, uthopp privatläkare)", description = "REST API för fråga/svar via uthoppslänk, privatläkare", produces = MediaType.APPLICATION_JSON)
public class PrivatePractitionerFragaSvarIntegrationController extends BaseIntegrationController {

    private static final String PARAM_CERT_TYPE = "certType";
    private static final String PARAM_CERT_ID = "certId";

    private static final Logger LOG = LoggerFactory.getLogger(LegacyIntygIntegrationController.class);

    private String urlFragmentTemplate;

    public void setUrlFragmentTemplate(String urlFragmentTemplate) {
        this.urlFragmentTemplate = urlFragmentTemplate;
    }

    /**
     * Fetches a certificate from IT and then performs a redirect to the view that displays
     * the questions for the cert. Can be used for FK7263 certificates.
     *
     * @param uriInfo
     * @param intygId
     *            The id of the certificate to view.
     * @return
     */
    @GET
    @Path("/{intygId}/questions")
    public Response redirectToIntyg(@Context UriInfo uriInfo, @PathParam("intygId") String intygId) {

        super.validateRedirectToIntyg(intygId);

        String intygType = FK7263.toString();
        LOG.debug("Redirecting to view intyg {} of type {}", intygId, intygType);

        return buildRedirectResponse(uriInfo, intygType, intygId);
    }


    // - - - - - Protected scope - - - - -

    @Override
    protected String[] getGrantedRoles() {
        return new String[] { ROLE_PRIVATLAKARE };
    }

    @Override
    protected UserOriginType getGrantedRequestOrigin() {
        return UserOriginType.NORMAL;
    }


    // - - - - - Default scope - - - - -

    private Response buildRedirectResponse(UriInfo uriInfo, String certificateType, String certificateId) {

        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();

        Map<String, Object> urlParams = new HashMap<String, Object>();
        urlParams.put(PARAM_CERT_TYPE, certificateType);
        urlParams.put(PARAM_CERT_ID, certificateId);

        URI location = uriBuilder.replacePath(getUrlBaseTemplate()).fragment(urlFragmentTemplate).buildFromMap(urlParams);

        return Response.status(Response.Status.TEMPORARY_REDIRECT).location(location).build();
    }



}
