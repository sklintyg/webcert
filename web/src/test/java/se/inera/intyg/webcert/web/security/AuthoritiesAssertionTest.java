package se.inera.intyg.webcert.web.security;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.webcert.common.security.authority.UserRole;

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
