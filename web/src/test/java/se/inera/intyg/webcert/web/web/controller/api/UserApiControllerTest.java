/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Mottagning;
import se.inera.intyg.infra.security.authorities.CommonAuthoritiesResolver;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Feature;
import se.inera.intyg.webcert.web.service.underskrift.dss.DssSignatureService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.ChangeSelectedUnitRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.WebUserFeaturesRequest;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class UserApiControllerTest {

  private static final String ID = "id";
  @InjectMocks UserApiController userApiController;

  @Mock private WebCertUserService webCertUserService;

  @Mock private DssSignatureService dssSignatureService;

  @Mock private CommonAuthoritiesResolver commonAuthoritiesResolver;

  @Mock private WebCertUser webCertUser;

  @Captor private ArgumentCaptor<Map<String, Feature>> captor;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    when(webCertUserService.getUser()).thenReturn(webCertUser);
  }

  @Test
  void testUserFeaturesEnableDisabled() {
    // Given
    final WebUserFeaturesRequest webUserFeaturesRequest = new WebUserFeaturesRequest();
    webUserFeaturesRequest.setJsLoggning(true);

    final HashMap<String, Feature> features = new HashMap<>();
    when(webCertUser.getFeatures()).thenReturn(features);

    // When
    userApiController.userFeatures(webUserFeaturesRequest);

    // Then
    verify(webCertUser, times(1)).setFeatures(captor.capture());
    assertTrue(captor.getValue().containsKey(AuthoritiesConstants.FEATURE_JS_LOGGNING));
  }

  @Test
  void testUserFeaturesDisableEnabled() {
    // Given
    final WebUserFeaturesRequest webUserFeaturesRequest = new WebUserFeaturesRequest();
    webUserFeaturesRequest.setJsLoggning(false);

    final HashMap<String, Feature> features = new HashMap<>();
    Feature f1 = new Feature();
    f1.setName(AuthoritiesConstants.FEATURE_JS_LOGGNING);
    features.put(f1.getName(), f1);
    when(webCertUser.getFeatures()).thenReturn(features);

    // When
    userApiController.userFeatures(webUserFeaturesRequest);

    // Then
    verify(webCertUser, times(1)).setFeatures(captor.capture());
    assertFalse(captor.getValue().containsKey(AuthoritiesConstants.FEATURE_JS_LOGGNING));
  }

  @Test
  void testUserFeaturesEnableEnabled() {
    // Given
    final WebUserFeaturesRequest webUserFeaturesRequest = new WebUserFeaturesRequest();
    webUserFeaturesRequest.setJsLoggning(true);

    final HashMap<String, Feature> features = new HashMap<>();
    Feature f1 = new Feature();
    f1.setName(AuthoritiesConstants.FEATURE_JS_LOGGNING);
    features.put(f1.getName(), f1);
    when(webCertUser.getFeatures()).thenReturn(features);

    // When
    userApiController.userFeatures(webUserFeaturesRequest);

    // Then
    verify(webCertUser, times(1)).setFeatures(captor.capture());
    assertTrue(captor.getValue().containsKey(AuthoritiesConstants.FEATURE_JS_LOGGNING));
  }

  @Test
  void testUserFeaturesDisableDisabled() {
    // Given
    final WebUserFeaturesRequest webUserFeaturesRequest = new WebUserFeaturesRequest();
    webUserFeaturesRequest.setJsLoggning(false);

    final HashMap<String, Feature> features = new HashMap<>();
    when(webCertUser.getFeatures()).thenReturn(features);

    // When
    userApiController.userFeatures(webUserFeaturesRequest);

    // Then
    verify(webCertUser, times(1)).setFeatures(captor.capture());
    assertFalse(captor.getValue().containsKey(AuthoritiesConstants.FEATURE_JS_LOGGNING));
  }

  @Test
  void testLogout() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpSession session = mock(HttpSession.class);

    when(request.getSession()).thenReturn(session);

    userApiController.logoutUserAfterTimeout(request);

    verify(webCertUserService).scheduleSessionRemoval(session);
  }

  @Test
  void testLogoutCancel() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpSession session = mock(HttpSession.class);

    when(request.getSession()).thenReturn(session);

    userApiController.cancelLogout(request);

    verify(webCertUserService).cancelScheduledLogout(session);
  }

  @Test
  void shallSetUseSigningServiceToTrueIfUnitIsNotWhiteListed() {
    final var request = new ChangeSelectedUnitRequest();
    request.setId(ID);
    final var selectedUnit = new Mottagning();
    selectedUnit.setId(ID);
    final var selectedCareGiver = new Mottagning();
    selectedCareGiver.setId(ID);

    doReturn(true).when(webCertUser).changeValdVardenhet(ID);
    doReturn(selectedUnit).when(webCertUser).getValdVardenhet();
    doReturn(selectedCareGiver).when(webCertUser).getValdVardgivare();
    doReturn(true).when(dssSignatureService).shouldUseSigningService(anyString());

    final var argumentCaptor = ArgumentCaptor.forClass(Boolean.class);
    userApiController.changeSelectedUnitOnUser(request);

    verify(webCertUser).setUseSigningService(argumentCaptor.capture());
    assertTrue(argumentCaptor.getValue());
  }

  @Test
  void shallSetUseSigningServiceToFalseIfUnitIsWhiteListed() {
    final var request = new ChangeSelectedUnitRequest();
    request.setId(ID);
    final var selectedUnit = new Mottagning();
    selectedUnit.setId(ID);
    final var selectedCareGiver = new Mottagning();
    selectedCareGiver.setId(ID);

    doReturn(true).when(webCertUser).changeValdVardenhet(ID);
    doReturn(selectedUnit).when(webCertUser).getValdVardenhet();
    doReturn(selectedCareGiver).when(webCertUser).getValdVardgivare();
    doReturn(false).when(dssSignatureService).shouldUseSigningService(anyString());

    final var argumentCaptor = ArgumentCaptor.forClass(Boolean.class);
    userApiController.changeSelectedUnitOnUser(request);

    verify(webCertUser).setUseSigningService(argumentCaptor.capture());
    assertFalse(argumentCaptor.getValue());
  }
}
