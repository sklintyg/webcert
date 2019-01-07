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
package se.inera.intyg.webcert.web.auth.oidc.jwt;

import com.google.common.base.Strings;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.auth.WebcertAuthenticationSuccessHandler;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.integration.IntygIntegrationController;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static se.inera.intyg.webcert.web.web.controller.integration.IntygIntegrationController.PARAM_ENHET_ID;

/**
 * Custom Spring Security {@link AuthenticationSuccessHandler} that post-authorization can augment the created
 * session with {@link IntegrationParameters} and redirect the user to the originally requested resource given a certId
 * parameter.
 *
 * Uses the same /visa/intyg/{intygsId}/saved controller as the {@link WebcertAuthenticationSuccessHandler}.
 *
 * @author eriklupander
 */
public class JwtAuthenticationSuccessHandler extends
        SimpleUrlAuthenticationSuccessHandler implements
        AuthenticationSuccessHandler {

    public JwtAuthenticationSuccessHandler() {
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {

        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect");
            return;
        }

        clearAuthenticationAttributes(request);
        String intygsId = request.getParameter("certId");

        WebCertUser webCertUser = (WebCertUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (webCertUser == null) {
            // Should never happen...
            throw new IllegalStateException("No user principal, cannot bind integration params.");
        }

        // Make sure this is a fresh session, e.g. must NOT have any existing params.
        if (webCertUser.getParameters() != null) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM,
                    "This user session is already active and using Webcert. "
                            + "Please use a new user session for each deep integration link.");
        }

        IntegrationParameters integrationParameters = IntegrationParameters.of(
                getStringParam(request, IntygIntegrationController.PARAM_REFERENCE),
                getStringParam(request, IntygIntegrationController.PARAM_RESPONSIBLE_HOSP_NAME),
                getStringParam(request, IntygIntegrationController.PARAM_PATIENT_ALTERNATE_SSN),
                getStringParam(request, IntygIntegrationController.PARAM_PATIENT_FORNAMN),
                getStringParam(request, IntygIntegrationController.PARAM_PATIENT_MELLANNAMN),
                getStringParam(request, IntygIntegrationController.PARAM_PATIENT_EFTERNAMN),
                getStringParam(request, IntygIntegrationController.PARAM_PATIENT_POSTADRESS),
                getStringParam(request, IntygIntegrationController.PARAM_PATIENT_POSTNUMMER),
                getStringParam(request, IntygIntegrationController.PARAM_PATIENT_POSTORT),
                getBooleanParameter(request, IntygIntegrationController.PARAM_COHERENT_JOURNALING, false),
                getBooleanParameter(request, IntygIntegrationController.PARAM_PATIENT_DECEASED, false),
                getBooleanParameter(request, IntygIntegrationController.PARAM_INACTIVE_UNIT, false),
                getBooleanParameter(request, IntygIntegrationController.PARAM_COPY_OK, true));

        webCertUser.setParameters(integrationParameters);

        String redirectUrl = "/visa/intyg/" + intygsId + "/saved";
        if (!Strings.isNullOrEmpty(getStringParam(request, PARAM_ENHET_ID))) {
            redirectUrl = redirectUrl + "?" + PARAM_ENHET_ID + "=" + getStringParam(request, PARAM_ENHET_ID);
        }

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private String getStringParam(HttpServletRequest request, String paramName) {
        if (!Strings.isNullOrEmpty(request.getParameter(paramName))) {
            return request.getParameter(paramName);
        }
        return null;
    }

    private boolean getBooleanParameter(HttpServletRequest request, String paramName, boolean defaultValue) {

        String val = request.getParameter(paramName);
        if (!Strings.isNullOrEmpty(val)) {
            return Boolean.parseBoolean(val);
        }
        return defaultValue;
    }
}
