package se.inera.intyg.webcert.web.web.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.servlet.ModelAndView;
import se.inera.intyg.webcert.web.auth.authorities.AuthoritiesConstants;
import se.inera.intyg.webcert.web.auth.authorities.AuthoritiesResolverUtil;
import se.inera.intyg.webcert.web.auth.authorities.Role;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationTestSetup;
import se.inera.intyg.webcert.web.service.feature.WebcertFeature;
import se.inera.intyg.webcert.web.service.feature.WebcertFeatureService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@RunWith(MockitoJUnitRunner.class)
public class PageControllerTest extends AuthoritiesConfigurationTestSetup {

    @Mock
    private WebCertUserService webCertUserService;
    @Mock
    private WebcertFeatureService webcertFeatureService;
    @InjectMocks
    private PageController controller = new PageController();


    @Before
    public void setup() throws Exception {
        CONFIGURATION_LOADER.afterPropertiesSet();
    }

    @Test
    public void testStartViewForDoctor() {
        when(webCertUserService.getUser()).thenReturn(createMockUser(true));
        when(webcertFeatureService.isFeatureActive(any(WebcertFeature.class))).thenReturn(true);
        ModelAndView result = controller.displayStart();
        assertEquals(PageController.DASHBOARD_VIEW_REDIRECT, result.getViewName());
    }

    @Test
    public void testStartViewForNonDoctor() {
        when(webCertUserService.getUser()).thenReturn(createMockUser(false));
        when(webcertFeatureService.isFeatureActive(any(WebcertFeature.class))).thenReturn(true);
        ModelAndView result = controller.displayStart();
        assertEquals(PageController.ADMIN_VIEW_REDIRECT, result.getViewName());
    }

    @Test
    public void testResolveStartViewDoctor() {
        when(webcertFeatureService.isFeatureActive(any(WebcertFeature.class))).thenReturn(true);
        String result = controller.resolveStartView(createMockUser(true));
        assertEquals(PageController.DASHBOARD_VIEW_REDIRECT, result);
    }

    @Test
    public void testResolveStartViewNonDoctor() {
        when(webcertFeatureService.isFeatureActive(any(WebcertFeature.class))).thenReturn(true);
        String result = controller.resolveStartView(createMockUser(false));
        assertEquals(PageController.ADMIN_VIEW_REDIRECT, result);
    }

    private WebCertUser createMockUser(boolean doctor) {
        Role role = AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_LAKARE);

        if (!doctor) {
            role = AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_ADMIN);
        }

        WebCertUser user = new WebCertUser();
        user.setRoles(AuthoritiesResolverUtil.toMap(role));
        user.setAuthorities(AuthoritiesResolverUtil.toMap(role.getPrivileges()));

        return user;
    }

}
