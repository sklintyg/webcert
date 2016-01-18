/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
