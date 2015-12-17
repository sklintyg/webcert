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

package se.inera.intyg.webcert.web.security;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.intyg.webcert.common.common.security.authority.UserRole;

/**
 * Created by Magnus Ekstrand on 17/09/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthoritiesAssertionTest {

    @InjectMocks
    private AuthoritiesAssertion authoritiesAssertion = new AuthoritiesAssertion();


    @Test
    public void whenGrantedRolesContainUserRole() {
        String[] grantedRoles = new String[] {UserRole.ROLE_LAKARE_DJUPINTEGRERAD.name(), UserRole.ROLE_VARDADMINISTRATOR_DJUPINTEGRERAD.name() };
        String[] userRoles = new String[] { UserRole.ROLE_LAKARE_DJUPINTEGRERAD.name() };

        authoritiesAssertion.assertUserRoles(grantedRoles, userRoles);
    }

    @Test(expected = AuthoritiesException.class)
    public void whenGrantedRolesDoesNotContainUserRole() {
        String[] grantedRoles = new String[] {UserRole.ROLE_LAKARE_UTHOPP.name(), UserRole.ROLE_VARDADMINISTRATOR_UTHOPP.name() };
        String[] userRoles = new String[] { UserRole.ROLE_LAKARE_DJUPINTEGRERAD.name() };

        authoritiesAssertion.assertUserRoles(grantedRoles, userRoles);
    }

}
