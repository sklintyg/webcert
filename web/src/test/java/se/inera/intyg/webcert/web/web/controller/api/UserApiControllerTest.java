package se.inera.intyg.webcert.web.web.controller.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import se.inera.intyg.webcert.web.service.feature.WebcertFeature;
import se.inera.intyg.webcert.web.service.feature.WebcertFeatureService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogServiceImpl;
import se.inera.intyg.webcert.web.service.privatlakaravtal.AvtalService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.WebUserFeaturesRequest;

import java.util.HashSet;
import java.util.Set;

public class UserApiControllerTest {

    @InjectMocks
    UserApiController userApiController;

    @Mock
    private AvtalService avtalService;

    @Mock
    private WebcertFeatureService featureService;

    @Mock
    private MonitoringLogServiceImpl monitoringService;

    @Mock
    private WebCertUserService webCertUserService;

    @Mock
    private WebCertUser webCertUser;

    @Captor
    private ArgumentCaptor<Set<String>> stringSetCaptor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Mockito.when(webCertUserService.getUser()).thenReturn(webCertUser);
    }

    @Test
    public void testUserFeaturesEnableDisabled() throws Exception {
        //Given
        final WebUserFeaturesRequest webUserFeaturesRequest = new WebUserFeaturesRequest();
        webUserFeaturesRequest.setJsLoggning(true);
        webUserFeaturesRequest.setJsMinified(true);

        final HashSet<String> features = new HashSet<>();
        Mockito.when(webCertUser.getFeatures()).thenReturn(features);

        //When
        userApiController.userFeatures(webUserFeaturesRequest);

        //Then
        Mockito.verify(webCertUser, times(1)).setFeatures(stringSetCaptor.capture());
        assertTrue(stringSetCaptor.getValue().contains(WebcertFeature.JS_LOGGNING.getName()));
        assertTrue(stringSetCaptor.getValue().contains(WebcertFeature.JS_MINIFIED.getName()));

        Mockito.verify(featureService, times(0)).setFeature(anyString(), anyString());
    }

    @Test
    public void testUserFeaturesDisableEnabled() throws Exception {
        //Given
        final WebUserFeaturesRequest webUserFeaturesRequest = new WebUserFeaturesRequest();
        webUserFeaturesRequest.setJsLoggning(false);
        webUserFeaturesRequest.setJsMinified(false);

        final HashSet<String> features = new HashSet<>();
        features.add(WebcertFeature.JS_LOGGNING.getName());
        features.add(WebcertFeature.JS_MINIFIED.getName());
        Mockito.when(webCertUser.getFeatures()).thenReturn(features);

        //When
        userApiController.userFeatures(webUserFeaturesRequest);

        //Then
        Mockito.verify(webCertUser, times(1)).setFeatures(stringSetCaptor.capture());
        assertFalse(stringSetCaptor.getValue().contains(WebcertFeature.JS_LOGGNING.getName()));
        assertFalse(stringSetCaptor.getValue().contains(WebcertFeature.JS_MINIFIED.getName()));

        Mockito.verify(featureService, times(0)).setFeature(anyString(), anyString());
    }

    @Test
    public void testUserFeaturesEnableEnabled() throws Exception {
        //Given
        final WebUserFeaturesRequest webUserFeaturesRequest = new WebUserFeaturesRequest();
        webUserFeaturesRequest.setJsLoggning(true);
        webUserFeaturesRequest.setJsMinified(true);

        final HashSet<String> features = new HashSet<>();
        features.add(WebcertFeature.JS_LOGGNING.getName());
        features.add(WebcertFeature.JS_MINIFIED.getName());
        Mockito.when(webCertUser.getFeatures()).thenReturn(features);

        //When
        userApiController.userFeatures(webUserFeaturesRequest);

        //Then
        Mockito.verify(webCertUser, times(1)).setFeatures(stringSetCaptor.capture());
        assertTrue(stringSetCaptor.getValue().contains(WebcertFeature.JS_LOGGNING.getName()));
        assertTrue(stringSetCaptor.getValue().contains(WebcertFeature.JS_MINIFIED.getName()));

        Mockito.verify(featureService, times(0)).setFeature(anyString(), anyString());
    }

    @Test
    public void testUserFeaturesDisableDisabled() throws Exception {
        //Given
        final WebUserFeaturesRequest webUserFeaturesRequest = new WebUserFeaturesRequest();
        webUserFeaturesRequest.setJsLoggning(false);
        webUserFeaturesRequest.setJsMinified(false);

        final HashSet<String> features = new HashSet<>();
        Mockito.when(webCertUser.getFeatures()).thenReturn(features);

        //When
        userApiController.userFeatures(webUserFeaturesRequest);

        //Then
        Mockito.verify(webCertUser, times(1)).setFeatures(stringSetCaptor.capture());
        assertFalse(stringSetCaptor.getValue().contains(WebcertFeature.JS_LOGGNING.getName()));
        assertFalse(stringSetCaptor.getValue().contains(WebcertFeature.JS_MINIFIED.getName()));

        Mockito.verify(featureService, times(0)).setFeature(anyString(), anyString());
    }

}
