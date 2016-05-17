/**
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 * <p>
 * This file is part of rehabstod (https://github.com/sklintyg/rehabstod).
 * <p>
 * rehabstod is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * rehabstod is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.web.auth.authorities;

import org.springframework.util.Assert;
import se.inera.intyg.webcert.web.auth.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.webcert.web.model.UserDetails;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Magnus Ekstrand on 2016-05-13.
 */
public class AuthoritiesHelper {

    private AuthoritiesResolver authoritiesResolver;

    public AuthoritiesHelper(AuthoritiesResolver authoritiesResolver) {
        this.authoritiesResolver = authoritiesResolver;
    }

    /**
     * Method returns all granted intygstyper for a certain user's privilege.
     * If user doesn't have a privilege, an empty set is returned.
     *
     * Note:
     * The configuration mindset of privileges is that if there are no
     * intygstyper attached to a privilege, the privilege is implicitly
     * valid for all intygstyper. However, this method will return an
     * explicit list with granted intygstyper in all cases.
     *
     * @param user the current user
     * @param privilegeName the privilege name
     * @return returns a set of granted intygstyper, an empty set means no granted intygstyper for this privilege
     */
    public Set<String> getIntygstyperForPrivilege(UserDetails user, String privilegeName) {
        Assert.notNull(privilegeName);

        // If user doesn't have a privilege, return an empty set
        AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();
        if (!authoritiesValidator.given(user).privilege(privilegeName).isVerified()) {
            return Collections.emptySet();
        }

        // User is granted privilege access, get the privilege's intygstyper
        Privilege privilege = user.getAuthorities().get(privilegeName);

        // Return intygstyper configured for this privilege
        List<String> intygsTyper = privilege.getIntygstyper();

        // If the privilege doesn't have any intygstyper
        // restrictions, return all known intygstyper
        if (intygsTyper == null || intygsTyper.isEmpty()) {
            return toSet(authoritiesResolver.getIntygstyper());
        }

        // Get privilege's requestOrigins
        List<RequestOrigin> requestOrigins = privilege.getRequestOrigins();

        // If the privilege doesn't have any requestOrigin
        // restrictions, return all the privilege's intygstyper
        if (requestOrigins == null || requestOrigins.isEmpty()) {
            return toSet(intygsTyper);
        }

        // Get user's origin
        String origin = user.getOrigin();

        // If user's origin can be found within one of the privilege's
        // requestOrigins, return intygstyper for the match.
        if (requestOrigins.stream().anyMatch(o -> o.getName().equalsIgnoreCase(origin))) {
            RequestOrigin ro = requestOrigins.stream().filter(o -> o.getName().equalsIgnoreCase(origin)).findFirst().get();
            return toSet(ro.getIntygstyper());
        }

        // User's origin wasn't within thre privilege's
        // requestOrigins, return empty set of intygstyper.
        return toSet(new ArrayList<>());
    }

    private Set<String> toSet(List<String> intygsTyper) {
        return intygsTyper.stream().collect(Collectors.toSet());
    }

}
