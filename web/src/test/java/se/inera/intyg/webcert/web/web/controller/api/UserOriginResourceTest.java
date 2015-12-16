package se.inera.intyg.webcert.web.web.controller.api;

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
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationTestSetup;
import se.inera.intyg.webcert.web.security.WebCertUserOriginType;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.authtestability.UserResource;

public class UserOriginResourceTest extends AuthoritiesConfigurationTestSetup {

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
    public void testGetUserOrigin() throws Exception {
        String origin = WebCertUserOriginType.NORMAL.name();

        //Given
        WebCertUser user = Mockito.mock(WebCertUser.class);

        Mockito.when(user.getOrigin()).thenReturn(origin);
        Mockito.when(webCertUserService.getUser()).thenReturn(user);

        //When
        final String originResponse = (String) userResource.getOrigin().getEntity();

        //Then
        assertEquals(origin, originResponse);
    }

    @Test
    public void testSetUserRole() throws Exception {
        //Given
        final WebCertUser user = Mockito.mock(WebCertUser.class);
        Mockito.when(webCertUserService.getUser()).thenReturn(user);

        //When
        String newOrigin = WebCertUserOriginType.UTHOPP.name();
        userResource.setOrigin(newOrigin);

        //Then
        Mockito.verify(webCertUserService, times(1)).updateOrigin(roleArrCaptor.capture());
        assertEquals(newOrigin, roleArrCaptor.getValue());
    }
}
