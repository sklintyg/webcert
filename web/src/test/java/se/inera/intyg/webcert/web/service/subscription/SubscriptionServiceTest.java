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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.ELEG_AUTHN_CLASSES;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.SITHS_AUTHN_CLASSES;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Mottagning;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Feature;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.webcert.web.auth.exceptions.MissingSubscriptionException;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.integration.dto.SubscriptionAction;
import se.inera.intyg.webcert.web.web.controller.integration.dto.SubscriptionInfo;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = SubscriptionServiceImpl.class)
@TestPropertySource(locations = "classpath:subscription/subscription-test.properties")
public class SubscriptionServiceTest {

    @Value("${kundportalen.access.token}")
    private String kundportalenAccessToken;

    @Value("${kundportalen.subscriptions.url}")
    private String kundportalenSubscriptionServiceUrl;

    @Value("#{${kundportalen.service.codes.eleg}}")
    private List<String> elegServiceCodes;

    @Value("#{${kundportalen.service.codes.siths}}")
    private List<String> sithsServiceCodes;

    @Value("${subscription.block.start.date}")
    private String subscriptionBlockStartDate;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    @Autowired
    private SubscriptionServiceImpl subscriptionService;

    private static final ParameterizedTypeReference<Map<String, Boolean>> MAP_STRING_BOOLEAN_TYPE = new ParameterizedTypeReference<>() { };
    private static final String PERSON_ID = "191212121212";

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldReturnSubscriptionActionNoneWhenNotFristaende() {
        final var webCertUser = createWebCertSithsUser(true, false, 1, 1, 0);
        webCertUser.setOrigin(UserOriginType.DJUPINTEGRATION.name());

        final var response = subscriptionService.fetchSubscriptionInfo(webCertUser);

        assertEquals(SubscriptionAction.NONE, response.getSubscriptionAction());
    }

    @Test
    public void shouldReturnSubscriptionActionNoneWhenBeforeAdjustment() {
        final var webCertUser = createWebCertSithsUser(false, false, 1, 1, 0);

        final var response = subscriptionService.fetchSubscriptionInfo(webCertUser);

        assertEquals(SubscriptionAction.NONE, response.getSubscriptionAction());
    }

    @Test
    public void shouldReturnSubscriptionActionWarnWhenDuringAdjustment() {
        final var webCertUser = createWebCertElegUser(true, false);

        setMockToReturnValue();

        final var response = subscriptionService.fetchSubscriptionInfo(webCertUser);

        assertEquals(SubscriptionAction.MISSING_SUBSCRIPTION_WARN, response.getSubscriptionAction());
    }

    @Test
    public void shouldReturnSubscriptionActionBlockWhenPastAdjustment() {
        final var webCertUser = createWebCertElegUser(false, true);

        setMockToReturnValue();

        final var response = subscriptionService.fetchSubscriptionInfo(webCertUser);

        assertEquals(SubscriptionAction.MISSING_SUBSCRIPTION_BLOCK, response.getSubscriptionAction());
    }

    @Test(expected = MissingSubscriptionException.class)
    public void shouldThrowExceptionIfSithsUserIsMissingSubscriptionWhenPastAdjustment() {
        final var webCertUser = createWebCertSithsUser(false, true, 1, 1, 0);

        setMockToSubscriptionOkForCareProvider("CARE_PROVIDER_ORGANIZATION_NO_1", HttpStatus.OK, "", false);

        subscriptionService.fetchSubscriptionInfo(webCertUser);
    }

    @Test(expected = MissingSubscriptionException.class)
    public void shouldThrowExceptionIfElegUserIsMissingSubscriptionWhenPastAdjustment() {
        final var webCertUser = createWebCertElegUser(false, true);

        setMockToSubscriptionOkForCareProvider("121212-1212", HttpStatus.OK, "", true);

        subscriptionService.fetchSubscriptionInfo(webCertUser);
    }

    @Test
    public void shouldUsePersonalNumberAsOrganizationNumberWhenElegUser() {
        final var webCertUser = createWebCertElegUser(true, false);
        final var captureUrl = ArgumentCaptor.forClass(String.class);
        final var personId = webCertUser.getPersonId();
        final var expectedOrganizationNumber = personId.substring(2, 8) + "-" + personId.substring(personId.length() - 4);

        setMockToReturnValue();

        subscriptionService.fetchSubscriptionInfo(webCertUser);

        verify(restTemplate).exchange(captureUrl.capture(), any(HttpMethod.class), any(HttpEntity.class), eq(MAP_STRING_BOOLEAN_TYPE));
        assertEquals(expectedOrganizationNumber, captureUrl.getValue().split("/")[6]);
    }

    @Test
    public void shouldUseCareProviderOrganizationNumberWhenSithsUser() {
        final var webCertUser = createWebCertSithsUser(true, false, 1, 1, 1);
        final var captureUrl = ArgumentCaptor.forClass(String.class);
        final var expectedOrganizationNumber = "CARE_PROVIDER_ORGANIZATION_NO_1";

        subscriptionService.fetchSubscriptionInfo(webCertUser);

        verify(restTemplate).exchange(captureUrl.capture(), any(HttpMethod.class), any(HttpEntity.class), eq(MAP_STRING_BOOLEAN_TYPE));
        assertEquals(expectedOrganizationNumber, captureUrl.getValue().split("/")[6]);
    }

    @Test
    public void shouldUseGetForSubscriptionCheckCallToKundportalen() {
        final var webCertUser = createWebCertElegUser(true, false);
        final var captureHttpMethod = ArgumentCaptor.forClass(HttpMethod.class);

        setMockToReturnValue();

        subscriptionService.fetchSubscriptionInfo(webCertUser);

        verify(restTemplate).exchange(any(String.class), captureHttpMethod.capture(), any(HttpEntity.class), eq(MAP_STRING_BOOLEAN_TYPE));
        assertEquals(HttpMethod.GET, captureHttpMethod.getValue());
    }

    @Test
    public void shouldAddAuthorizationHeaderToKundportalenRestRequest() {
        final var webCertUser = createWebCertElegUser(true, false);
        final var captureHttpEntity = ArgumentCaptor.forClass(HttpEntity.class);

        setMockToReturnValue();

        subscriptionService.fetchSubscriptionInfo(webCertUser);

        verify(restTemplate).exchange(any(String.class), any(HttpMethod.class), captureHttpEntity.capture(), eq(MAP_STRING_BOOLEAN_TYPE));
        assertTrue(captureHttpEntity.getValue().getHeaders().containsKey("Authorization"));
        assertEquals(1, Objects.requireNonNull(captureHttpEntity.getValue().getHeaders().get("Authorization")).size());
        assertTrue(Objects.requireNonNull(captureHttpEntity.getValue().getHeaders().get("Authorization")).contains(kundportalenAccessToken));
    }


    @Test
    public void shouldReturnZeroMissingSubscriptionsWhenHasSubscriptionForElegUser() {
        final var webCertUser = createWebCertElegUser(true, false);

        setMockToSubscriptionOkForCareProvider("121212-1212", HttpStatus.OK, elegServiceCodes.get(0), true);

        final var response = subscriptionService.fetchSubscriptionInfo(webCertUser);

        assertEquals(0, response.getUnitHsaIdList().size());
    }

    @Test
    public void shouldReturnOneHsaIdWhenSubscriptionMissingForElegUser() {
        final var webCertUser = createWebCertElegUser(true, false);

        setMockToSubscriptionOkForCareProvider("121212-1212", HttpStatus.OK, "", true);

        final var response = subscriptionService.fetchSubscriptionInfo(webCertUser);

        assertEquals(1, response.getUnitHsaIdList().size());
        assertEquals("CARE_PROVIDER_HSA_ID_1", response.getUnitHsaIdList().get(0));
    }

    @Test
    public void shouldReturnZeroMissingSubscriptionsWhenFailedServiceCallForElegUser() {
        final var webCertUser = createWebCertElegUser(true, false);

        setMockToSubscriptionOkForCareProvider("121212-1212", HttpStatus.SERVICE_UNAVAILABLE, elegServiceCodes.get(0),true);

        final var response = subscriptionService.fetchSubscriptionInfo(webCertUser);

        assertEquals(0, response.getUnitHsaIdList().size());
    }

    @Test
    public void shouldNotReturnMissingSubscriptionWhenFailedServiceCallForSithsUser() {
        final var webCertUser = createWebCertSithsUser(true, false, 2, 2, 0);

        setMockToSubscriptionOkForCareProvider("CARE_PROVIDER_ORGANIZATION_NO_1", HttpStatus.SERVICE_UNAVAILABLE, sithsServiceCodes.get(3), false);
        setMockToSubscriptionOkForCareProvider("CARE_PROVIDER_ORGANIZATION_NO_2", HttpStatus.OK, sithsServiceCodes.get(2), false);

        final var response = subscriptionService.fetchSubscriptionInfo(webCertUser);

        assertEquals(0, response.getUnitHsaIdList().size());
    }

    @Test
    public void shouldReturnZeroHsaIdsWhenSubscriptionOkForSithsUserWithSingleCareProvider() {
        final var webCertUser = createWebCertSithsUser(true, false, 1, 1, 1);

        setMockToSubscriptionOkForCareProvider("CARE_PROVIDER_ORGANIZATION_NO_1", HttpStatus.OK, sithsServiceCodes.get(2), false);

        final var response = subscriptionService.fetchSubscriptionInfo(webCertUser);

        assertEquals(0, response.getUnitHsaIdList().size());
    }

    @Test
    public void shouldReturnHsaIdWhenSubscriptionMissingForSithsUserWithSingleCareProvider() {
        final var webCertUser = createWebCertSithsUser(true, false, 1, 1, 1);

        setMockToSubscriptionOkForCareProvider("CARE_PROVIDER_ORGANIZATION_NO_1", HttpStatus.OK, "", false);

        final var response = subscriptionService.fetchSubscriptionInfo(webCertUser);

        assertEquals(1, response.getUnitHsaIdList().size());
        assertTrue(response.getUnitHsaIdList().contains("CARE_PROVIDER_HSA_ID_1"));
    }

    @Test
    public void shouldReturnZeroHsaIdsWhenSubscriptionsOkForSithsUserWithMultipleCareProviders() {
        final var webCertUser = createWebCertSithsUser(true, false, 3, 1, 1);

        setMockToSubscriptionOkForCareProvider("CARE_PROVIDER_ORGANIZATION_NO_1", HttpStatus.OK, sithsServiceCodes.get(2), false);

        final var response = subscriptionService.fetchSubscriptionInfo(webCertUser);

        assertEquals(0, response.getUnitHsaIdList().size());
    }

    @Test
    public void shouldReturnHsaIdWhenSubscriptionMissingForSithsUserWithMultipleCareProviders() {
        final var webCertUser = createWebCertSithsUser(true, false, 3, 2, 1);

        setMockToSubscriptionOkForCareProvider("CARE_PROVIDER_ORGANIZATION_NO_1", HttpStatus.OK, "", false);
        setMockToSubscriptionOkForCareProvider("CARE_PROVIDER_ORGANIZATION_NO_2", HttpStatus.OK, sithsServiceCodes.get(2), false);
        setMockToSubscriptionOkForCareProvider("CARE_PROVIDER_ORGANIZATION_NO_3", HttpStatus.OK, "", false);

        final var response = subscriptionService.fetchSubscriptionInfo(webCertUser);

        assertEquals(2, response.getUnitHsaIdList().size());
        assertTrue(response.getUnitHsaIdList().contains("CARE_PROVIDER_HSA_ID_1"));
        assertFalse(response.getUnitHsaIdList().contains("CARE_PROVIDER_HSA_ID_2"));
        assertTrue(response.getUnitHsaIdList().contains("CARE_PROVIDER_HSA_ID_3"));
    }

    @Test
    public void shouldCheckAllServiceCodesForSithsUser() {
        final var webCertUser = createWebCertSithsUser(true, false, 1, 1, 0);

        setMockToSubscriptionOkForCareProvider("CARE_PROVIDER_ORGANIZATION_NO_1", HttpStatus.OK, "", false);

        subscriptionService.fetchSubscriptionInfo(webCertUser);

        verify(restTemplate, times(sithsServiceCodes.size())).exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), eq(MAP_STRING_BOOLEAN_TYPE));
    }

    @Test
    public void shouldNotMakeExtraServiceCallsWhenSubscriptionFound() {
        final var webCertUser = createWebCertSithsUser(true, true, 1, 1, 0);

        setMockToSubscriptionOkForCareProvider("CARE_PROVIDER_ORGANIZATION_NO_1", HttpStatus.OK, sithsServiceCodes.get(1), false);

        subscriptionService.fetchSubscriptionInfo(webCertUser);

        verify(restTemplate, times(2)).exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), eq(MAP_STRING_BOOLEAN_TYPE));
    }

    @Test
    public void shouldReturnFalseWhenUnregisteredElegUserHasSubscription() {
        setMockToSubscriptionOkForCareProvider("121212-1212", HttpStatus.OK, elegServiceCodes.get(0), true);

        final var response = subscriptionService.fetchSubscriptionInfoUnregisteredElegUser(PERSON_ID);

        assertFalse(response);
    }

    @Test
    public void shouldReturnTrueWhenUnregisteredElegUserMissingSubscription() {
        setMockToSubscriptionOkForCareProvider("121212-1212", HttpStatus.OK, "", true);

        final var response = subscriptionService.fetchSubscriptionInfoUnregisteredElegUser(PERSON_ID);

        assertTrue(response);
    }

    @Test
    public void shouldReturnFalseWhenServiceCallFailureForUnregisteredElegUser() {
        setMockToSubscriptionOkForCareProvider("121212-1212", HttpStatus.SERVICE_UNAVAILABLE, elegServiceCodes.get(0), true);

        final var response = subscriptionService.fetchSubscriptionInfoUnregisteredElegUser(PERSON_ID);

        assertFalse(response);
    }

    @Test
    public void shouldReturnListWithAddedHsaIdWhenAcknowledgedWarning() {
        final var webCertUser = createWebCertElegUser(true, false);
        webCertUser.setSubscriptionInfo(SubscriptionInfo.createSubscriptionInfoNoAction());

        final var acknowledgedHsaId = "ACKNOWLEDGED_HSA_ID";
        final var expandedList = subscriptionService.setAcknowledgedWarning(webCertUser, acknowledgedHsaId);

        assertTrue(expandedList.contains(acknowledgedHsaId));
    }

    @Test
    public void shouldAddAcknowledgedHsaIdToWebCertUser() {
        final var webCertUser = createWebCertElegUser(true, false);
        webCertUser.setSubscriptionInfo(SubscriptionInfo.createSubscriptionInfoNoAction());

        final var acknowledgedHsaId = "ACKNOWLEDGED_HSA_ID";
        subscriptionService.setAcknowledgedWarning(webCertUser, acknowledgedHsaId);

        assertTrue(webCertUser.getSubscriptionInfo().getAcknowledgedWarnings().contains(acknowledgedHsaId));
    }

    @Test
    public void shouldSetStartBlockPeriodWhenAdjustmentOrBlockPeriod() {
        final var webCertUser = createWebCertElegUser(true, false);

        setMockToReturnValue();

        final var response = subscriptionService.fetchSubscriptionInfo(webCertUser);

        assertEquals(subscriptionBlockStartDate, response.getAdjustmentPeriodStartDate());
    }

    @Test
    public void shouldSetAuthenticationMethod() {
        final var webCertUser = createWebCertElegUser(true, false);

        setMockToReturnValue();

        final var response = subscriptionService.fetchSubscriptionInfo(webCertUser);

        assertEquals(AuthenticationMethodEnum.ELEG, response.getAuthenticationMethod());
    }

    private void setMockToReturnValue() {
        doReturn(new ResponseEntity<Map<String, Boolean>>(HttpStatus.OK)).when(restTemplate)
            .exchange(any(String.class),any(HttpMethod.class), any(HttpEntity.class), eq(MAP_STRING_BOOLEAN_TYPE));
    }

    private void setMockToSubscriptionOkForCareProvider(String organizationNumber, HttpStatus httpStatus,
        String serviceCodeWithSubscription, boolean isElegUser) {
        final var subscriptionTrue = Map.of("subscriptionActive", true);
        final var subscriptionFalse = Map.of("subscriptionActive", false);
        final var serviceCodes = isElegUser ? elegServiceCodes : sithsServiceCodes;

        for (var serviceCode : serviceCodes) {
            final var url = kundportalenSubscriptionServiceUrl + "/" + organizationNumber + "/" + serviceCode;
            final ResponseEntity<Map<String, Boolean>> responseEntity;
            if (serviceCode.equals(serviceCodeWithSubscription)) {
                responseEntity = new ResponseEntity<>(subscriptionTrue, httpStatus);
            } else {
                responseEntity = new ResponseEntity<>(subscriptionFalse, httpStatus);
            }
            doReturn(responseEntity).when(restTemplate).exchange(eq(url), any(HttpMethod.class), any(HttpEntity.class),
                eq(MAP_STRING_BOOLEAN_TYPE));
        }
    }

    private WebCertUser createWebCertElegUser(boolean duringAdjustment, boolean pastAdjustment) {
        final var webCertUser = createBaseWebCertUser(duringAdjustment, pastAdjustment);
        webCertUser.setAuthenticationScheme(ELEG_AUTHN_CLASSES.get(2));
        webCertUser.setVardgivare(getCareProviders(1, 1, 0));

        final var careProvider = webCertUser.getVardgivare().get(0);
        careProvider.getVardenheter().get(0).setId(careProvider.getId());
        return webCertUser;
    }

    private WebCertUser createWebCertSithsUser(boolean duringAdjustment, boolean pastAdjustment, int numCareProviders, int numCareUnits,
        int numUnits) {
        final var webCertUser = createBaseWebCertUser(duringAdjustment, pastAdjustment);
        webCertUser.setAuthenticationScheme(SITHS_AUTHN_CLASSES.get(1));
        webCertUser.setVardgivare(getCareProviders(numCareProviders, numCareUnits, numUnits));
        return webCertUser;
    }

    private WebCertUser createBaseWebCertUser(boolean duringAdjustment, boolean pastAdjustment) {
        final var webCertUser = new WebCertUser();
        webCertUser.setPersonId(PERSON_ID);
        webCertUser.setRoles(Map.of(AuthoritiesConstants.ROLE_PRIVATLAKARE, new Role()));
        webCertUser.setOrigin(UserOriginType.NORMAL.name());
        webCertUser.setFeatures(createFeatures(duringAdjustment, pastAdjustment));
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

    private Map<String, Feature> createFeatures(boolean subscriptionDuringAdjustmentPeriodActive,
        boolean subscriptionPastAdjustmentPeriodActive) {

        var featureSubscriptionDuringAdjustmentPeriod = new Feature();
        featureSubscriptionDuringAdjustmentPeriod.setGlobal(subscriptionDuringAdjustmentPeriodActive);
        featureSubscriptionDuringAdjustmentPeriod.setName(AuthoritiesConstants.FEATURE_SUBSCRIPTION_DURING_ADJUSTMENT_PERIOD);

        var featureSubscriptionPastAdjustmentPeriod = new Feature();
        featureSubscriptionPastAdjustmentPeriod.setGlobal(subscriptionPastAdjustmentPeriodActive);
        featureSubscriptionPastAdjustmentPeriod.setName(AuthoritiesConstants.FEATURE_SUBSCRIPTION_PAST_ADJUSTMENT_PERIOD);

        var features = new HashMap<String, Feature>();
        features.put(AuthoritiesConstants.FEATURE_SUBSCRIPTION_DURING_ADJUSTMENT_PERIOD, featureSubscriptionDuringAdjustmentPeriod);
        features.put(AuthoritiesConstants.FEATURE_SUBSCRIPTION_PAST_ADJUSTMENT_PERIOD, featureSubscriptionPastAdjustmentPeriod);
        return features;
    }
}
