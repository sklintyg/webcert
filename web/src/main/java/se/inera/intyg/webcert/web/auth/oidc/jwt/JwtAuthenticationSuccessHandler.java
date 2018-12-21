package se.inera.intyg.webcert.web.auth.oidc.jwt;

import com.google.common.base.Strings;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.integration.IntygIntegrationController;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static se.inera.intyg.webcert.web.web.controller.integration.IntygIntegrationController.PARAM_ENHET_ID;

public class JwtAuthenticationSuccessHandler extends
        SimpleUrlAuthenticationSuccessHandler implements
        AuthenticationSuccessHandler {

    public JwtAuthenticationSuccessHandler() {
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

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
            redirectUrl  = redirectUrl + "?" + PARAM_ENHET_ID + "=" + getStringParam(request, PARAM_ENHET_ID);
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
