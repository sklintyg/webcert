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

package se.inera.intyg.webcert.web.web.controller.integration;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.webcert.web.auth.authorities.AuthoritiesException;
import se.inera.intyg.webcert.web.auth.authorities.Role;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static se.inera.intyg.webcert.web.auth.authorities.AuthoritiesAssertion.assertRequestOrigin;
import static se.inera.intyg.webcert.web.auth.authorities.AuthoritiesAssertion.assertUserRoles;

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
     * Method should return the granted roles that allows.
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
