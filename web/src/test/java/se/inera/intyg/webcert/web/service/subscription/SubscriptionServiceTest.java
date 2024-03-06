/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static se.inera.intyg.infra.security.common.model.AuthoritiesConstants.FEATURE_SUBSCRIPTION_ADAPTATION_PERIOD;
import static se.inera.intyg.infra.security.common.model.AuthoritiesConstants.FEATURE_SUBSCRIPTION_REQUIRED;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.ELEG_AUTHN_CLASSES;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.SITHS_AUTHN_CLASSES;

import se.inera.intyg.webcert.integration.api.subscription.AuthenticationMethodEnum;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.subscription.dto.SubscriptionAction;
import se.inera.intyg.webcert.web.service.subscription.dto.SubscriptionInfo;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@ExtendWith(MockitoExtension.class)
public class SubscriptionServiceTest {

    @Mock
    private SubscriptionRestServiceImpl subscriptionRestService;

    @Mock
    private FeaturesHelper featuresHelper;

    @Mock
    private MonitoringLogService monitoringLogService;

    @Captor
    private ArgumentCaptor<Map<String, List<String>>> restServiceParamCaptor;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    private static final String PERSON_ID = "191212121212";
    private static final String ADAPTATION_START_DATE = "subscriptionAdaptationStartDate";
    private static final String REQUIRE_START_DATE = "requireSubscriptionStartDate";

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(subscriptionService, ADAPTATION_START_DATE, ADAPTATION_START_DATE);
        ReflectionTestUtils.setField(subscriptionService, REQUIRE_START_DATE, REQUIRE_START_DATE);
    }

    @Test
    public void shouldNotCallRestServiceWhenNotFristaendeUser() {
        final var sithsUser = createWebCertSithsUser(1, 1, 1);
        sithsUser.setOrigin(UserOriginType.DJUPINTEGRATION.name());

        subscriptionService.checkSubscriptions(sithsUser);

        verifyNoInteractions(subscriptionRestService);
    }

    @Test
    public void shouldAlwaysHaveSubscriptionActionNoneAndEmptyMissingSubscriptionListWhenNotFristaendeUser() {
        final var sithsUser = createWebCertSithsUser(2, 1, 1);
        sithsUser.setOrigin(UserOriginType.DJUPINTEGRATION.name());

        subscriptionService.checkSubscriptions(sithsUser);

        assertAll(
            () -> assertEquals(SubscriptionAction.NONE, sithsUser.getSubscriptionInfo().getSubscriptionAction()),
            () -> assertTrue(sithsUser.getSubscriptionInfo().getCareProvidersForSubscriptionModal().isEmpty()),
            () -> assertTrue(sithsUser.getSubscriptionInfo().getCareProvidersMissingSubscription().isEmpty())
        );
    }

    @Test
    public void shouldSetSubscriptionStartDatesWhenSithsUser() {
        final var sithsUser = createWebCertSithsUser(1, 1, 1);
        sithsUser.setOrigin(UserOriginType.DJUPINTEGRATION.name());

        subscriptionService.checkSubscriptions(sithsUser);

        assertAll(
            () -> assertEquals(ADAPTATION_START_DATE, sithsUser.getSubscriptionInfo().getSubscriptionAdaptationStartDate()),
            () -> assertEquals(REQUIRE_START_DATE, sithsUser.getSubscriptionInfo().getRequireSubscriptionStartDate())
        );
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
    public void shouldHaveExactImmutableCopyOfMissingSubscriptions() {
        final var webCertUser = createWebCertSithsUser(3, 3, 3);

        setRestServiceMockToReturn(3);
        setFeaturesHelperMockToReturn(false, true);
        subscriptionService.checkSubscriptions(webCertUser);

        assertAll(
            () -> assertIterableEquals(webCertUser.getSubscriptionInfo().getCareProvidersForSubscriptionModal(),
                webCertUser.getSubscriptionInfo().getCareProvidersMissingSubscription()),
            () -> assertThrows(UnsupportedOperationException.class, () -> webCertUser.getSubscriptionInfo()
                .getCareProvidersMissingSubscription().remove(1))
        );
    }

    @Test
    public void shouldSetSubscriptionActionNoneAndEmptyMissingSubscriptionListWhenNoSubscriptionFeaturesActive() {
        final var webCertUser = createWebCertSithsUser(1, 1, 0);

        setFeaturesHelperMockToReturn(false, false);

        subscriptionService.checkSubscriptions(webCertUser);

        assertAll(
            () -> assertEquals(SubscriptionAction.NONE, webCertUser.getSubscriptionInfo().getSubscriptionAction()),
            () -> assertTrue(webCertUser.getSubscriptionInfo().getCareProvidersMissingSubscription().isEmpty())
        );
    }

    @Test
    public void shouldSetSubscriptionActionWarnWhenFeatureSubscriptionAdaptation() {
        final var webCertUser = createWebCertSithsUser(1, 1, 0);

        setRestServiceMockToReturn(1);
        setFeaturesHelperMockToReturn(true, false);

        subscriptionService.checkSubscriptions(webCertUser);

        assertAll(
            () -> assertEquals(SubscriptionAction.WARN, webCertUser.getSubscriptionInfo().getSubscriptionAction()),
            () -> assertEquals(1, webCertUser.getSubscriptionInfo().getCareProvidersMissingSubscription().size())
        );
    }

    @Test
    public void shouldSetSubscriptionActionBlockWhenFeatureSubscriptionRequired() {
        final var webCertUser = createWebCertSithsUser(2, 1, 0);

        setRestServiceMockToReturn(1);
        setFeaturesHelperMockToReturn(true, true);

        subscriptionService.checkSubscriptions(webCertUser);

        assertAll(
            () -> assertEquals(SubscriptionAction.BLOCK, webCertUser.getSubscriptionInfo().getSubscriptionAction()),
            () -> assertEquals(1, webCertUser.getSubscriptionInfo().getCareProvidersMissingSubscription().size()),
            () -> assertTrue(webCertUser.getSubscriptionInfo().getCareProvidersMissingSubscription().contains("CARE_PROVIDER_HSA_ID_1"))
        );
    }

    @Test
    public void shouldUseCareProviderOrganizationNumberWhenSithsUser() {
        final var webCertUser = createWebCertSithsUser(1, 1, 1);
        final var expectedOrganizationNumber = "CARE_PROVIDER_ORGANIZATION_NO_1";

        setRestServiceMockToReturn(0);
        setFeaturesHelperMockToReturn(true, false);

        subscriptionService.checkSubscriptions(webCertUser);

        verify(subscriptionRestService).getMissingSubscriptions(restServiceParamCaptor.capture(), any(AuthenticationMethodEnum.class));

        assertAll(
            () -> assertTrue(restServiceParamCaptor.getValue().containsKey(expectedOrganizationNumber)),
            () -> assertEquals("CARE_PROVIDER_HSA_ID_1", restServiceParamCaptor.getValue().get(expectedOrganizationNumber).get(0))
        );
    }

    @Test
    public void shouldNotThrowExceptionIfSithsUserIsMissingSingleSubscriptionWhenSubscriptionRequired() {
        final var webCertUser = createWebCertSithsUser(1, 1, 0);

        setRestServiceMockToReturn(1);
        setFeaturesHelperMockToReturn(false, true);

        assertDoesNotThrow(() -> subscriptionService.checkSubscriptions(webCertUser));
    }

    @Test
    public void shouldNotThrowExceptionIfSithsUserIsMissingAllSubscriptionsWhenSubscriptionRequired() {
        final var webCertUser = createWebCertSithsUser(3, 1, 1);

        setRestServiceMockToReturn(3);
        setFeaturesHelperMockToReturn(false, true);

        subscriptionService.checkSubscriptions(webCertUser);

        assertDoesNotThrow(() -> subscriptionService.checkSubscriptions(webCertUser));
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
        final var webcertUser = createWebCertSithsUser(3, 1, 2);

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

        final var param = restServiceParamCaptor.getValue().values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        verify(monitoringLogService).logSubscriptionServiceCallFailure(param, "MESSAGE_TEXT");
    }

    @Test
    public void shouldMonitorlogWhenRestClientResponseExceptionSubtypeForSithsUser() {
        final var sithsUser = createWebCertSithsUser(1, 2, 2);

        setMockToReturnRestClientResponseException(403);
        setFeaturesHelperMockToReturn(false, true);

        subscriptionService.checkSubscriptions(sithsUser);

        verify(subscriptionRestService).getMissingSubscriptions(restServiceParamCaptor.capture(), any(AuthenticationMethodEnum.class));

        final var param = restServiceParamCaptor.getValue().values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        verify(monitoringLogService).logSubscriptionServiceCallFailure(param, "MESSAGE_TEXT");
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
    public void shouldHandleMultipleCareProvidersWithSameOrgNumberForSithsUser() {
        final var sithsUser = createWebCertSithsUser(4, 2, 0);
        sithsUser.getVardgivare().get(1).getVardenheter().get(0).setVardgivareOrgnr("CARE_PROVIDER_ORGANIZATION_NO_1");
        sithsUser.getVardgivare().get(2).getVardenheter().get(0).setVardgivareOrgnr("CARE_PROVIDER_ORGANIZATION_NO_1");

        setRestServiceMockToReturn(3);
        setFeaturesHelperMockToReturn(true, false);

        subscriptionService.checkSubscriptions(sithsUser);

        verify(subscriptionRestService).getMissingSubscriptions(restServiceParamCaptor.capture(), any(AuthenticationMethodEnum.class));
        assertAll(
            () -> assertEquals(3, restServiceParamCaptor.getValue().get("CARE_PROVIDER_ORGANIZATION_NO_1").size()),
            () -> assertEquals(1, restServiceParamCaptor.getValue().get("CARE_PROVIDER_ORGANIZATION_NO_4").size())
        );
    }

    @Test
    public void shouldSetSubscriptionStartDatesWhenElegUser() {
        final var elegUser = createWebCertElegUser();

        subscriptionService.checkSubscriptions(elegUser);

        assertAll(
            () -> assertEquals(ADAPTATION_START_DATE, elegUser.getSubscriptionInfo().getSubscriptionAdaptationStartDate()),
            () -> assertEquals(REQUIRE_START_DATE, elegUser.getSubscriptionInfo().getRequireSubscriptionStartDate())
        );
    }

    @Test
    public void shouldNotCallRestServiceWhenNoOrganizationsForElegUser() {
        final var elegUser = createWebCertElegUser();
        elegUser.setPersonId(null);

        setFeaturesHelperMockToReturn(true, true);

        subscriptionService.checkSubscriptions(elegUser);

        verifyNoInteractions(subscriptionRestService);
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
        assertAll(
            () -> assertTrue(restServiceParamCaptor.getValue().containsKey(expectedOrganizationNumber)),
            () -> assertEquals("CARE_PROVIDER_HSA_ID_1", restServiceParamCaptor.getValue().get(expectedOrganizationNumber).get(0))
        );
    }

    @Test
    public void shouldReturnTrueIfElegUserHasSubscriptionWhenSubscriptionRequired() {
        final var elegUser = createWebCertElegUser();

        setRestServiceMockToReturn(0);
        setFeaturesHelperMockToReturn(false, true);

        final var hasSubscription = subscriptionService.checkSubscriptions(elegUser);

        assertTrue(hasSubscription);
    }

    @Test
    public void shouldReturnTrueIfElegUserHasSubscriptionWhenSubscriptionAdaptation() {
        final var elegUser = createWebCertElegUser();

        setRestServiceMockToReturn(0);
        setFeaturesHelperMockToReturn(true, false);

        final var hasSubscription = subscriptionService.checkSubscriptions(elegUser);

        assertTrue(hasSubscription);
    }

    @Test
    public void shouldReturnFalseIfElegUserIsMissingSubscriptionWhenSubscriptionAdaptation() {
        final var elegUser = createWebCertElegUser();

        setRestServiceMockToReturn(1);
        setFeaturesHelperMockToReturn(true, false);

        final var hasSubscription = subscriptionService.checkSubscriptions(elegUser);

        assertFalse(hasSubscription);
    }

    @Test
    public void shouldReturnFalseIfElegUserIsMissingSubscriptionWhenSubscriptionRequired() {
        final var elegUser = createWebCertElegUser();

        setRestServiceMockToReturn(1);
        setFeaturesHelperMockToReturn(true, true);

        final var hasSubscription = subscriptionService.checkSubscriptions(elegUser);

        assertFalse(hasSubscription);
    }

    @Test
    public void shouldMonitorlogWhenRestClientExceptionForElegUser() {
        final var elegUser = createWebCertElegUser();

        setMockToReturnRestClientException();
        setFeaturesHelperMockToReturn(false, true);

        subscriptionService.checkSubscriptions(elegUser);

        verify(subscriptionRestService).getMissingSubscriptions(restServiceParamCaptor.capture(), any(AuthenticationMethodEnum.class));

        final var param = restServiceParamCaptor.getValue().values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        verify(monitoringLogService).logSubscriptionServiceCallFailure(param, "MESSAGE_TEXT");
    }

    @Test
    public void shouldMonitorlogWhenRestClientResponseExceptionSubtypeForElegUser() {
        final var elegUser = createWebCertElegUser();

        setMockToReturnRestClientResponseException(503);
        setFeaturesHelperMockToReturn(false, true);

        subscriptionService.checkSubscriptions(elegUser);

        verify(subscriptionRestService).getMissingSubscriptions(restServiceParamCaptor.capture(), any(AuthenticationMethodEnum.class));

        final var param = restServiceParamCaptor.getValue().values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        verify(monitoringLogService).logSubscriptionServiceCallFailure(param, "MESSAGE_TEXT");
    }

    @Test
    public void shouldMonitorLogSubscriptionWarningForElegUserWhenSubscriptionAdaptation() {
        final var elegUser = createWebCertElegUser();
        final var expectedHsaIds = List.of(elegUser.getVardgivare().get(0).getId()).toString();
        final var expectedUserId = elegUser.getHsaId();

        setRestServiceMockToReturn(1);
        setFeaturesHelperMockToReturn(true, false);

        subscriptionService.checkSubscriptions(elegUser);

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

        assertAll(
            () -> assertTrue(boolean1),
            () -> assertFalse(boolean2)
        );
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
        final var hashedOrgNo = List.of(HashUtility.hash("121212-1212"));
        setMockToReturnRestClientExceptionForUnregistered();

        subscriptionService.isUnregisteredElegUserMissingSubscription("191212121212");

        verify(monitoringLogService).logSubscriptionServiceCallFailure(hashedOrgNo, "MESSAGE_TEXT");
    }

    @Test
    public void shouldMonitorlogWhenRestClientResponseExceptionSubtypeForUnregisteredElegUser() {
        final var hashedOrgNo = List.of(HashUtility.hash("121212-1212"));

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
    public void shouldRemoveCareProviderFromMissingSubscriptionListWhenAcknowledgedModalElegUser() {
        final var careProviderIndex = 0;
        final var webCertUser = createWebCertElegUser();
        setSelectedCareProviderForSubscriptionModalDisplay(webCertUser, careProviderIndex);

        assertAll(
            () -> assertEquals(SubscriptionAction.BLOCK, webCertUser.getSubscriptionInfo().getSubscriptionAction()),
            () -> assertEquals(1, webCertUser.getSubscriptionInfo().getCareProvidersForSubscriptionModal().size()),
            () -> assertTrue(webCertUser.getSubscriptionInfo().getCareProvidersForSubscriptionModal().contains(webCertUser.getVardgivare()
                .get(careProviderIndex).getId()))
        );

        subscriptionService.acknowledgeSubscriptionModal(webCertUser);

        assertAll(
            () -> assertEquals(SubscriptionAction.BLOCK, webCertUser.getSubscriptionInfo().getSubscriptionAction()),
            () -> assertTrue(webCertUser.getSubscriptionInfo().getCareProvidersForSubscriptionModal().isEmpty())
        );
    }

    @Test
    public void shouldRemoveCareProviderFromMissingSubscriptionListWhenAcknowledgedModalForSithsUserWithMultipleCareProviders() {
        final var careProviderIndex = 1;
        final var webCertUser = createWebCertSithsUser(3, 2, 1);
        setSelectedCareProviderForSubscriptionModalDisplay(webCertUser, careProviderIndex);
        webCertUser.getSubscriptionInfo().getCareProvidersForSubscriptionModal().add(webCertUser.getVardgivare().get(2).getId());

        assertAll(
            () -> assertEquals(SubscriptionAction.BLOCK, webCertUser.getSubscriptionInfo().getSubscriptionAction()),
            () -> assertEquals(2, webCertUser.getSubscriptionInfo().getCareProvidersForSubscriptionModal().size()),
            () -> assertTrue(webCertUser.getSubscriptionInfo().getCareProvidersForSubscriptionModal().contains(webCertUser.getVardgivare()
                .get(1).getId())),
            () -> assertTrue(webCertUser.getSubscriptionInfo().getCareProvidersForSubscriptionModal().contains(webCertUser.getVardgivare()
                .get(2).getId()))
        );

        subscriptionService.acknowledgeSubscriptionModal(webCertUser);

        assertAll(
            () -> assertEquals(SubscriptionAction.BLOCK, webCertUser.getSubscriptionInfo().getSubscriptionAction()),
            () -> assertEquals(1, webCertUser.getSubscriptionInfo().getCareProvidersForSubscriptionModal().size()),
            () -> assertTrue(webCertUser.getSubscriptionInfo().getCareProvidersForSubscriptionModal().contains(webCertUser.getVardgivare()
                .get(2).getId()))
        );
    }

    @Test
    public void shouldReturnTrueOnlyWhenSubscriptionAdaptationIsSinglyActivated() {
        setFeaturesHelperMockToReturn(true, false);
        final var boolean1 = subscriptionService.isSubscriptionAdaptation();

        setFeaturesHelperMockToReturn(true, true);
        final var boolean2 = subscriptionService.isSubscriptionAdaptation();

        setFeaturesHelperMockToReturn(false, false);
        final var boolean3 = subscriptionService.isSubscriptionAdaptation();

        assertAll(
            () -> assertTrue(boolean1),
            () -> assertFalse(boolean2),
            () -> assertFalse(boolean3)
        );
    }

    @Test
    public void shouldReturnTrueWheneverSubscriptionRequiredIsActivated() {
        setFeaturesHelperMockToReturn(true, true);
        final var boolean1 = subscriptionService.isSubscriptionRequired();

        setFeaturesHelperMockToReturn(false, true);
        final var boolean2 = subscriptionService.isSubscriptionRequired();

        setFeaturesHelperMockToReturn(false, false);
        final var boolean3 = subscriptionService.isSubscriptionRequired();

        assertAll(
            () -> assertTrue(boolean1),
            () -> assertTrue(boolean2),
            () -> assertFalse(boolean3)
        );
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

        assertAll(
            () -> assertTrue(boolean1),
            () -> assertTrue(boolean2),
            () -> assertTrue(boolean3),
            () -> assertFalse(boolean4)
        );
    }

    private void setRestServiceMockToReturn(int careProviderHsaIdCount) {
        final var careProviderHsaIds = getMissingSubscriptionList(careProviderHsaIdCount);
        when(subscriptionRestService.getMissingSubscriptions(any(), any())).thenReturn(careProviderHsaIds);
    }

    private List<String> getMissingSubscriptionList(int numberOfMissingSubscriptions) {
        final var careProviderHsaIds = new ArrayList<String>();

        for (var i = 1; i <= numberOfMissingSubscriptions; i++) {
            careProviderHsaIds.add("CARE_PROVIDER_HSA_ID_" + i);
        }
        return careProviderHsaIds;
    }

    private void setRestServiceUnregisteredElegMockToReturn(Boolean isMissingSubscription) {
        when(subscriptionRestService.isMissingSubscriptionUnregisteredElegUser(any(String.class))).thenReturn(isMissingSubscription);
    }

    private void setFeaturesHelperMockToReturn(boolean subscriptionAdaptation, boolean subscriptionRequired) {
        lenient().when(featuresHelper.isFeatureActive(FEATURE_SUBSCRIPTION_REQUIRED)).thenReturn(subscriptionRequired);
        lenient().when(featuresHelper.isFeatureActive(FEATURE_SUBSCRIPTION_ADAPTATION_PERIOD)).thenReturn(subscriptionAdaptation);
    }

    private void setMockToReturnRestClientExceptionForUnregistered() {
        final var e = new RestClientException("MESSAGE_TEXT");
        when(subscriptionRestService.isMissingSubscriptionUnregisteredElegUser(any(String.class))).thenThrow(e);
    }

    private void setMockToReturnRestClientResponseExceptionForUnregistered() {
        final var e = new RestClientResponseException("MESSAGE_TEXT", 500, "statusText", null, null, null);
        when(subscriptionRestService.isMissingSubscriptionUnregisteredElegUser(any(String.class))).thenThrow(e);
    }

    private void setMockToReturnRestClientException() {
        final var e = new RestClientException("MESSAGE_TEXT");
        when(subscriptionRestService.getMissingSubscriptions(any(), any(AuthenticationMethodEnum.class))).thenThrow(e);
    }

    private void setMockToReturnRestClientResponseException(int httpStatusCode) {
        final var e = new RestClientResponseException("MESSAGE_TEXT", httpStatusCode, "statusText", null, null, null);
        when(subscriptionRestService.getMissingSubscriptions(any(), any(AuthenticationMethodEnum.class))).thenThrow(e);
    }

    private WebCertUser createWebCertElegUser() {
        final var webCertUser = createBaseWebCertUser();
        webCertUser.setAuthenticationScheme(ELEG_AUTHN_CLASSES.get(2));
        webCertUser.setVardgivare(getCareProviders(1, 1, 0));
        webCertUser.setRoles(Map.of(AuthoritiesConstants.ROLE_PRIVATLAKARE, new Role()));

        final var careProvider = webCertUser.getVardgivare().get(0);
        careProvider.getVardenheter().get(0).setId(careProvider.getId());
        return webCertUser;
    }

    private WebCertUser createWebCertSithsUser(int numCareProviders, int numCareUnits, int numUnits) {
        final var webCertUser = createBaseWebCertUser();
        webCertUser.setAuthenticationScheme(SITHS_AUTHN_CLASSES.get(1));
        webCertUser.setVardgivare(getCareProviders(numCareProviders, numCareUnits, numUnits));
        webCertUser.setRoles(Map.of(AuthoritiesConstants.ROLE_LAKARE, new Role()));
        return webCertUser;
    }

    private WebCertUser createBaseWebCertUser() {
        final var webCertUser = new WebCertUser();
        webCertUser.setPersonId(PERSON_ID);
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

    private void setSelectedCareProviderForSubscriptionModalDisplay(WebCertUser webCertUser, int careProviderIndex) {
        final var subscriptionInfo = new SubscriptionInfo();
        subscriptionInfo.setSubscriptionAction(SubscriptionAction.BLOCK);
        final var careProviderList = new ArrayList<String>();
        careProviderList.add(webCertUser.getVardgivare().get(careProviderIndex).getId());
        subscriptionInfo.setCareProvidersForSubscriptionModal(careProviderList);
        webCertUser.setSubscriptionInfo(subscriptionInfo);
        webCertUser.setValdVardgivare(webCertUser.getVardgivare().get(careProviderIndex));
    }
}
