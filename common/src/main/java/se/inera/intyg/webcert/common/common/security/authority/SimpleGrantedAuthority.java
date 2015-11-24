package se.inera.intyg.webcert.common.common.security.authority;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.util.Assert;

/**
 * Basic concrete implementation of a {@link org.springframework.security.core.GrantedAuthority}.
 *
 * <p>
 * Stores a {@code String} representation of an authority granted to the
 * {@link org.springframework.security.core.Authentication Authentication} object.
 *
 * @author Magnus kstrand
 */
public final class SimpleGrantedAuthority implements GrantedAuthority {

    private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

    private final String authority;
    private final String description;

    public SimpleGrantedAuthority(String authority, String description) {
        Assert.hasText(authority, "A granted authority textual representation is required");
        this.authority = authority;
        this.description = description;
    }

    public String getAuthority() {
        return authority;
    }

    public String getDescription() {
        return description;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof SimpleGrantedAuthority) {
            return authority.equals(((SimpleGrantedAuthority) obj).authority);
        }

        return false;
    }

    public int hashCode() {
        return this.authority.hashCode();
    }

    public String toString() {
        return this.authority;
    }
}
