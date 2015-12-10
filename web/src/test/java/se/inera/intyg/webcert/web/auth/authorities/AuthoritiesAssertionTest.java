package se.inera.intyg.webcert.web.auth.authorities;

import static se.inera.intyg.webcert.web.auth.authorities.AuthoritiesAssertion.assertRequestOrigin;
import static se.inera.intyg.webcert.web.auth.authorities.AuthoritiesAssertion.assertUserRoles;
import static se.inera.intyg.webcert.web.auth.authorities.AuthoritiesConstants.ROLE_ADMIN;
import static se.inera.intyg.webcert.web.auth.authorities.AuthoritiesConstants.ROLE_LAKARE;
import static se.inera.intyg.webcert.web.auth.authorities.AuthoritiesConstants.ROLE_TANDLAKARE;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.intyg.webcert.web.security.WebCertUserOriginType;

/**
 * Created by Magnus Ekstrand on 17/09/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthoritiesAssertionTest {

    @Test
    public void whenGrantedRolesContainUserRole() {
        String[] grantedRoles = new String[] { ROLE_ADMIN, ROLE_LAKARE, ROLE_TANDLAKARE };
        String[] userRoles = new String[] { ROLE_LAKARE };

        assertUserRoles(grantedRoles, userRoles);
    }

    @Test(expected = AuthoritiesException.class)
    public void whenGrantedRolesDoesNotContainUserRole() {
        String[] grantedRoles = new String[] { ROLE_ADMIN, ROLE_LAKARE };
        String[] userRoles = new String[] { ROLE_TANDLAKARE };

        assertUserRoles(grantedRoles, userRoles);
    }

    @Test
    public void whenGrantedRequestOriginContainUserRequestOrigin() {
        String grantedOrigin = WebCertUserOriginType.NORMAL.name();
        String userOrigin = WebCertUserOriginType.NORMAL.name();

        assertRequestOrigin(grantedOrigin, userOrigin);
    }

    @Test(expected = AuthoritiesException.class)
    public void whenGrantedRequestOriginDoesNotContainUserRequestOrigin() {
        String grantedOrigin = WebCertUserOriginType.NORMAL.name();
        String userOrigin = WebCertUserOriginType.DJUPINTEGRATION.name();

        assertRequestOrigin(grantedOrigin, userOrigin);
    }

}
