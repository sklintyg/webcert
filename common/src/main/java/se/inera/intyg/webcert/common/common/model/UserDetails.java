package se.inera.intyg.webcert.common.common.model;

/**
 * Created by Magnus Ekstrand on 27/08/15.
 */

import se.inera.intyg.webcert.common.common.security.authority.UserPrivilege;
import se.inera.intyg.webcert.common.common.security.authority.UserRole;

import java.io.Serializable;
import java.util.Map;

/**
 *
 * Created by Magnus Ekstrand on 2015-08-27.
 *
 */
public interface UserDetails extends Serializable {

    /**
     * Returns the authorities (privileges) granted to the user. Cannot return <code>null</code>.
     *
     * @return the authorities, sorted by natural key (never <code>null</code>)
     */
    Map<String, UserPrivilege> getAuthorities();

    /**
     * Set the authorities (privileges) granted to a user.
     */
    void setAuthorities(Map<String, UserPrivilege> authorities);

    /**
     * Returns the name to authenticate the user. Cannot return <code>null</code>.
     *
     * @return the name (never <code>null</code>)
     */
    String getNamn();

    /**
     * Returns the personal identifier used to authenticate the user.
     *
     * @return the personal identifier
     */
    String getPersonId();

    /**
     * Returns the role granted to the user. Cannot return <code>null</code>.
     *
     * @return the role
     */
    Map<String, UserRole> getRoles();

    /**
     * Set the roles granted to a user.
     */
    void setRoles(Map<String, UserRole> roles);

}
