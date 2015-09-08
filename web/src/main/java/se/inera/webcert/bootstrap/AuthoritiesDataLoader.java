package se.inera.webcert.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import se.inera.webcert.common.model.UserPrivileges;
import se.inera.webcert.common.model.UserRoles;
import se.inera.webcert.persistence.roles.model.Privilege;
import se.inera.webcert.persistence.roles.model.Role;
import se.inera.webcert.persistence.roles.repository.PrivilegeRepository;
import se.inera.webcert.persistence.roles.repository.RoleRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Magnus Ekstrand on 28/08/15.
 */
@Component
public class AuthoritiesDataLoader implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(AuthoritiesDataLoader.class);

    private boolean alreadySetup = false;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PrivilegeRepository privilegeRepository;

    // API

    @Override
    @Transactional
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        if (alreadySetup) {
            return;
        }

        // == create privileges
        loadPrivileges(UserPrivileges.values());

        // == create roles
        loadRoles(UserRoles.values());

        alreadySetup = true;
    }


    // - - - - - Private scope - - - - -

    private void loadPrivileges(UserPrivileges[] userPrivileges) {
        if (userPrivileges == null) {
            throw new IllegalArgumentException("User privileges cannot be null");
        }

        for (UserPrivileges up : userPrivileges) {
            createPrivilegeIfNotFound(up.name());
        }

    }

    private void loadRoles(UserRoles[] userRoles) {
        if (userRoles == null) {
            throw new IllegalArgumentException("User roles cannot be null");
        }

        Map<UserRoles, List<UserPrivileges>> userRolesUserPrivilegesMap = getUserRolesPrivilegesMap();

        for (UserRoles ur : userRoles) {
            if (userRolesUserPrivilegesMap.containsKey(ur)) {
                System.err.println("Getting privileges for UserRole: " + ur.name());
                createRoleIfNotFound(ur.name(), getPrivilegeList(userRolesUserPrivilegesMap.get(ur)));
            } else {
                System.err.println(String.format("User role %s has not been setup with any privileges. Role will not be created.", ur.name()));
                LOG.warn("User role {} has not been setup with any privileges. Role will not be created.", ur.name());
            }
        }

    }

    private Privilege createPrivilegeIfNotFound(final String name) {
        Privilege privilege = privilegeRepository.findByName(name);
        if (privilege == null) {
            privilege = new Privilege(name);
            privilegeRepository.save(privilege);
        }
        return privilege;
    }

    private Role createRoleIfNotFound(final String name, final Collection<Privilege> privileges) {
        Role role = roleRepository.findByName(name);
        if (role == null) {
            role = new Role(name);

            for (Privilege p : privileges) {
                role.getPrivileges().add(privilegeRepository.findByName(p.getName()));
            }

            // role.setPrivileges(privileges);
            roleRepository.save(role);
        }
        return role;
    }

    private List<Privilege> getPrivilegeList(final List<UserPrivileges> userPrivileges) {
        if (userPrivileges == null) {
            System.err.println("NULL in privileges");
        }

        List<Privilege> privileges = new ArrayList<>();

        for (UserPrivileges up: userPrivileges) {
            Privilege privilege = new Privilege(up.name());
            privileges.add(privilege);
        }

        return privileges;
    }

    private Map<UserRoles, List<UserPrivileges>> getUserRolesPrivilegesMap() {

        Map<UserRoles, List<UserPrivileges>> map = new HashMap<>();

        // TODO read from some mapping file/class

        map.put(UserRoles.ROLE_LAKARE, getPrivileges(UserRoles.ROLE_LAKARE));
        map.put(UserRoles.ROLE_PRIVATLAKARE, getPrivileges(UserRoles.ROLE_PRIVATLAKARE));
        map.put(UserRoles.ROLE_VARDADMINISTRATOR, getPrivileges(UserRoles.ROLE_VARDADMINISTRATOR));

        return map;
    }

    private List<UserPrivileges> getPrivileges(UserRoles userRoles) {
        List<UserPrivileges> userPrivileges = null;

        switch (userRoles) {
            case ROLE_LAKARE:
                userPrivileges = getLakarePrivilegeList();
                break;
            case ROLE_LAKARE_DJUPINTEGRERAD:
                userPrivileges = getDjupintegreradLakarePrivilegeList();
                break;
            case ROLE_LAKARE_UTHOPP:
                userPrivileges = getUthoppsLakarePrivilegeList();
                break;
            case ROLE_PRIVATLAKARE:
                userPrivileges = getPrivatLakarePrivilegeList();
                break;
            case ROLE_TANDLAKARE:
                userPrivileges = getTandlakarePrivilegeList();
                break;
            default:
                userPrivileges = getVardadministratorPrivilegeList();
        }

        return userPrivileges;
    }

    private List<UserPrivileges> getVardadministratorPrivilegeList() {
        return Arrays.asList(new UserPrivileges[] {
            UserPrivileges.PRIVILEGE_SKRIVA_INTYG,
            UserPrivileges.PRIVILEGE_KOPIERA_INTYG,
            UserPrivileges.PRIVILEGE_VIDAREBEFORDRA_FRAGASVAR,
            UserPrivileges.PRIVILEGE_VIDAREBEFORDRA_UTKAST });
    }

    private List<UserPrivileges> getTandlakarePrivilegeList() {
        // TODO ordna med rättigheter för tandläkare
        return Arrays.asList(UserPrivileges.values());
    }

    private List<UserPrivileges> getPrivatLakarePrivilegeList() {
        return Arrays.asList(new UserPrivileges[] {
                UserPrivileges.PRIVILEGE_SKRIVA_INTYG,
                UserPrivileges.PRIVILEGE_KOPIERA_INTYG,
                UserPrivileges.PRIVILEGE_MAKULERA_INTYG,
                UserPrivileges.PRIVILEGE_SIGNERA_INTYG,
                UserPrivileges.PRIVILEGE_BESVARA_KOMPLETTERINGSFRAGA });
    }

    private List<UserPrivileges> getUthoppsLakarePrivilegeList() {
        // TODO ordna med rättigheter för tandläkare
        return Arrays.asList(UserPrivileges.values());
    }

    private List<UserPrivileges> getDjupintegreradLakarePrivilegeList() {
        return Arrays.asList(new UserPrivileges[] {
                UserPrivileges.PRIVILEGE_SKRIVA_INTYG,
                UserPrivileges.PRIVILEGE_KOPIERA_INTYG,
                UserPrivileges.PRIVILEGE_MAKULERA_INTYG,
                UserPrivileges.PRIVILEGE_SIGNERA_INTYG,
                UserPrivileges.PRIVILEGE_BESVARA_KOMPLETTERINGSFRAGA });
    }

    private List<UserPrivileges> getLakarePrivilegeList() {
        return Arrays.asList(UserPrivileges.values());
    }
}

