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

package se.inera.intyg.webcert.web.web.controller.integration;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.webcert.web.auth.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.webcert.web.security.WebCertUserOriginType;
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
    protected AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();

    // ~ API
    // ========================================================================================

    public void validateRedirectToIntyg(String intygId) {

        // Input validation
        if (StringUtils.isBlank(intygId)) {
            throw new IllegalArgumentException("Path parameter 'intygId' was either whitespace, empty (\"\") or null");
        }

        // Do Auth validation, given the subclass role/origin constraints
        authoritiesValidator.given(webCertUserService.getUser())
                .roles(getGrantedRoles())
                .origins(getGrantedRequestOrigin())
                .orThrow();

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
     * Method should return the granted roles that allows.
     *
     * @return
     */
    protected abstract String[] getGrantedRoles();

    protected abstract WebCertUserOriginType getGrantedRequestOrigin();

    protected String getUrlBaseTemplate() {
        return urlBaseTemplate;
    }

    protected WebCertUserService getWebCertUserService() {
        return webCertUserService;
    }

}
