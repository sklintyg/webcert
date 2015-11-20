package se.inera.intyg.webcert.web.web.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.servlet.ModelAndView;
import se.inera.webcert.common.security.authority.UserPrivilege;
import se.inera.webcert.common.security.authority.UserRole;
import se.inera.intyg.webcert.web.service.feature.WebcertFeature;
import se.inera.intyg.webcert.web.service.feature.WebcertFeatureService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        WebCertUser user = new WebCertUser();
        user.setRoles(getGrantedRole(doctor));
        user.setAuthorities(getGrantedPrivileges(doctor));
        return user;
    }

    private Map<String, UserRole> getGrantedRole(boolean doctor) {
        Map<String, UserRole> map = new HashMap<>();

        if (doctor) {
            map.put(UserRole.ROLE_LAKARE.name(), UserRole.ROLE_LAKARE);
        } else {
            map.put(UserRole.ROLE_VARDADMINISTRATOR.name(), UserRole.ROLE_VARDADMINISTRATOR);
        }

        return map;
    }

    private Map<String, UserPrivilege> getGrantedPrivileges(boolean doctor) {
        List<UserPrivilege> list = Arrays.asList(UserPrivilege.values());
        Map<String, UserPrivilege> privilegeMap = new HashMap<>();

        // convert list to map
        if (doctor) {
            privilegeMap = Maps.uniqueIndex(list, new Function<UserPrivilege, String>() {
                @Override
                public String apply(UserPrivilege userPrivilege) {
                    return userPrivilege.name();
                }
            });
        }

        return privilegeMap;
    }

}
