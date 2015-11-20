package se.inera.intyg.webcert.web.bootstrap;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.event.ContextRefreshedEvent;
import se.inera.webcert.common.security.authority.UserPrivilege;
import se.inera.webcert.common.security.authority.UserRole;
import se.inera.webcert.persistence.roles.model.Privilege;
import se.inera.webcert.persistence.roles.model.Role;
import se.inera.webcert.persistence.roles.model.TitleCode;
import se.inera.webcert.persistence.roles.repository.PrivilegeRepository;
import se.inera.webcert.persistence.roles.repository.RoleRepository;
import se.inera.webcert.persistence.roles.repository.TitleCodeRepository;

import java.util.Collection;
import java.util.HashSet;

/**
 * The AuthoritiesDataLoader is not very well suited for unit-testing, given that it has a single entry-point and then
 * creates roles, privileges and titleCodes using three different repositories with interdependent data.
 *
 * A future refactoring may be to extract role, privilege and titleCode creation to separate components which then could
 * expose a domain-specific API much more suitable for unit testing than the AuthoritiesDataLoader.
 *
 * Created by eriklupander on 2015-10-19.
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthoritiesDataLoaderTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PrivilegeRepository privilegeRepository;

    @Mock
    private TitleCodeRepository titleCodeRepository;

    @InjectMocks
    private AuthoritiesDataLoader testee = new AuthoritiesDataLoader();

    @Mock
    private ContextRefreshedEvent contextRefreshedEvent;

    @Mock
    private Role roleLakare;

    @Before
    public void init() {
        roleLakare = new Role();
        roleLakare.setId(1L);
        roleLakare.setName(UserRole.ROLE_LAKARE.name());
        roleLakare.setText("Läkare");
        Collection<Privilege> lakarPrivileges = buildLakarPrivileges();
        roleLakare.setPrivileges(lakarPrivileges);
    }

    private Collection<Privilege> buildLakarPrivileges() {
        Collection<Privilege> lakarPrivileges = new HashSet<>();
        for (UserPrivilege up : UserPrivilege.values()) {
            Privilege p = new Privilege();
            p.setName(up.name());
            p.setText(up.text());
            lakarPrivileges.add(p);
        }
        return lakarPrivileges;
    }

    @Test
    public void testCreateFromEmptyState() {

        // First time, return null (e.g. make sure we create role). Next time, return a concrete class to use when titles
        // are created.
        when(roleRepository.findByName(UserRole.ROLE_LAKARE.name())).thenReturn(null, roleLakare);
        when(roleRepository.save(roleLakare)).thenReturn(roleLakare);

        testee.onApplicationEvent(contextRefreshedEvent);
        verify(privilegeRepository, times(UserPrivilege.values().length)).save(any(Privilege.class));
        verify(roleRepository, times(UserRole.values().length)).save(any(Role.class));
        verify(titleCodeRepository, times(3)).save(any(TitleCode.class));
    }

    @Test
    public void testPrivilegeAddedToRoleTriggersUpdate() {

        // Create a sample Privilege we can use to remove from the privileges of the role.
        Privilege p = new Privilege();
        p.setName(UserPrivilege.PRIVILEGE_SIGNERA_INTYG.name());
        roleLakare.getPrivileges().remove(p);

        // E.g. mock that the ROLE_LAKARE already exists.
        when(roleRepository.findByName(UserRole.ROLE_LAKARE.name())).thenReturn(roleLakare);
        when(roleRepository.save(roleLakare)).thenReturn(roleLakare);

        testee.onApplicationEvent(contextRefreshedEvent);

        verify(privilegeRepository, times(UserPrivilege.values().length)).save(any(Privilege.class));
        verify(roleRepository, times(10)).save(any(Role.class));
        verify(titleCodeRepository, times(3)).save(any(TitleCode.class));
    }
}
