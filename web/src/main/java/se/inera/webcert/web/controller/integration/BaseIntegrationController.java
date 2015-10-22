package se.inera.webcert.web.controller.integration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.webcert.common.security.authority.UserRole;
import se.inera.webcert.security.AuthoritiesAssertion;
import se.inera.webcert.security.AuthoritiesException;
import se.inera.webcert.service.user.WebCertUserService;
import se.inera.webcert.service.user.dto.WebCertUser;

/**
 * Base class for deep-integration and uthopp controllers.
 *
 * Created by eriklupander on 2015-10-08.
 */
public abstract class BaseIntegrationController extends AuthoritiesAssertion {

    private static final Logger LOG = LoggerFactory.getLogger(BaseIntegrationController.class);

    private WebCertUserService webCertUserService;
    private String urlBaseTemplate;

    protected abstract String[] getGrantedRoles();
    protected abstract void updateUserRoles(WebCertUser user);

    /*
     * Gör inget om användare redan har rollen:
     * - ROLE_LAKARE_DJUPINTEGRERAD eller
     * - ROLE_VARDADMINISTRATOR_DJUPINTEGRERAD
     *
     * Om användare har rollen:
     * - ROLE_LAKARE eller
     * - ROLE_VARDADMINISTRATOR
     *
     * så ändra/nedgradera rollen till
     * - ROLE_LAKARE_DJUPINTEGRERAD eller
     * - ROLE_VARDADMINISTRATOR_DJUPINTEGRERAD
     *
     * För alla andra roller, eller ingen roll,
     * släng ett exception.
     */
    protected void assertUserRole(WebCertUser user) {

        Map<String, UserRole> userRoles = user.getRoles();

        List<String> gr = Arrays.asList(UserRole.ROLE_LAKARE.name(), UserRole.ROLE_VARDADMINISTRATOR.name());
        for (String role : userRoles.keySet()) {
            if (gr.contains(role)) {
                updateUserRoles(user);
                return;
            }
        }

        // Assert user has a valid role for this request
        webCertUserService.assertUserRoles(getGrantedRoles());
    }

    protected void writeUserRoles(String userRole) {

        LOG.debug("Updating user role to be {}", userRole);
        webCertUserService.updateUserRoles(new String[] { userRole });
    }

    public boolean validateRedirectToIntyg(String intygId) {
        if (StringUtils.isBlank(intygId)) {
            LOG.error("Path parameter 'intygId' was either whitespace, empty (\"\") or null");
            return false;
        }

        WebCertUser user = webCertUserService.getUser();

        try {
            // Ensure user has valid role
            assertUserRole(user);
        } catch (AuthoritiesException e) {
            LOG.error(e.getMessage());
            return false;
        }
        return true;
    }



    @Autowired
    public void setUrlBaseTemplate(String urlBaseTemplate) {
        this.urlBaseTemplate = urlBaseTemplate;
    }

    protected String getUrlBaseTemplate() {
        return urlBaseTemplate;
    }

    @Autowired
    public void setWebCertUserService(WebCertUserService webCertUserService) {
        this.webCertUserService = webCertUserService;
    }

    protected WebCertUserService getWebCertUserService() {
        return webCertUserService;
    }
}
