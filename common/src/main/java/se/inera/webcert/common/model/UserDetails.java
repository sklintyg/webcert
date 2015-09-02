package se.inera.webcert.common.model;

/**
 * Created by Magnus Ekstrand on 27/08/15.
 */

import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;
import java.util.Collection;

/**
 *
 * Created by Magnus Ekstrand on 2015-08-27.
 *
 */
public interface UserDetails extends Serializable {

    /**
     * Returns the personal identifier used to authenticate the user.
     *
     * @return the personal identifier
     */
    String getPersonId();

    /**
     * Returns the name to authenticate the user. Cannot return <code>null</code>.
     *
     * @return the name (never <code>null</code>)
     */
    String getNamn();

    /**
     * Returns the authorities granted to the user. Cannot return <code>null</code>.
     *
     * @return the authorities, sorted by natural key (never <code>null</code>)
     */
    Collection<? extends GrantedAuthority> getAuthorities();

}
