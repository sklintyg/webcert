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
package se.inera.intyg.webcert.web.auth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.util.StringUtils;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.integration.IntygIntegrationController;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

import static se.inera.intyg.webcert.web.security.WebCertUserOrigin.REGEXP_REQUESTURI_DJUPINTEGRATION;
import static se.inera.intyg.webcert.web.web.controller.integration.IntygIntegrationController.PARAM_ENHET_ID;

public class WebcertAuthenticationSuccessHandler extends
        SimpleUrlAuthenticationSuccessHandler {
    protected final Log logger = LogFactory.getLog(this.getClass());

    private RequestCache requestCache = new HttpSessionRequestCache();

    private Pattern djupintegrationPattern = Pattern.compile(REGEXP_REQUESTURI_DJUPINTEGRATION);

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response, Authentication authentication)
            throws ServletException, IOException {
        SavedRequest savedRequest = requestCache.getRequest(request, response);

        if (savedRequest == null) {
            super.onAuthenticationSuccess(request, response, authentication);

            return;
        }
        String targetUrlParameter = getTargetUrlParameter();
        if (isAlwaysUseDefaultTargetUrl()
                || (targetUrlParameter != null && StringUtils.hasText(request
                        .getParameter(targetUrlParameter)))) {
            requestCache.removeRequest(request, response);
            super.onAuthenticationSuccess(request, response, authentication);

            return;
        }

        clearAuthenticationAttributes(request);

        // Use the DefaultSavedRequest URL
        String targetUrl = savedRequest.getRedirectUrl();
        String targetUri = ((DefaultSavedRequest) savedRequest).getRequestURI();

        // If original req was POST for djupintegration, we need to extract parameters
        if (savedRequest.getMethod().equalsIgnoreCase(HttpMethod.POST.name()) && djupintegrationPattern.matcher(targetUri).matches()) {
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
                    fromSavedReq(savedRequest, IntygIntegrationController.PARAM_REFERENCE),
                    fromSavedReq(savedRequest, IntygIntegrationController.PARAM_RESPONSIBLE_HOSP_NAME),
                    fromSavedReq(savedRequest, IntygIntegrationController.PARAM_PATIENT_ALTERNATE_SSN),
                    fromSavedReq(savedRequest, IntygIntegrationController.PARAM_PATIENT_FORNAMN),
                    fromSavedReq(savedRequest, IntygIntegrationController.PARAM_PATIENT_MELLANNAMN),
                    fromSavedReq(savedRequest, IntygIntegrationController.PARAM_PATIENT_EFTERNAMN),
                    fromSavedReq(savedRequest, IntygIntegrationController.PARAM_PATIENT_POSTADRESS),
                    fromSavedReq(savedRequest, IntygIntegrationController.PARAM_PATIENT_POSTNUMMER),
                    fromSavedReq(savedRequest, IntygIntegrationController.PARAM_PATIENT_POSTORT),
                    fromSavedReqBoolean(savedRequest, IntygIntegrationController.PARAM_COHERENT_JOURNALING),
                    fromSavedReqBoolean(savedRequest, IntygIntegrationController.PARAM_PATIENT_DECEASED),
                    fromSavedReqBoolean(savedRequest, IntygIntegrationController.PARAM_INACTIVE_UNIT),
                    fromSavedReqBoolean(savedRequest, IntygIntegrationController.PARAM_COPY_OK, true));

            webCertUser.setParameters(integrationParameters);

            // Modify so the redirect is to /visa/intyg/{intygsId}/saved which ignores reading the params above from
            // GET params or POST body.
            targetUrl = targetUrl + "/saved";

            // Add a query-param with the enhet=... in case it's present as a form-param. enhet is NOT handled as an IntegrationParameter.
            if (savedRequest.getParameterMap().containsKey(PARAM_ENHET_ID)) {
                targetUrl = targetUrl + "?" + PARAM_ENHET_ID + "=" + fromSavedReq(savedRequest, PARAM_ENHET_ID);
            }
        }

        logger.debug("Redirecting to DefaultSavedRequest Url: " + targetUrl);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String fromSavedReq(SavedRequest savedRequest, String paramName) {
        String[] val = savedRequest.getParameterMap().get(paramName);
        if (val != null && val.length > 0) {
            return val[0];
        }
        return null;
    }

    private boolean fromSavedReqBoolean(SavedRequest savedRequest, String paramName) {
        return fromSavedReqBoolean(savedRequest, paramName, false);
    }

    private boolean fromSavedReqBoolean(SavedRequest savedRequest, String paramName, boolean defaultValue) {
        String[] val = savedRequest.getParameterMap().get(paramName);
        if (val != null && val.length > 0) {
            return Boolean.parseBoolean(val[0]);
        }
        return defaultValue;
    }

    public void setRequestCache(RequestCache requestCache) {
        this.requestCache = requestCache;
    }
}
