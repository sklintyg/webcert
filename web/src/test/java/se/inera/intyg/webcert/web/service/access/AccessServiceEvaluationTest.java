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
package se.inera.intyg.webcert.web.service.access;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Feature;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.subscription.dto.SubscriptionAction;
import se.inera.intyg.webcert.web.service.subscription.dto.SubscriptionInfo;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;

@RunWith(MockitoJUnitRunner.class)
public class AccessServiceEvaluationTest {

    @Mock
    private WebCertUserService webCertUserService;
    @Mock
    private PatientDetailsResolver patientDetailsResolver;
    @Mock
    private UtkastService utkastService;
    @Mock
    private IntygTextsService intygTextsService;
    @Mock
    private WebCertUser user;

    private AccessServiceEvaluation accessServiceEvaluation;

    @Before
    public void setup() {
        accessServiceEvaluation = AccessServiceEvaluation
            .create(webCertUserService, patientDetailsResolver, utkastService, intygTextsService);
    }

    @Test
    public void shallNotAllowIfFeatureInactiveCertificateTypeIsTrue() {
        final var feature = new Feature();
        feature.setGlobal(true);
        feature.setIntygstyper(Collections.singletonList("ts-bas"));

        final var features = Map.of(AuthoritiesConstants.FEATURE_INACTIVE_CERTIFICATE_TYPE, feature);
        when(user.getFeatures()).thenReturn(features);

        final var actualAccessResult = accessServiceEvaluation.given(user, "ts-bas")
            .checkInactiveCertificateType()
            .evaluate();

        assertEquals(AccessResultCode.INACTIVE_CERTIFICATE_TYPE, actualAccessResult.getCode());
    }

    @Test
    public void shallAllowIfFeatureInactiveCertificateTypeIsFalse() {
        final var feature = new Feature();
        feature.setGlobal(true);
        feature.setIntygstyper(Collections.singletonList("ts-bas"));

        final var features = Map.of(AuthoritiesConstants.FEATURE_INACTIVE_CERTIFICATE_TYPE, feature);
        when(user.getFeatures()).thenReturn(features);

        final var actualAccessResult = accessServiceEvaluation.given(user, "ts-diabetes")
            .checkInactiveCertificateType()
            .evaluate();

        assertEquals(AccessResultCode.NO_PROBLEM, actualAccessResult.getCode());
    }

    @Test
    public void shallBlockIfNotOfLatestMajorVersion() {
        final var feature = new Feature();
        feature.setGlobal(true);
        feature.setIntygstyper(Collections.singletonList("ts-bas"));

        final var features = Map.of(AuthoritiesConstants.FEATURE_INACTIVATE_PREVIOUS_MAJOR_VERSION, feature);
        when(user.getFeatures()).thenReturn(features);

        final var actualAccessResult = accessServiceEvaluation.given(user, "ts-bas")
            .checkLatestCertificateTypeVersion("6.8")
            .evaluate();

        assertEquals(AccessResultCode.NOT_LATEST_MAJOR_VERSION, actualAccessResult.getCode());
    }

    @Test
    public void shallBlockIfNotOfLatestMajorVersionWhenAddCheck() {
        final var feature = new Feature();
        feature.setGlobal(true);
        feature.setIntygstyper(Collections.singletonList("ts-bas"));

        final var features = Map.of(AuthoritiesConstants.FEATURE_INACTIVATE_PREVIOUS_MAJOR_VERSION, feature);
        when(user.getFeatures()).thenReturn(features);

        final var actualAccessResult = accessServiceEvaluation.given(user, "ts-bas")
            .checkLatestCertificateTypeVersionIf("6.8", true)
            .evaluate();

        assertEquals(AccessResultCode.NOT_LATEST_MAJOR_VERSION, actualAccessResult.getCode());
    }

    @Test
    public void shallAllowIfNotOfLatestMajorVersionWhenNotAddCheck() {
        final var actualAccessResult = accessServiceEvaluation.given(user, "ts-bas")
            .checkLatestCertificateTypeVersionIf("6.8", false)
            .evaluate();
        assertEquals(AccessResultCode.NO_PROBLEM, actualAccessResult.getCode());

        // Make sure no interaction with user-mock as it would show that the check is being made anyway.
        verifyNoInteractions(user);
    }

    @Test
    public void shallAllowIfLatestMajorVersion() {
        final var feature = new Feature();
        feature.setGlobal(true);
        feature.setIntygstyper(Collections.singletonList("ts-bas"));

        final var features = Map.of(AuthoritiesConstants.FEATURE_INACTIVATE_PREVIOUS_MAJOR_VERSION, feature);
        when(user.getFeatures()).thenReturn(features);
        when(intygTextsService.isLatestMajorVersion("ts-bas", "7.0")).thenReturn(true);

        final var actualAccessResult = accessServiceEvaluation.given(user, "ts-bas")
            .checkLatestCertificateTypeVersion("7.0")
            .evaluate();

        assertEquals(AccessResultCode.NO_PROBLEM, actualAccessResult.getCode());
    }

    @Test
    public void shallAllowIfNotLatestMajorVersionWhenNotActive() {
        final var feature = new Feature();
        feature.setGlobal(true);
        feature.setIntygstyper(Collections.singletonList("ts-bas"));

        final var features = Map.of(AuthoritiesConstants.FEATURE_INACTIVATE_PREVIOUS_MAJOR_VERSION, feature);
        when(user.getFeatures()).thenReturn(features);

        final var actualAccessResult = accessServiceEvaluation.given(user, "lisjp")
            .checkLatestCertificateTypeVersion("2.0")
            .evaluate();

        assertEquals(AccessResultCode.NO_PROBLEM, actualAccessResult.getCode());
    }

    @Test
    public void shallAllowIfNotLatestMajorVersionWhenFeatureMissing() {
        when(user.getFeatures()).thenReturn(Collections.emptyMap());

        final var actualAccessResult = accessServiceEvaluation.given(user, "lisjp")
            .checkLatestCertificateTypeVersion("2.0")
            .evaluate();

        assertEquals(AccessResultCode.NO_PROBLEM, actualAccessResult.getCode());
    }

    @Test
    public void shallAllowIfNoBlockRuleActive() {
        final var feature = new Feature();
        feature.setGlobal(false);

        final var features = Map.of(AuthoritiesConstants.FEATURE_ENABLE_BLOCK_ORIGIN_NORMAL, feature);
        when(user.getFeatures()).thenReturn(features);

        final var actualAccessResult = accessServiceEvaluation.given(user, "lisjp")
            .blockFeature(AuthoritiesConstants.FEATURE_ENABLE_BLOCK_ORIGIN_NORMAL)
            .evaluate();

        assertEquals(AccessResultCode.NO_PROBLEM, actualAccessResult.getCode());
    }

    @Test
    public void shallAllowIfNoBlockRuleExists() {
        when(user.getFeatures()).thenReturn(Collections.emptyMap());

        final var actualAccessResult = accessServiceEvaluation.given(user, "lisjp")
            .blockFeature(AuthoritiesConstants.FEATURE_ENABLE_BLOCK_ORIGIN_NORMAL)
            .evaluate();

        assertEquals(AccessResultCode.NO_PROBLEM, actualAccessResult.getCode());
    }

    @Test
    public void shallBlockIfBlockRuleValid() {
        final var feature = new Feature();
        feature.setGlobal(true);

        final var features = Map.of(AuthoritiesConstants.FEATURE_ENABLE_BLOCK_ORIGIN_NORMAL, feature);
        when(user.getFeatures()).thenReturn(features);

        final var actualAccessResult = accessServiceEvaluation.given(user, "lisjp")
            .blockFeature(AuthoritiesConstants.FEATURE_ENABLE_BLOCK_ORIGIN_NORMAL)
            .evaluate();

        assertEquals(AccessResultCode.AUTHORIZATION_BLOCKED, actualAccessResult.getCode());
    }

    @Test
    public void shallAllowIfHasSubscriptionWhenSubscriptionRequired() {
        setupMocksForSubscriptionCheck(SubscriptionAction.BLOCK, 2, UserOriginType.NORMAL);

        final var actualAccessResult = accessServiceEvaluation.given(user, "lisjp")
            .checkSubscription()
            .evaluate();

        assertEquals(AccessResultCode.NO_PROBLEM, actualAccessResult.getCode());
    }

    @Test
    public void shallNotAllowIfMissingSubscriptionWhenSubscriptionRequired() {
        setupMocksForSubscriptionCheck(SubscriptionAction.BLOCK, 3, UserOriginType.NORMAL);

        final var actualAccessResult = accessServiceEvaluation.given(user, "lisjp")
            .checkSubscription()
            .evaluate();

        assertEquals(AccessResultCode.MISSING_SUBSCRIPTION, actualAccessResult.getCode());
    }

    @Test
    public void shallAllowIfMissingSubscriptionWhenSubscriptionRequiredButNotFristaende() {
        setupMocksForSubscriptionCheck(SubscriptionAction.BLOCK, 3, UserOriginType.DJUPINTEGRATION);

        final var actualAccessResult = accessServiceEvaluation.given(user, "lisjp")
            .checkSubscription()
            .evaluate();

        assertEquals(AccessResultCode.NO_PROBLEM, actualAccessResult.getCode());
    }

    @Test
    public void shallAllowIfMissingSubscriptionWhenSubscriptionNotRequired() {
        setupMocksForSubscriptionCheck(SubscriptionAction.NONE, 3, UserOriginType.NORMAL);

        final var actualAccessResult = accessServiceEvaluation.given(user, "lisjp")
            .checkSubscription()
            .evaluate();

        assertEquals(AccessResultCode.NO_PROBLEM, actualAccessResult.getCode());
    }

    private void setupMocksForSubscriptionCheck(SubscriptionAction subscriptionAction, int numberOfMissingSubscriptions,
        UserOriginType userOriginType) {
        final var subscriptionInfo = getSubscriptionInfo(subscriptionAction, numberOfMissingSubscriptions);
        final var selectedCareProviderMock = mock(Vardgivare.class);
        when(user.getSubscriptionInfo()).thenReturn(subscriptionInfo);
        when(user.getOrigin()).thenReturn(userOriginType.name());
        when(user.getValdVardgivare()).thenReturn(selectedCareProviderMock);
        when(selectedCareProviderMock.getId()).thenReturn("CARE_PROVIDER_HSA_ID_3");
    }

    private SubscriptionInfo getSubscriptionInfo(SubscriptionAction subscriptionAction, int numberOfMissingSubscriptions) {
        final var missingSubscriptionList = getMissingSubscriptionsList(numberOfMissingSubscriptions);
        final var subscriptionInfo = new SubscriptionInfo();
        subscriptionInfo.setSubscriptionAction(subscriptionAction);
        subscriptionInfo.setCareProvidersMissingSubscription(List.copyOf(missingSubscriptionList));
        return subscriptionInfo;
    }

    private List<String> getMissingSubscriptionsList(int numberOfMissingSubscriptions) {
        final var careProviderHsaIds = new ArrayList<String>();
        for (var i = 1; i <= numberOfMissingSubscriptions; i++) {
            careProviderHsaIds.add("CARE_PROVIDER_HSA_ID_" + i);
        }
        return careProviderHsaIds;
    }
}