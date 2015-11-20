package se.inera.intyg.webcert.web.web.controller.integration;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.webcert.web.security.AuthoritiesAssertion;
import se.inera.intyg.webcert.web.security.AuthoritiesException;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;

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

    public boolean validateRedirectToIntyg(String intygId) {
        if (StringUtils.isBlank(intygId)) {
            LOG.error("Path parameter 'intygId' was either whitespace, empty (\"\") or null");
            return false;
        }

        try {
            webCertUserService.assertUserRoles(getGrantedRoles());
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
