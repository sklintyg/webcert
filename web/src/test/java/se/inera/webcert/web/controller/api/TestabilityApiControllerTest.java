package se.inera.webcert.web.controller.api;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;

import java.util.Collection;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import se.inera.webcert.common.security.authority.UserRole;
import se.inera.webcert.persistence.privatlakaravtal.repository.AvtalRepository;
import se.inera.webcert.persistence.privatlakaravtal.repository.GodkantAvtalRepository;
import se.inera.webcert.service.user.WebCertUserService;
import se.inera.webcert.service.user.dto.WebCertUser;

public class TestabilityApiControllerTest {

    @Mock
    private WebCertUserService webCertUserService;

    @InjectMocks
    private TestabilityApiController testabilityApiController;

    @Captor
    private ArgumentCaptor<String[]> roleArrCaptor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetUserRoles() throws Exception {
        //Given
        final WebCertUser user = Mockito.mock(WebCertUser.class);
        final HashMap<String, UserRole> roleHashMap = new HashMap<>();
        roleHashMap.put(UserRole.ROLE_LAKARE.name(), UserRole.ROLE_LAKARE);
        Mockito.when(user.getRoles()).thenReturn(roleHashMap);
        Mockito.when(webCertUserService.getUser()).thenReturn(user);

        //When
        final Collection<String> rolesResponse = (Collection<String>) testabilityApiController.getUserRoles().getEntity();

        //Then
        assertArrayEquals(new String[]{UserRole.ROLE_LAKARE.name()}, rolesResponse.toArray());
    }

    @Test
    public void testSetUserRole() throws Exception {
        //Given
        final WebCertUser user = Mockito.mock(WebCertUser.class);
        Mockito.when(webCertUserService.getUser()).thenReturn(user);

        //When
        final UserRole newRole = UserRole.ROLE_LAKARE_UTHOPP;
        testabilityApiController.setUserRole(newRole);

        //Then
        Mockito.verify(webCertUserService, times(1)).updateUserRoles(roleArrCaptor.capture());
        assertEquals(1, roleArrCaptor.getValue().length);
        assertEquals(newRole.name(), roleArrCaptor.getValue()[0]);
    }
}
