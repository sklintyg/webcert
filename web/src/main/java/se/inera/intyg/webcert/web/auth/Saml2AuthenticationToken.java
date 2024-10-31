/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

import java.util.Objects;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;

public class Saml2AuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;
    private final Saml2Authentication saml2Authentication;
    private final String name;

    public Saml2AuthenticationToken(Object principal, Saml2Authentication authentication) {
        super(authentication.getAuthorities());
        this.principal = principal;
        this.saml2Authentication = authentication;
        this.name = authentication.getName();
    }

    @Override
    public Object getCredentials() {
        return saml2Authentication.getCredentials();
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public String getName() {
        return name;
    }

    public Saml2Authentication getSaml2Authentication() {
        return saml2Authentication;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        Saml2AuthenticationToken token = (Saml2AuthenticationToken) o;
        return Objects.equals(saml2Authentication, token.saml2Authentication) && Objects.equals(name, token.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), principal, saml2Authentication, name);
    }
}
