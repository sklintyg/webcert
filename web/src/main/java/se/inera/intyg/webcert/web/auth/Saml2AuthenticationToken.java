/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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
import org.jspecify.annotations.NullMarked;
import org.springframework.security.saml2.provider.service.authentication.Saml2AssertionAuthentication;

public class Saml2AuthenticationToken extends Saml2AssertionAuthentication {

  private final String name;

  public Saml2AuthenticationToken(Object principal, Saml2AssertionAuthentication authentication) {
    super(
        principal,
        authentication.getCredentials(),
        authentication.getAuthorities(),
        authentication.getRelyingPartyRegistrationId());
    this.name = authentication.getName();
  }

  @Override
  @NullMarked
  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    Saml2AuthenticationToken that = (Saml2AuthenticationToken) o;
    return Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), name);
  }
}
