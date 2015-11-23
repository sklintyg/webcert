package se.inera.intyg.webcert.web.auth.common;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.webcert.common.security.authority.UserPrivilege;
import se.inera.webcert.common.security.authority.UserRole;
import se.inera.webcert.persistence.roles.model.Privilege;
import se.inera.webcert.persistence.roles.model.Role;
import se.inera.webcert.persistence.roles.repository.RoleRepository;
import se.inera.intyg.webcert.web.service.feature.WebcertFeatureService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base UserDetailsService for both Siths and E-leg based authentication.
 *
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

    public final UserRole getRoleAuthority(final Role role) {
        return getGrantedRole(role);
    }

    public final Map<String, UserPrivilege> getPrivilegeAuthorities(final Role role) {
        List<UserPrivilege> grantedPrivileges = getGrantedPrivileges(getPrivileges(role));

        // convert list to map
        Map<String, UserPrivilege> privilegeMap = Maps.uniqueIndex(grantedPrivileges, new Function<UserPrivilege, String>() {
            public String apply(UserPrivilege userPrivilege) {
                return userPrivilege.name();
            }
        });

        return privilegeMap;
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

    protected void decorateWebCertUserWithAvailableFeatures(WebCertUser webcertUser) {
        Set<String> availableFeatures = webcertFeatureService.getActiveFeatures();
        webcertUser.setAktivaFunktioner(availableFeatures);
    }

    protected Map<String, UserRole> roleToMap(UserRole userRole) {
        Map<String, UserRole> map = new HashMap<>();
        map.put(userRole.name(), userRole);
        return map;
    }

    // - - - - - Private scope - - - - -

    private UserRole getGrantedRole(final Role role) {
        return UserRole.valueOf(role.getName());
    }

    private List<UserPrivilege> getGrantedPrivileges(final List<Privilege> privileges) {
        final List<UserPrivilege> authorities = new ArrayList<>();
        for (final Privilege privilege : privileges) {
            authorities.add(UserPrivilege.valueOf(privilege.getName()));
        }
        return authorities;
    }

    private List<Privilege> getPrivileges(final Role role) {
        List<Privilege> privileges = new ArrayList<>();
        privileges.addAll(role.getPrivileges());
        return privileges;
    }

}
