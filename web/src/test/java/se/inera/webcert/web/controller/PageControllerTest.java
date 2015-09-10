package se.inera.webcert.web.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.servlet.ModelAndView;
import se.inera.webcert.common.security.authority.SimpleGrantedAuthority;
import se.inera.webcert.common.security.authority.UserPrivilege;
import se.inera.webcert.common.security.authority.UserRole;
import se.inera.webcert.service.feature.WebcertFeature;
import se.inera.webcert.service.feature.WebcertFeatureService;
import se.inera.webcert.service.user.WebCertUserService;
import se.inera.webcert.service.user.dto.WebCertUser;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@RunWith(MockitoJUnitRunner.class)
public class PageControllerTest {

    @Mock
    private WebCertUserService webCertUserService;
    @Mock
    private WebcertFeatureService webcertFeatureService;

    @InjectMocks
    private PageController controller = new PageController();

    @Test
    public void testStartViewForDoctor() {
        when(webCertUserService.getWebCertUser()).thenReturn(createMockUser(true));
        when(webcertFeatureService.isFeatureActive(any(WebcertFeature.class))).thenReturn(true);
        ModelAndView result = controller.displayStart();
        assertEquals(PageController.DASHBOARD_VIEW_REDIRECT, result.getViewName());
    }

    @Test
    public void testStartViewForNonDoctor() {
        when(webCertUserService.getWebCertUser()).thenReturn(createMockUser(false));
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

    private WebCertUser createMockUser(boolean isLakare) {
        return new WebCertUser(getGrantedRole(isLakare), getGrantedPrivileges(isLakare));
    }

    private GrantedAuthority getGrantedRole(boolean isLakare) {
        if (isLakare) {
            return new SimpleGrantedAuthority(UserRole.ROLE_LAKARE.name(), UserRole.ROLE_LAKARE.toString());
        }

        return new SimpleGrantedAuthority(UserRole.ROLE_VARDADMINISTRATOR.name(), UserRole.ROLE_VARDADMINISTRATOR.toString());
    }


    private Collection<? extends GrantedAuthority> getGrantedPrivileges(boolean isLakare) {
        Set<SimpleGrantedAuthority> privileges = new HashSet<SimpleGrantedAuthority>();

        if (isLakare) {
            for (UserPrivilege userPrivilege : UserPrivilege.values()) {
                privileges.add(new SimpleGrantedAuthority(userPrivilege.name(), userPrivilege.toString()));
            }
        }

        return privileges;
    }

}
