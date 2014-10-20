package se.inera.webcert.web.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.servlet.ModelAndView;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.service.feature.Features;
import se.inera.webcert.service.feature.WebcertFeatureService;
import se.inera.webcert.web.service.WebCertUserService;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

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
        when(webcertFeatureService.isFeatureActive(any(Features.class))).thenReturn(true);
        ModelAndView result = controller.displayStart();
        assertEquals(PageController.DASHBOARD_VIEW_REDIRECT, result.getViewName());
    }

    @Test
    public void testStartViewForNonDoctor() {
        when(webCertUserService.getWebCertUser()).thenReturn(createMockUser(false));
        when(webcertFeatureService.isFeatureActive(any(Features.class))).thenReturn(true);
        ModelAndView result = controller.displayStart();
        assertEquals(PageController.ADMIN_VIEW_REDIRECT, result.getViewName());
    }

    @Test
    public void testResolveStartViewDoctor() {
        when(webcertFeatureService.isFeatureActive(any(Features.class))).thenReturn(true);
        String result = controller.resolveStartView(createMockUser(true));
        assertEquals(PageController.DASHBOARD_VIEW_REDIRECT, result);
    }

    @Test
    public void testResolveStartViewNonDoctor() {
        when(webcertFeatureService.isFeatureActive(any(Features.class))).thenReturn(true);
        String result = controller.resolveStartView(createMockUser(false));
        assertEquals(PageController.ADMIN_VIEW_REDIRECT, result);
    }
    private WebCertUser createMockUser(boolean isLakare) {
        WebCertUser user = new WebCertUser();
        user.setLakare(isLakare);
        return user;
    }

}
