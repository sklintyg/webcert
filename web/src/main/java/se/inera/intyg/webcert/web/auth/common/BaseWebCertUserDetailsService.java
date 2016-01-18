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

package se.inera.intyg.webcert.web.auth.common;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import se.inera.intyg.webcert.web.auth.authorities.AuthoritiesResolver;
import se.inera.intyg.webcert.web.service.feature.WebcertFeatureService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

/**
 * Base UserDetailsService for both Siths and E-leg based authentication.
 *
 * Created by eriklupander on 2015-06-16.
 */
public abstract class BaseWebCertUserDetailsService {

    protected static final String COMMA = ", ";
    protected static final String SPACE = " ";

    private AuthoritiesResolver authoritiesResolver;

    private WebcertFeatureService webcertFeatureService;


    // - - - - - Public scope - - - - -

    public AuthoritiesResolver getAuthoritiesResolver() {
        return authoritiesResolver;
    }

    @Autowired
    public void setAuthoritiesResolver(AuthoritiesResolver authoritiesResolver) {
        this.authoritiesResolver = authoritiesResolver;
    }

    @Autowired
    public void setWebcertFeatureService(WebcertFeatureService webcertFeatureService) {
        this.webcertFeatureService = webcertFeatureService;
    }


    // - - - - - Protected scope - - - - -

    protected String compileName(String fornamn, String mellanOchEfterNamn) {

        StringBuilder sb = new StringBuilder();

        if (StringUtils.isNotBlank(fornamn)) {
            sb.append(fornamn);
        }

        if (StringUtils.isNotBlank(mellanOchEfterNamn)) {
            if (sb.length() > 0) {
                sb.append(SPACE);
            }
            sb.append(mellanOchEfterNamn);
        }

        return sb.toString();
    }

    protected void decorateWebCertUserWithAvailableFeatures(WebCertUser webcertUser) {
        Set<String> availableFeatures = webcertFeatureService.getActiveFeatures();
        webcertUser.setFeatures(availableFeatures);
    }

    protected HttpServletRequest getCurrentRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }

}
