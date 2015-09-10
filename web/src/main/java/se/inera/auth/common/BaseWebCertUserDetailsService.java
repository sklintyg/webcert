package se.inera.auth.common;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import se.inera.webcert.common.security.authority.SimpleGrantedAuthority;
import se.inera.webcert.persistence.roles.model.Privilege;
import se.inera.webcert.persistence.roles.model.Role;
import se.inera.webcert.persistence.roles.repository.RoleRepository;
import se.inera.webcert.service.feature.WebcertFeatureService;
import se.inera.webcert.service.user.dto.WebCertUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by eriklupander on 2015-06-16.
 */
public abstract class BaseWebCertUserDetailsService {

    protected static final String COMMA = ", ";
    protected static final String SPACE = " ";

    private RoleRepository roleRepository;

    private WebcertFeatureService webcertFeatureService;


    // - - - - - Public scope - - - - -

    public RoleRepository getRoleRepository() {
        return roleRepository;
    }

    @Autowired
    public void setRoleRepository(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Autowired
    public void setWebcertFeatureService(WebcertFeatureService webcertFeatureService) {
        this.webcertFeatureService = webcertFeatureService;
    }

    public final GrantedAuthority getRoleAuthority(final Role role) {
        return getGrantedRole(role);
    }

    public final Collection<? extends GrantedAuthority> getPrivilegeAuthorities(final Role role) {
        return getGrantedPrivileges(getPrivileges(role));
    }


    // - - - - - Protected scope - - - - -

    protected String compileName(String fornamn, String mellanOchEfterNamn) {

        StringBuilder sb = new StringBuilder();

        if (StringUtils.isNotBlank(fornamn)) {
            sb.append(fornamn);
        }

        if (StringUtils.isNotBlank(mellanOchEfterNamn)) {
            if (sb.length() > 0) {
                sb.append(SPACE);
            }
            sb.append(mellanOchEfterNamn);
        }

        return sb.toString();
    }

    protected abstract WebCertUser createUser(String userRole);

    protected void decorateWebCertUserWithAvailableFeatures(WebCertUser webcertUser) {
        Set<String> availableFeatures = webcertFeatureService.getActiveFeatures();
        webcertUser.setAktivaFunktioner(availableFeatures);
    }



    // - - - - - Private scope - - - - -

    private GrantedAuthority getGrantedRole(final Role role) {
        return new SimpleGrantedAuthority(role.getName(), role.getText());
    }

    private List<GrantedAuthority> getGrantedPrivileges(final List<Privilege> privileges) {
        final List<GrantedAuthority> authorities = new ArrayList<>();
        for (final Privilege privilege : privileges) {
            authorities.add(new SimpleGrantedAuthority(privilege.getName(), privilege.getText()));
        }
        return authorities;
    }

    private List<Privilege> getPrivileges(final Role role) {
        List<Privilege> privileges = new ArrayList<>();
        privileges.addAll(role.getPrivileges());
        return privileges;
    }

}
