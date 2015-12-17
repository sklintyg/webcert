package se.inera.intyg.webcert.web.web.controller.api;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import se.inera.intyg.webcert.web.auth.authorities.AuthoritiesConstants;
import se.inera.intyg.webcert.web.auth.authorities.Role;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationTestSetup;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.authtestability.UserResource;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class UserRoleResourceTest extends AuthoritiesConfigurationTestSetup {

    @Mock
    private WebCertUserService webCertUserService;

    @InjectMocks
    private UserResource userResource;

    @Captor
    private ArgumentCaptor<String> roleArrCaptor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetUserRoles() throws Exception {
        Role role = AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_LAKARE);

        //Given
        WebCertUser user = Mockito.mock(WebCertUser.class);
        Map<String, Role> roleHashMap = new HashMap<>();
        roleHashMap.put(role.getName(), role);

        Mockito.when(user.getRoles()).thenReturn(roleHashMap);
        Mockito.when(webCertUserService.getUser()).thenReturn(user);

        //When
        final Collection<String> rolesResponse = (Collection<String>) userResource.getUserRoles().getEntity();

        //Then
        assertArrayEquals(new String[]{role.getName()}, rolesResponse.toArray());
    }

    @Test
    public void testSetUserRole() throws Exception {
        //Given
        final WebCertUser user = Mockito.mock(WebCertUser.class);
        Mockito.when(webCertUserService.getUser()).thenReturn(user);

        //When
        //Role newRole = AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_LAKARE);
        userResource.setUserRole(AuthoritiesConstants.ROLE_LAKARE);

        //Then
        Mockito.verify(webCertUserService, times(1)).updateUserRole(roleArrCaptor.capture());
        assertEquals(AuthoritiesConstants.ROLE_LAKARE, roleArrCaptor.getValue());
        //assertEquals(1, roleArrCaptor.getValue().length);
        //assertEquals(newRole.getName(), roleArrCaptor.getValue()[0]);
    }
}
