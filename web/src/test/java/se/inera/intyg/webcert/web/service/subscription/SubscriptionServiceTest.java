/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.subscription;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static se.inera.intyg.infra.security.common.model.AuthoritiesConstants.FEATURE_SUBSCRIPTION_ADAPTATION_PERIOD;
import static se.inera.intyg.infra.security.common.model.AuthoritiesConstants.FEATURE_SUBSCRIPTION_REQUIRED;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.ELEG_AUTHN_CLASSES;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.SITHS_AUTHN_CLASSES;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Mottagning;
import se.inera.intyg.infra.integration.hsatk.model.legacy.SubscriptionAction;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.infra.security.authorities.FeaturesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.schemas.contract.util.HashUtility;
import se.inera.intyg.webcert.web.auth.exceptions.MissingSubscriptionException;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.subscription.enumerations.AuthenticationMethodEnum;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.subscription.enumerations.SubscriptionState;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionServiceTest {

    @Mock
    private SubscriptionRestServiceImpl subscriptionRestService;

    @Mock
    private FeaturesHelper featuresHelper;

    @Mock
    private MonitoringLogService monitoringLogService;

    @Captor
    private ArgumentCaptor<Map<String, String>> restServiceParamCaptor;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    private static final String PERSON_ID = "191212121212";

    @Before
    public void setup() {
        ReflectionTestUtils.setField(subscriptionService, "requireSubscriptionStartDate", "requireSubscriptionStartDate");
    }

    @Test
    public void shouldReturnSubscriptionStateNoneWhenNotFristaende() {
        final var webCertUser = createWebCertSithsUser( 1, 1, 0);
        webCertUser.setOrigin(UserOriginType.DJUPINTEGRATION.name());

        final var subscriptionInfo = subscriptionService.checkSubscriptions(webCertUser);

        assertEquals(SubscriptionState.NONE, subscriptionInfo.getSubscriptionState());
    }

    @Test
    public void shouldReturnSubscriptionStateNoneWhenNoSubscriptionFeaturesActive() {
        final var webCertUser = createWebCertSithsUser(1, 1, 0);

        final var subscriptionInfo = subscriptionService.checkSubscriptions(webCertUser);

        assertEquals(SubscriptionState.NONE, subscriptionInfo.getSubscriptionState());
    }

    @Test
    public void shouldReturnSubscriptionStateAdaptationWhenSubscriptionAdaptation() {
        final var webCertUser = createWebCertElegUser();

        setRestServiceMockToReturn(0);
        setFeaturesHelperMockToReturn(true, false);

        final var subscriptionInfo = subscriptionService.checkSubscriptions(webCertUser);

        assertEquals(SubscriptionState.SUBSCRIPTION_ADAPTATION, subscriptionInfo.getSubscriptionState());
    }

    @Test
    public void shouldReturnSubscriptionStateRequiredWhenFeatureRequiredIsSet() {
        final var webCertUser = createWebCertElegUser();

        setRestServiceMockToReturn(0);
        setFeaturesHelperMockToReturn(true, true);

        final var subscriptionInfo = subscriptionService.checkSubscriptions(webCertUser);

        assertEquals(SubscriptionState.SUBSCRIPTION_REQUIRED, subscriptionInfo.getSubscriptionState());
    }

    @Test
    public void shouldSetActionNoneWhenAcknowledgedWarningElegUser() {
        final var careProviderIndex = 0;
        final var webCertUser = createWebCertElegUser();
        setSelectedCareProviderWithWarning(webCertUser, careProviderIndex);

        subscriptionService.acknowledgeSubscriptionWarning(webCertUser);

        assertEquals(SubscriptionAction.NONE, ((Vardgivare)webCertUser.getValdVardgivare()).getSubscriptionAction());
        assertEquals(SubscriptionAction.NONE, webCertUser.getVardgivare().get(careProviderIndex).getSubscriptionAction());
    }

    @Test
    public void shouldSetActionNoneWhenAcknowledgedWarningForSithsUserWithMultipleCareProviders() {
        final var careProviderIndex = 1;
        final var webCertUser = createWebCertSithsUser(3, 2,1 );
        setSelectedCareProviderWithWarning(webCertUser, careProviderIndex);
        webCertUser.getVardgivare().get(2).setSubscriptionAction(SubscriptionAction.WARN);

        subscriptionService.acknowledgeSubscriptionWarning(webCertUser);

        assertEquals(SubscriptionAction.NONE, ((Vardgivare)webCertUser.getValdVardgivare()).getSubscriptionAction());
        assertEquals(SubscriptionAction.NONE, webCertUser.getVardgivare().get(careProviderIndex).getSubscriptionAction());
        assertEquals(SubscriptionAction.WARN, webCertUser.getVardgivare().get(careProviderIndex + 1).getSubscriptionAction());
        assertEquals(SubscriptionAction.NONE, webCertUser.getVardgivare().get(careProviderIndex - 1).getSubscriptionAction());
    }

    @Test
    public void shouldSetRequireSubscriptionStartDate() {
        final var webCertUser = createWebCertElegUser();

        setRestServiceMockToReturn(0);
        setFeaturesHelperMockToReturn(false, false);
        final var response1 = subscriptionService.checkSubscriptions(webCertUser);
        setFeaturesHelperMockToReturn(true, false);
        final var response2 = subscriptionService.checkSubscriptions(webCertUser);
        setFeaturesHelperMockToReturn(true, true);
        final var response3 = subscriptionService.checkSubscriptions(webCertUser);

        assertEquals("requireSubscriptionStartDate", response1.getRequireSubscriptionStartDate());
        assertEquals("requireSubscriptionStartDate", response2.getRequireSubscriptionStartDate());
        assertEquals("requireSubscriptionStartDate", response3.getRequireSubscriptionStartDate());
    }

    @Test
    public void shouldUsePersonalIdAsOrganizationNumberWhenElegUser() {
        final var webCertUser = createWebCertElegUser();
        final var personId = webCertUser.getPersonId();
        final var expectedOrganizationNumber = personId.substring(2, 8) + "-" + personId.substring(personId.length() - 4);

        setRestServiceMockToReturn(0);
        setFeaturesHelperMockToReturn(true, false);

        subscriptionService.checkSubscriptions(webCertUser);

        verify(subscriptionRestService).getMissingSubscriptions(restServiceParamCaptor.capture(), any(AuthenticationMethodEnum.class));
        assertTrue(restServiceParamCaptor.getValue().containsKey(expectedOrganizationNumber));
        assertEquals("CARE_PROVIDER_HSA_ID_1", restServiceParamCaptor.getValue().get(expectedOrganizationNumber));
    }

    @Test
    public void shouldUseCareProviderOrganizationNumberWhenSithsUser() {
        final var webCertUser = createWebCertSithsUser(1, 1, 1);
        final var expectedOrganizationNumber = "CARE_PROVIDER_ORGANIZATION_NO_1";

        setRestServiceMockToReturn(0);
        setFeaturesHelperMockToReturn(true, false);

        subscriptionService.checkSubscriptions(webCertUser);

        verify(subscriptionRestService).getMissingSubscriptions(restServiceParamCaptor.capture(), any(AuthenticationMethodEnum.class));
        assertTrue(restServiceParamCaptor.getValue().containsKey(expectedOrganizationNumber));
        assertEquals("CARE_PROVIDER_HSA_ID_1", restServiceParamCaptor.getValue().get(expectedOrganizationNumber));
    }

    @Test(expected = MissingSubscriptionException.class)
    public void shouldThrowExceptionIfSithsUserIsMissingSingleSubscriptionWhenSubscriptionRequired() {
        final var webCertUser = createWebCertSithsUser(1, 1, 0);

        setRestServiceMockToReturn(1);
        setFeaturesHelperMockToReturn(false, true);

        subscriptionService.checkSubscriptions(webCertUser);
    }

    @Test(expected = MissingSubscriptionException.class)
    public void shouldThrowExceptionIfSithsUserIsMissingAllSubscriptionsWhenSubscriptionRequired() {
        final var webCertUser = createWebCertSithsUser(3, 1, 1);

        setRestServiceMockToReturn(3);
        setFeaturesHelperMockToReturn(false, true);

        subscriptionService.checkSubscriptions(webCertUser);
    }

    @Test(expected = MissingSubscriptionException.class)
    public void shouldThrowExceptionIfElegUserIsMissingSubscriptionWhenSubscriptionRequired() {
        final var webCertUser = createWebCertElegUser();

        setRestServiceMockToReturn(1);
        setFeaturesHelperMockToReturn(true, true);

        subscriptionService.checkSubscriptions(webCertUser);
    }

    @Test
    public void shouldQueryOrgNumberForAllCareProviders() {
        final var webCertUser = createWebCertSithsUser(3, 1, 0);

        setRestServiceMockToReturn(1);
        setFeaturesHelperMockToReturn(true, true);

        subscriptionService.checkSubscriptions(webCertUser);

        verify(subscriptionRestService).getMissingSubscriptions(restServiceParamCaptor.capture(), any(AuthenticationMethodEnum.class));
        assertEquals(3, restServiceParamCaptor.getValue().size());
    }

    @Test
    public void shouldUsePersonalIdWhenQueryForUnregisteredElegUser() {
        final var restServiceParamCaptor = ArgumentCaptor.forClass(String.class);

        subscriptionService.checkSubscriptionUnregisteredElegUser(PERSON_ID);

        verify(subscriptionRestService).isMissingSubscriptionUnregisteredElegUser(restServiceParamCaptor.capture());
        assertEquals("121212-1212", restServiceParamCaptor.getValue());
    }

    @Test
    public void shouldReturnValueReceivedFromRestServiceForUnregisteredElegUser() {

        setRestServiceUnregisteredElegMockToReturn(true);
        final var boolean1 = subscriptionService.checkSubscriptionUnregisteredElegUser(PERSON_ID);
        setRestServiceUnregisteredElegMockToReturn(false);
        final var boolean2 = subscriptionService.checkSubscriptionUnregisteredElegUser(PERSON_ID);

        assertTrue(boolean1);
        assertFalse(boolean2);
    }

    @Test
    public void shouldReturnTrueWhenAnySubscriptionFeatureIsActivated() {
        setFeaturesHelperMockToReturn(true, false);
        final var boolean1 = subscriptionService.isAnySubscriptionFeatureActive();

        setFeaturesHelperMockToReturn(false, true);
        final var boolean2 = subscriptionService.isAnySubscriptionFeatureActive();

        setFeaturesHelperMockToReturn(true, true);
        final var boolean3 = subscriptionService.isAnySubscriptionFeatureActive();

        setFeaturesHelperMockToReturn(false, false);
        final var boolean4 = subscriptionService.isAnySubscriptionFeatureActive();

        assertTrue(boolean1);
        assertTrue(boolean2);
        assertTrue(boolean3);
        assertFalse(boolean4);
    }

    @Test
    public void shouldReturnTrueOnlyWhenSubscriptionAdaptationIsSinglyActivated() {
        setFeaturesHelperMockToReturn(true, false);
        final var boolean1 = subscriptionService.isSubscriptionAdaptation();

        setFeaturesHelperMockToReturn(true, true);
        final var boolean2 = subscriptionService.isSubscriptionAdaptation();

        setFeaturesHelperMockToReturn(false, false);
        final var boolean3 = subscriptionService.isSubscriptionAdaptation();

        assertTrue(boolean1);
        assertFalse(boolean2);
        assertFalse(boolean3);
    }

    @Test
    public void shouldReturnTrueWheneverSubscriptionRequiredIsActivated() {
        setFeaturesHelperMockToReturn(true, true);
        final var boolean1 = subscriptionService.isSubscriptionRequired();

        setFeaturesHelperMockToReturn(false, true);
        final var boolean2 = subscriptionService.isSubscriptionRequired();

        setFeaturesHelperMockToReturn(false, false);
        final var boolean3 = subscriptionService.isSubscriptionRequired();

        assertTrue(boolean1);
        assertTrue(boolean2);
        assertFalse(boolean3);
    }

    @Test
    public void shouldMonitorLogLoginAttemptsMissingSubscriptionWhenSubscriptionRequired() {
        final var webcertUser = createWebCertSithsUser(3, 1,2);

        setFeaturesHelperMockToReturn(false, true);
        setRestServiceMockToReturn(2);

        subscriptionService.checkSubscriptions(webcertUser);

        verify(monitoringLogService).logLoginAttemptMissingSubscription("only-for-test-use", "SITHS",
            List.of("CARE_PROVIDER_HSA_ID_1", "CARE_PROVIDER_HSA_ID_2").toString());
    }

    @Test
    public void shouldNotMonitorLogLoginAttemptsMissingSubscriptionWhenSubscriptionAdaptation() {
        final var webcertUser = createWebCertSithsUser(3, 1,2);

        setFeaturesHelperMockToReturn(true, false);
        setRestServiceMockToReturn(2);

        subscriptionService.checkSubscriptions(webcertUser);

        verifyNoInteractions(monitoringLogService);
    }

    @Test
    public void shouldMonitorLogLoginAttemptsMissingSubscriptionWhenSubscriptionRequiredForUnregElegUser() {
        final var expectedUserId = HashUtility.hash(PERSON_ID);
        final var expectedOrg = HashUtility.hash("121212-1212");

        setFeaturesHelperMockToReturn(false, true);
        setRestServiceUnregisteredElegMockToReturn(true);

        subscriptionService.checkSubscriptionUnregisteredElegUser(PERSON_ID);

        verify(monitoringLogService).logLoginAttemptMissingSubscription(expectedUserId, "ELEG", expectedOrg);
    }

    @Test
    public void shouldNotMonitorLogLoginAttemptsMissingSubscriptionWhenSubscriptiondaptationForUnregElegUser() {

        setFeaturesHelperMockToReturn(true, false);
        setRestServiceUnregisteredElegMockToReturn(true);

        subscriptionService.checkSubscriptionUnregisteredElegUser(PERSON_ID);

        verifyNoInteractions(monitoringLogService);
    }

    @Test
    public void shouldSetWarningActionOnCareProviderDuringAdaptationForElegUser() {
        final var webCertUser = createWebCertElegUser();

        setRestServiceMockToReturn(1);
        setFeaturesHelperMockToReturn(true, false);

        subscriptionService.checkSubscriptions(webCertUser);

        assertEquals(SubscriptionAction.WARN, webCertUser.getVardgivare().get(0).getSubscriptionAction());
        assertEquals(SubscriptionAction.NONE, webCertUser.getVardgivare().get(0).getVardenheter().get(0).getSubscriptionAction());
    }

    @Test
    public void shouldSetBlockActionOnAllLevelsWhenSubscriptionRequiredForSithsUser() {
        final var webCertUser = createWebCertSithsUser(2, 1, 1);

        setRestServiceMockToReturn(1);
        setFeaturesHelperMockToReturn(false, true);

        subscriptionService.checkSubscriptions(webCertUser);

        assertEquals(SubscriptionAction.BLOCK, webCertUser.getVardgivare().get(0).getSubscriptionAction());
        assertEquals(SubscriptionAction.BLOCK, webCertUser.getVardgivare().get(0).getVardenheter().get(0).getSubscriptionAction());
        assertEquals(SubscriptionAction.BLOCK, webCertUser.getVardgivare().get(0).getVardenheter().get(0).getMottagningar().get(0)
            .getSubscriptionAction());

        assertEquals(SubscriptionAction.NONE, webCertUser.getVardgivare().get(1).getSubscriptionAction());
        assertEquals(SubscriptionAction.NONE, webCertUser.getVardgivare().get(1).getVardenheter().get(0).getSubscriptionAction());
        assertEquals(SubscriptionAction.NONE, webCertUser.getVardgivare().get(1).getVardenheter().get(0).getMottagningar().get(0)
            .getSubscriptionAction());
    }

    private void setRestServiceMockToReturn(int careProviderHsaIdCount) {
        final var careProviderHsaIds = new ArrayList<String>();

        for (var i = 1; i <= careProviderHsaIdCount; i++) {
            careProviderHsaIds.add("CARE_PROVIDER_HSA_ID_" + i);
        }
        when(subscriptionRestService.getMissingSubscriptions(any(), any())).thenReturn(careProviderHsaIds);
    }

    private void setRestServiceUnregisteredElegMockToReturn(Boolean isMissingSubscription) {
        when(subscriptionRestService.isMissingSubscriptionUnregisteredElegUser(any(String.class))).thenReturn(isMissingSubscription);
    }

    private void setFeaturesHelperMockToReturn(boolean subscriptionAdaptation, boolean subscriptionRequired) {
        when(featuresHelper.isFeatureActive(FEATURE_SUBSCRIPTION_ADAPTATION_PERIOD)).thenReturn(subscriptionAdaptation);
        when(featuresHelper.isFeatureActive(FEATURE_SUBSCRIPTION_REQUIRED)).thenReturn(subscriptionRequired);
    }

    private WebCertUser createWebCertElegUser() {
        final var webCertUser = createBaseWebCertUser();
        webCertUser.setAuthenticationScheme(ELEG_AUTHN_CLASSES.get(2));
        webCertUser.setVardgivare(getCareProviders(1, 1, 0));

        final var careProvider = webCertUser.getVardgivare().get(0);
        careProvider.getVardenheter().get(0).setId(careProvider.getId());
        return webCertUser;
    }

    private WebCertUser createWebCertSithsUser(int numCareProviders, int numCareUnits, int numUnits) {
        final var webCertUser = createBaseWebCertUser();
        webCertUser.setAuthenticationScheme(SITHS_AUTHN_CLASSES.get(1));
        webCertUser.setVardgivare(getCareProviders(numCareProviders, numCareUnits, numUnits));
        return webCertUser;
    }

    private WebCertUser createBaseWebCertUser() {
        final var webCertUser = new WebCertUser();
        webCertUser.setPersonId(PERSON_ID);
        webCertUser.setRoles(Map.of(AuthoritiesConstants.ROLE_PRIVATLAKARE, new Role()));
        webCertUser.setOrigin(UserOriginType.NORMAL.name());
        return webCertUser;
    }

    private List<Vardgivare> getCareProviders(int numCareProviders, int numCareUnits, int numUnits) {
        final var careProviders = new ArrayList<Vardgivare>();
        for (int i = 1; i <= numCareProviders; i++) {
            final var careProvider = new Vardgivare();
            careProvider.setId("CARE_PROVIDER_HSA_ID_" + i);
            careProvider.setVardenheter(getCareUnits(i, numCareUnits, numUnits));
            careProviders.add(careProvider);
        }
        return careProviders;
    }

    private List<Vardenhet> getCareUnits(int careProviderId, int numCareUnits, int numUnits) {
        final var careUnits = new ArrayList<Vardenhet>();
        for (int j = 1; j <= numCareUnits; j++) {
            final var careUnit = new Vardenhet();
            final var careUnitId = String.valueOf(careProviderId) + j;
            final var careProviderOrganizationNo = "CARE_PROVIDER_ORGANIZATION_NO_" + careProviderId;
            careUnit.setId("CARE_UNIT_HSA_ID_" + careProviderId + j);
            careUnit.setVardgivareOrgnr(careProviderOrganizationNo);
            careUnit.setMottagningar(getUnits(careProviderOrganizationNo, careUnitId, numUnits));
            careUnits.add(careUnit);
        }
        return careUnits;
    }

    private List<Mottagning> getUnits(String careProviderOrganizationNo, String careUnitId, int numUnits) {
        final var units = new ArrayList<Mottagning>();
        for (int k = 1; k <= numUnits; k++) {
            final var unit = new Mottagning();
            unit.setId("UNIT_HSA_ID_" + careUnitId + k);
            unit.setParentHsaId("CARE_UNIT_HSA_ID_" + careUnitId);
            unit.setVardgivareOrgnr(careProviderOrganizationNo);
            units.add(unit);
        }
        return units;
    }

    private void setSelectedCareProviderWithWarning(WebCertUser webCertUser, int careProviderIndex) {
        webCertUser.getVardgivare().get(careProviderIndex).setSubscriptionAction(SubscriptionAction.WARN);
        webCertUser.setValdVardgivare(webCertUser.getVardgivare().get(careProviderIndex));
    }

}
