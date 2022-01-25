/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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

import static org.junit.Assert.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Feature;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
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
}