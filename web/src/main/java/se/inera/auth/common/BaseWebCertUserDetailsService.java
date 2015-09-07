package se.inera.auth.common;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.saml.SAMLCredential;
import se.inera.auth.SakerhetstjanstAssertion;
import se.inera.webcert.common.model.UserRoles;
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

    public final Collection<? extends GrantedAuthority> getAuthorities(final Collection<Role> roles) {
        return getGrantedAuthorities(getPrivileges(roles));
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

    private List<GrantedAuthority> getGrantedAuthorities(final List<String> privileges) {
        final List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        for (final String privilege : privileges) {
            authorities.add(new SimpleGrantedAuthority(privilege));
        }
        return authorities;
    }

    private List<String> getPrivileges(final Collection<Role> roles) {
        final List<String> privileges = new ArrayList<String>();
        final List<Privilege> collection = new ArrayList<Privilege>();
        for (final Role role : roles) {
            collection.addAll(role.getPrivileges());
        }
        for (final Privilege item : collection) {
            privileges.add(item.getName());
        }
        return privileges;
    }

}
