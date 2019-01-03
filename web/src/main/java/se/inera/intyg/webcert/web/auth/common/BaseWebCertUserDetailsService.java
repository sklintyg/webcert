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
package se.inera.intyg.webcert.web.auth.common;

import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import se.inera.intyg.infra.security.authorities.CommonAuthoritiesResolver;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;

/**
 * Base UserDetailsService for both Siths and E-leg based authentication.
 * <p>
 * Created by eriklupander on 2015-06-16.
 */
public abstract class BaseWebCertUserDetailsService {

    protected static final String COMMA = ", ";
    protected static final String SPACE = " ";

    private CommonAuthoritiesResolver authoritiesResolver;

    public CommonAuthoritiesResolver getAuthoritiesResolver() {
        return authoritiesResolver;
    }

    @Autowired
    public void setAuthoritiesResolver(CommonAuthoritiesResolver authoritiesResolver) {
        this.authoritiesResolver = authoritiesResolver;
    }

    protected String compileName(String fornamn, String mellanOchEfterNamn) {

        StringBuilder sb = new StringBuilder();

        sb.append(Strings.nullToEmpty(fornamn).trim());

        if (sb.length() > 0) {
            sb.append(SPACE);
        }
        sb.append(Strings.nullToEmpty(mellanOchEfterNamn).trim());

        return sb.toString();
    }

    protected void decorateWebCertUserWithAvailableFeatures(WebCertUser webcertUser) {
        if (webcertUser.getValdVardenhet() != null && webcertUser.getValdVardgivare() != null) {
            webcertUser.setFeatures(authoritiesResolver
                    .getFeatures(Arrays.asList(webcertUser.getValdVardenhet().getId(), webcertUser.getValdVardgivare().getId())));
        } else {
            webcertUser.setFeatures(authoritiesResolver.getFeatures(Collections.emptyList()));
        }
    }

    protected HttpServletRequest getCurrentRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }

}
