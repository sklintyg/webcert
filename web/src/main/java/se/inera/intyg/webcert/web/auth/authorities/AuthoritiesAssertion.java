package se.inera.intyg.webcert.web.auth.authorities;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Magnus Ekstrand on 16/09/15.
 */
public final class AuthoritiesAssertion {

    private AuthoritiesAssertion() {
    }

    /**
     * Assert that user has a role that matches a granted role.
     * If no role match then an IllegalAccessError will be thrown.
     *
     * @param grantedRoles the granted roles
     * @param userRoles user's current roles
     * @throws AuthoritiesException
     */
    public static void assertUserRoles(String[] grantedRoles, String[] userRoles) throws AuthoritiesException {
        List<String> gr = Arrays.asList(grantedRoles);

        for (String role : userRoles) {
            if (gr.contains(role)) {
                return;
            }
        }

        throw new AuthoritiesException(
            String.format("User does not have a valid role for current request. User's role must be one of [%s] but was [%s]",
                    StringUtils.join(grantedRoles, ","), StringUtils.join(userRoles, ",")));
    }

    /**
     * Assert that user has a privilege that matches a granted privilege.
     * If no privilege match then an IllegalAccessError will be thrown.
     *
     * @param grantedPrivileges the granted privileges
     * @param userPrivileges user's current privileges
     * @throws AuthoritiesException
     */
    public static void assertUserPrivileges(String[] grantedPrivileges, String[] userPrivileges) throws AuthoritiesException {
        List<String> gp = Arrays.asList(grantedPrivileges);

        for (String privilege : userPrivileges) {
            if (gp.contains(privilege)) {
                return;
            }
        }

        throw new AuthoritiesException(
                String.format("User does not have a valid role for current request. User's role must be one of [%s] but was [%s]",
                        StringUtils.join(grantedPrivileges, ","), StringUtils.join(grantedPrivileges, ",")));
    }

    /**
     * Assert that user has a request origin that matches the granted request origin.
     * If no request origin match then an IllegalAccessError will be thrown.
     *
     * @param grantedRequestOrigin the granted request origin
     * @param userRequestOrigin user's current request origin
     * @throws AuthoritiesException
     */
    public static void assertRequestOrigin(String grantedRequestOrigin, String userRequestOrigin) throws AuthoritiesException {
        if (grantedRequestOrigin.equals(userRequestOrigin)) {
            return;
        }

        throw new AuthoritiesException(
                String.format("User does not have a valid request origin for current task. User's request origin must be [%s] but was [%s]",
                        grantedRequestOrigin, userRequestOrigin));
    }

}
