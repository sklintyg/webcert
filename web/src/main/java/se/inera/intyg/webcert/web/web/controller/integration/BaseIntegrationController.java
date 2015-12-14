package se.inera.intyg.webcert.web.web.controller.integration;

import static se.inera.intyg.webcert.web.auth.authorities.AuthoritiesAssertion.assertRequestOrigin;
import static se.inera.intyg.webcert.web.auth.authorities.AuthoritiesAssertion.assertUserRoles;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.webcert.web.auth.authorities.AuthoritiesException;
import se.inera.intyg.webcert.web.auth.authorities.RequestOrigin;
import se.inera.intyg.webcert.web.auth.authorities.Role;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;

/**
 * Base class for deep-integration and uthopp controllers.
 *
 * Created by eriklupander on 2015-10-08.
 */
public abstract class BaseIntegrationController {

    private static final Logger LOG = LoggerFactory.getLogger(BaseIntegrationController.class);

    private WebCertUserService webCertUserService;
    private String urlBaseTemplate;


    // ~ API
    // ========================================================================================

    public boolean validateRedirectToIntyg(String intygId) {
        if (StringUtils.isBlank(intygId)) {
            LOG.error("Path parameter 'intygId' was either whitespace, empty (\"\") or null");
            return false;
        }

        try {
            Map<String, Role> userRoles = webCertUserService.getUser().getRoles();
            assertUserRoles(getGrantedRoles(), toArray(userRoles));

            String origin = webCertUserService.getUser().getOrigin();
            assertRequestOrigin(getGrantedRequestOrigin(), origin);

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

    @Autowired
    public void setWebCertUserService(WebCertUserService webCertUserService) {
        this.webCertUserService = webCertUserService;
    }


    // ~ Protected
    // ========================================================================================

    /**
     * Method should return the granted roles that allows
     * @return
     */
    protected abstract String[] getGrantedRoles();

    protected abstract String getGrantedRequestOrigin();

    protected String getUrlBaseTemplate() {
        return urlBaseTemplate;
    }

    protected WebCertUserService getWebCertUserService() {
        return webCertUserService;
    }


    // ~ Private
    // ========================================================================================

    private String[] toArray(Map<String, Role> roles) {
        List<String> list = roles.entrySet().stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        return list.toArray(new String[list.size()]);
    }

}
