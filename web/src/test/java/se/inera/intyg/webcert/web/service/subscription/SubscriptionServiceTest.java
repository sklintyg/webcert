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
import java.util.Collections;
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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Mottagning;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.infra.security.authorities.FeaturesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.schemas.contract.util.HashUtility;
import se.inera.intyg.webcert.integration.kundportalen.service.SubscriptionRestServiceImpl;
import se.inera.intyg.webcert.web.auth.exceptions.MissingSubscriptionException;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.subscription.dto.SubscriptionAction;
import se.inera.intyg.webcert.web.service.subscription.dto.SubscriptionInfo;
import se.inera.intyg.webcert.integration.kundportalen.enumerations.AuthenticationMethodEnum;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

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
    private static final String ADAPTATION_START_DATE = "subscriptionAdaptationStartDate";
    private static final String REQUIRE_START_DATE = "requireSubscriptionStartDate";

    @Before
    public void setup() {
        ReflectionTestUtils.setField(subscriptionService, ADAPTATION_START_DATE, ADAPTATION_START_DATE);
        ReflectionTestUtils.setField(subscriptionService, REQUIRE_START_DATE, REQUIRE_START_DATE);
    }

    @Test
    public void shouldNotCallRestServiceWhenNotFristaendeUser() {
        final var sithsUser = createWebCertSithsUser(1, 1, 1);
        sithsUser.setOrigin(UserOriginType.DJUPINTEGRATION.name());

        setFeaturesHelperMockToReturn(true, true);
        setRestServiceMockToReturn(4);

        subscriptionService.checkSubscriptions(sithsUser);

        verifyNoInteractions(subscriptionRestService);
    }

    @Test
    public void shouldAlwaysHaveSubscriptionActionNoneAndEmptyMissingSubscriptionListWhenNotFristaendeUser() {
        final var sithsUser = createWebCertSithsUser(2, 1, 1);
        sithsUser.setOrigin(UserOriginType.DJUPINTEGRATION.name());

        setFeaturesHelperMockToReturn(true, true);
        setRestServiceMockToReturn(2);

        subscriptionService.checkSubscriptions(sithsUser);

        assertEquals(SubscriptionAction.NONE, sithsUser.getSubscriptionInfo().getSubscriptionAction());
        assertTrue(sithsUser.getSubscriptionInfo().getCareProvidersMissingSubscription().isEmpty());
    }

    @Test
    public void shouldSetSubscriptionStartDatesWhenSithsUser() {
        final var sithsUser = createWebCertSithsUser(1, 1, 1);
        sithsUser.setOrigin(UserOriginType.DJUPINTEGRATION.name());

        subscriptionService.checkSubscriptions(sithsUser);

        assertEquals(ADAPTATION_START_DATE, sithsUser.getSubscriptionInfo().getSubscriptionAdaptationStartDate());
        assertEquals(REQUIRE_START_DATE, sithsUser.getSubscriptionInfo().getRequireSubscriptionStartDate());
    }

    @Test
    public void shouldNotCallRestServiceWhenNoOrganizationsForSithsUser() {
        final var sithsUser = createWebCertSithsUser(1, 1, 1);
        sithsUser.getVardgivare().get(0).getVardenheter().get(0).setVardgivareOrgnr(null);

        setFeaturesHelperMockToReturn(true, true);

        subscriptionService.checkSubscriptions(sithsUser);

        verifyNoInteractions(subscriptionRestService);
    }

    @Test
    public void shouldSetSubscriptionActionNoneAndEmptyMissingSubscriptionListWhenNotFristaende() {
        final var webCertUser = createWebCertSithsUser( 1, 1, 0);
        webCertUser.setOrigin(UserOriginType.DJUPINTEGRATION.name());

        subscriptionService.checkSubscriptions(webCertUser);

        assertEquals(SubscriptionAction.NONE, webCertUser.getSubscriptionInfo().getSubscriptionAction());
        assertTrue(webCertUser.getSubscriptionInfo().getCareProvidersMissingSubscription().isEmpty());
    }

    @Test
    public void shouldSetSubscriptionActionNoneAndEmptyMissingSubscriptionListWhenNoSubscriptionFeaturesActive() {
        final var webCertUser = createWebCertSithsUser(1, 1, 0);

        subscriptionService.checkSubscriptions(webCertUser);

        assertEquals(SubscriptionAction.NONE, webCertUser.getSubscriptionInfo().getSubscriptionAction());
        assertTrue(webCertUser.getSubscriptionInfo().getCareProvidersMissingSubscription().isEmpty());
    }

    @Test
    public void shouldSetSubscriptionActionWarnWhenFeatureSubscriptionAdaptation() {
        final var webCertUser = createWebCertSithsUser(1, 1, 0);

        setRestServiceMockToReturn(1);
        setFeaturesHelperMockToReturn(true, false);

        subscriptionService.checkSubscriptions(webCertUser);

        assertEquals(SubscriptionAction.WARN, webCertUser.getSubscriptionInfo().getSubscriptionAction());
        assertEquals(1, webCertUser.getSubscriptionInfo().getCareProvidersMissingSubscription().size());
    }

    @Test
    public void shouldSetSubscriptionActionBlockWhenFeatureSubscriptionRequired() {
        final var webCertUser = createWebCertSithsUser(2, 1, 0);

        setRestServiceMockToReturn(1);
        setFeaturesHelperMockToReturn(true, true);

        subscriptionService.checkSubscriptions(webCertUser);

        assertEquals(SubscriptionAction.BLOCK, webCertUser.getSubscriptionInfo().getSubscriptionAction());
        assertEquals(1, webCertUser.getSubscriptionInfo().getCareProvidersMissingSubscription().size());
        assertTrue(webCertUser.getSubscriptionInfo().getCareProvidersMissingSubscription().contains("CARE_PROVIDER_HSA_ID_1"));
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
    public void shouldMonitorLogLoginAttemptsMissingSubscriptionWhenSubscriptionRequired() {
        final var webcertUser = createWebCertSithsUser(3, 1,2);

        setFeaturesHelperMockToReturn(false, true);
        setRestServiceMockToReturn(2);

        subscriptionService.checkSubscriptions(webcertUser);

        verify(monitoringLogService).logLoginAttemptMissingSubscription("only-for-test-use", "SITHS",
            List.of("CARE_PROVIDER_HSA_ID_1", "CARE_PROVIDER_HSA_ID_2").toString());
    }

    @Test
    public void shouldMonitorlogWhenRestClientExceptionForSithsUser() {
        final var sithsUser = createWebCertSithsUser(2, 2, 1);

        setMockToReturnRestClientException();
        setFeaturesHelperMockToReturn(false, true);

        subscriptionService.checkSubscriptions(sithsUser);

        verify(subscriptionRestService).getMissingSubscriptions(restServiceParamCaptor.capture(), any(AuthenticationMethodEnum.class));
        verify(monitoringLogService).logSubscriptionServiceCallFailure(restServiceParamCaptor.getValue().values(), "MESSAGE_TEXT");
    }

    @Test
    public void shouldMonitorlogWhenRestClientResponseExceptionSubtypeForSithsUser() {
        final var sithsUser = createWebCertSithsUser(1, 2, 2);

        setMockToReturnRestClientResponseException(403);
        setFeaturesHelperMockToReturn(false, true);

        subscriptionService.checkSubscriptions(sithsUser);

        verify(subscriptionRestService).getMissingSubscriptions(restServiceParamCaptor.capture(), any(AuthenticationMethodEnum.class));
        verify(monitoringLogService).logSubscriptionServiceCallFailure(restServiceParamCaptor.getValue().values(), "MESSAGE_TEXT");
    }

    @Test
    public void shouldMonitorLogSubscriptionWarningForSithsUserWhenSubscriptionAdaptation() {
        final var sithsUser = createWebCertSithsUser(3, 2, 0);
        final var expectedHsaIds = List.of("CARE_PROVIDER_HSA_ID_1", "CARE_PROVIDER_HSA_ID_2").toString();
        final var expectedUserId = sithsUser.getHsaId();

        setRestServiceMockToReturn(2);
        setFeaturesHelperMockToReturn(true, false);

        subscriptionService.checkSubscriptions(sithsUser);

        verify(monitoringLogService).logSubscriptionWarnings(expectedUserId, AuthenticationMethodEnum.SITHS.name(), expectedHsaIds);
    }

    @Test
    public void shouldSetSubscriptionStartDatesWhenElegUser() {
        final var elegUser = createWebCertElegUser();

        subscriptionService.checkSubscriptionElegWebCertUser(elegUser);

        assertEquals(ADAPTATION_START_DATE, elegUser.getSubscriptionInfo().getSubscriptionAdaptationStartDate());
        assertEquals(REQUIRE_START_DATE, elegUser.getSubscriptionInfo().getRequireSubscriptionStartDate());
    }

    @Test
    public void shouldNotCallRestServiceWhenNoOrganizationsForElegUser() {
        final var elegUser = createWebCertElegUser();
        elegUser.setPersonId(null);

        setFeaturesHelperMockToReturn(true, true);

        subscriptionService.checkSubscriptionElegWebCertUser(elegUser);

        verifyNoInteractions(subscriptionRestService);
    }

    @Test
    public void shouldUsePersonalIdAsOrganizationNumberWhenElegUser() {
        final var webCertUser = createWebCertElegUser();
        final var personId = webCertUser.getPersonId();
        final var expectedOrganizationNumber = personId.substring(2, 8) + "-" + personId.substring(personId.length() - 4);

        setRestServiceMockToReturn(0);
        setFeaturesHelperMockToReturn(true, false);

        subscriptionService.checkSubscriptionElegWebCertUser(webCertUser);

        verify(subscriptionRestService).getMissingSubscriptions(restServiceParamCaptor.capture(), any(AuthenticationMethodEnum.class));
        assertTrue(restServiceParamCaptor.getValue().containsKey(expectedOrganizationNumber));
        assertEquals("CARE_PROVIDER_HSA_ID_1", restServiceParamCaptor.getValue().get(expectedOrganizationNumber));
    }

    @Test
    public void shouldReturnTrueIfElegUserHasSubscriptionWhenSubscriptionRequired() {
        final var elegUser = createWebCertElegUser();

        setRestServiceMockToReturn(0);
        setFeaturesHelperMockToReturn(false, true);

        final var hasSubscription = subscriptionService.checkSubscriptionElegWebCertUser(elegUser);

        assertTrue(hasSubscription);
    }

    @Test
    public void shouldReturnTrueIfElegUserHasSubscriptionWhenSubscriptionAdaptation() {
        final var elegUser = createWebCertElegUser();

        setRestServiceMockToReturn(0);
        setFeaturesHelperMockToReturn(true, false);

        final var hasSubscription = subscriptionService.checkSubscriptionElegWebCertUser(elegUser);

        assertTrue(hasSubscription);
    }

    @Test
    public void shouldReturnFalseIfElegUserIsMissingSubscriptionWhenSubscriptionAdaptation() {
        final var elegUser = createWebCertElegUser();

        setRestServiceMockToReturn(1);
        setFeaturesHelperMockToReturn(true, false);

        final var hasSubscription = subscriptionService.checkSubscriptionElegWebCertUser(elegUser);

        assertFalse(hasSubscription);
    }

    @Test
    public void shouldReturnFalseIfElegUserIsMissingSubscriptionWhenSubscriptionRequired() {
        final var elegUser = createWebCertElegUser();

        setRestServiceMockToReturn(1);
        setFeaturesHelperMockToReturn(true, true);

        final var hasSubscription = subscriptionService.checkSubscriptionElegWebCertUser(elegUser);

        assertFalse(hasSubscription);
    }

    @Test
    public void shouldMonitorlogWhenRestClientExceptionForElegUser() {
        final var elegUser = createWebCertElegUser();

        setMockToReturnRestClientException();
        setFeaturesHelperMockToReturn(false, true);

        subscriptionService.checkSubscriptionElegWebCertUser(elegUser);

        verify(subscriptionRestService).getMissingSubscriptions(restServiceParamCaptor.capture(), any(AuthenticationMethodEnum.class));
        verify(monitoringLogService).logSubscriptionServiceCallFailure(restServiceParamCaptor.getValue().values(), "MESSAGE_TEXT");
    }

    @Test
    public void shouldMonitorlogWhenRestClientResponseExceptionSubtypeForElegUser() {
        final var elegUser = createWebCertElegUser();

        setMockToReturnRestClientResponseException(503);
        setFeaturesHelperMockToReturn(false, true);

        subscriptionService.checkSubscriptionElegWebCertUser(elegUser);

        verify(subscriptionRestService).getMissingSubscriptions(restServiceParamCaptor.capture(), any(AuthenticationMethodEnum.class));
        verify(monitoringLogService).logSubscriptionServiceCallFailure(restServiceParamCaptor.getValue().values(), "MESSAGE_TEXT");
    }

    @Test
    public void shouldMonitorLogSubscriptionWarningForElegUserWhenSubscriptionAdaptation() {
        final var elegUser = createWebCertElegUser();
        final var expectedHsaIds = List.of(elegUser.getVardgivare().get(0).getId()).toString();
        final var expectedUserId = elegUser.getHsaId();

        setRestServiceMockToReturn(1);
        setFeaturesHelperMockToReturn(true, false);

        subscriptionService.checkSubscriptionElegWebCertUser(elegUser);

        verify(monitoringLogService).logSubscriptionWarnings(expectedUserId, AuthenticationMethodEnum.ELEG.name(), expectedHsaIds);
    }

    @Test
    public void shouldNotCallRestServiceWhenNoOrganizationsForUnregisteredElegUser() {
        setFeaturesHelperMockToReturn(true, true);

        final var response = subscriptionService.isUnregisteredElegUserMissingSubscription(null);

        verifyNoInteractions(subscriptionRestService);
        assertTrue(response);
    }

    @Test
    public void shouldUsePersonalIdWhenQueryForUnregisteredElegUser() {
        final var restServiceParamCaptor = ArgumentCaptor.forClass(String.class);

        subscriptionService.isUnregisteredElegUserMissingSubscription(PERSON_ID);

        verify(subscriptionRestService).isMissingSubscriptionUnregisteredElegUser(restServiceParamCaptor.capture());
        assertEquals("121212-1212", restServiceParamCaptor.getValue());
    }

    @Test
    public void shouldReturnValueReceivedFromRestServiceForUnregisteredElegUser() {

        setRestServiceUnregisteredElegMockToReturn(true);
        final var boolean1 = subscriptionService.isUnregisteredElegUserMissingSubscription(PERSON_ID);
        setRestServiceUnregisteredElegMockToReturn(false);
        final var boolean2 = subscriptionService.isUnregisteredElegUserMissingSubscription(PERSON_ID);

        assertTrue(boolean1);
        assertFalse(boolean2);
    }

    @Test
    public void shouldReturnFalseWhenExceptionReceivedFromRestServiceForUnregistered() {
        setMockToReturnRestClientExceptionForUnregistered();

        final var response = subscriptionService.isUnregisteredElegUserMissingSubscription(PERSON_ID);

        assertFalse(response);
    }

    @Test
    public void shouldMonitorLogLoginAttemptsMissingSubscriptionWhenSubscriptionRequiredForUnregElegUser() {
        final var expectedUserId = HashUtility.hash(PERSON_ID);
        final var expectedOrg = HashUtility.hash("121212-1212");

        setFeaturesHelperMockToReturn(false, true);
        setRestServiceUnregisteredElegMockToReturn(true);

        subscriptionService.isUnregisteredElegUserMissingSubscription(PERSON_ID);

        verify(monitoringLogService).logLoginAttemptMissingSubscription(expectedUserId, "ELEG", expectedOrg);
    }

    @Test
    public void shouldMonitorlogWhenRestClientExceptionForUnregisteredElegUser() {
        final var hashedOrgNo = Collections.singleton(HashUtility.hash("121212-1212"));
        setMockToReturnRestClientExceptionForUnregistered();

        subscriptionService.isUnregisteredElegUserMissingSubscription("191212121212");

        verify(monitoringLogService).logSubscriptionServiceCallFailure(hashedOrgNo, "MESSAGE_TEXT");
    }

    @Test
    public void shouldMonitorlogWhenRestClientResponseExceptionSubtypeForUnregisteredElegUser() {
        final var hashedOrgNo = Collections.singleton(HashUtility.hash("121212-1212"));

        setMockToReturnRestClientResponseExceptionForUnregistered();

        subscriptionService.isUnregisteredElegUserMissingSubscription("191212121212");

        verify(monitoringLogService).logSubscriptionServiceCallFailure(hashedOrgNo, "MESSAGE_TEXT");
    }

    @Test
    public void shouldMonitorLogSubscriptionWarningForUnregisteredElegUserWhenSubscriptionAdaptation() {
        final var expectedOrgNo = HashUtility.hash("121212-1212");
        final var expectedPersonId = HashUtility.hash(PERSON_ID);

        setRestServiceUnregisteredElegMockToReturn(true);
        setFeaturesHelperMockToReturn(true, false);

        subscriptionService.isUnregisteredElegUserMissingSubscription(PERSON_ID);

        verify(monitoringLogService).logSubscriptionWarnings(expectedPersonId, AuthenticationMethodEnum.ELEG.name(), expectedOrgNo);
    }

    @Test
    public void shouldRemoveCareProviderFromMissingSubscriptionListWhenAcknowledgedWarningElegUser() {
        final var careProviderIndex = 0;
        final var webCertUser = createWebCertElegUser();
        setSelectedCareProviderWithWarning(webCertUser, careProviderIndex);

        assertEquals(SubscriptionAction.WARN, webCertUser.getSubscriptionInfo().getSubscriptionAction());
        assertEquals(1, webCertUser.getSubscriptionInfo().getCareProvidersMissingSubscription().size());
        assertTrue(webCertUser.getSubscriptionInfo().getCareProvidersMissingSubscription().contains(webCertUser.getVardgivare()
            .get(careProviderIndex).getId()));

        subscriptionService.acknowledgeSubscriptionWarning(webCertUser);

        assertEquals(SubscriptionAction.WARN, webCertUser.getSubscriptionInfo().getSubscriptionAction());
        assertTrue(webCertUser.getSubscriptionInfo().getCareProvidersMissingSubscription().isEmpty());
    }

    @Test
    public void shouldRemoveCareProviderFromMissingSubscriptionListWhenAcknowledgedWarningForSithsUserWithMultipleCareProviders() {
        final var careProviderIndex = 1;
        final var webCertUser = createWebCertSithsUser(3, 2, 1);
        setSelectedCareProviderWithWarning(webCertUser, careProviderIndex);
        webCertUser.getSubscriptionInfo().getCareProvidersMissingSubscription().add(webCertUser.getVardgivare().get(2).getId());

        assertEquals(SubscriptionAction.WARN, webCertUser.getSubscriptionInfo().getSubscriptionAction());
        assertEquals(2, webCertUser.getSubscriptionInfo().getCareProvidersMissingSubscription().size());
        assertTrue(webCertUser.getSubscriptionInfo().getCareProvidersMissingSubscription().contains(webCertUser.getVardgivare()
            .get(1).getId()));
        assertTrue(webCertUser.getSubscriptionInfo().getCareProvidersMissingSubscription().contains(webCertUser.getVardgivare()
            .get(2).getId()));

        subscriptionService.acknowledgeSubscriptionWarning(webCertUser);

        assertEquals(SubscriptionAction.WARN, webCertUser.getSubscriptionInfo().getSubscriptionAction());
        assertEquals(1, webCertUser.getSubscriptionInfo().getCareProvidersMissingSubscription().size());
        assertTrue(webCertUser.getSubscriptionInfo().getCareProvidersMissingSubscription().contains(webCertUser.getVardgivare()
            .get(2).getId()));
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

    private void setMockToReturnRestClientExceptionForUnregistered() {
        final var e = new RestClientException("MESSAGE_TEXT");
        when(subscriptionRestService.isMissingSubscriptionUnregisteredElegUser(any(String.class))).thenThrow(e);
    }

    private void setMockToReturnRestClientResponseExceptionForUnregistered() {
        final var e = new RestClientResponseException("MESSAGE_TEXT", 500, "statusText", null,null, null);
        when(subscriptionRestService.isMissingSubscriptionUnregisteredElegUser(any(String.class))).thenThrow(e);
    }

    private void setMockToReturnRestClientException() {
        final var e = new RestClientException("MESSAGE_TEXT");
        when(subscriptionRestService.getMissingSubscriptions(any(), any(AuthenticationMethodEnum.class))).thenThrow(e);
    }

    private void setMockToReturnRestClientResponseException(int httpStatusCode) {
        final var e = new RestClientResponseException("MESSAGE_TEXT", httpStatusCode, "statusText", null,null, null);
        when(subscriptionRestService.getMissingSubscriptions(any(), any(AuthenticationMethodEnum.class))).thenThrow(e);
    }

    private WebCertUser createWebCertElegUser() {
        final var webCertUser = createBaseWebCertUser();
        webCertUser.setAuthenticationScheme(ELEG_AUTHN_CLASSES.get(2));
        webCertUser.setOrigin(UserOriginType.NORMAL.name());
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
        final var subscriptionInfo = new SubscriptionInfo();
        subscriptionInfo.setSubscriptionAction(SubscriptionAction.WARN);
        final var careProviderList = new ArrayList<String>();
        careProviderList.add(webCertUser.getVardgivare().get(careProviderIndex).getId());
        subscriptionInfo.setCareProvidersMissingSubscription(careProviderList);
        webCertUser.setSubscriptionInfo(subscriptionInfo);
        webCertUser.setValdVardgivare(webCertUser.getVardgivare().get(careProviderIndex));
    }
}
