/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.facade.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Feature;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

class ReactPilotUtilTest {

    private ReactPilotUtil reactPilotUtil;

    private WebCertUser user;
    private String certificateType;
    private Map<String, Feature> features = new HashMap<>();

    @BeforeEach
    void setUp() {
        reactPilotUtil = new ReactPilotUtil();

        user = mock(WebCertUser.class);
        certificateType = "lisjp";

        doReturn(features).when(user).getFeatures();
    }

    private Feature getUseReactWebclientFeature(boolean global) {
        final var feature = new Feature();
        feature.setName(AuthoritiesConstants.FEATURE_USE_REACT_WEBCLIENT);
        feature.setIntygstyper(Arrays.asList(certificateType));
        feature.setGlobal(global);
        return feature;
    }

    @Nested
    class TestBasedOnOrigin {

        @BeforeEach
        void setUp() {
            final var reactPilotFeature = getUseReactWebclientFeature(true);
            features.put(reactPilotFeature.getName(), reactPilotFeature);
        }

        @Test
        void shallNotUseReactClientIfOriginNormal() {
            doReturn(UserOriginType.NORMAL.name()).when(user).getOrigin();

            final var actualResult = reactPilotUtil.useReactClient(user, certificateType);

            assertFalse(actualResult);
        }

        @Test
        void shallNotUseReactClientIfOriginReadonly() {
            doReturn(UserOriginType.READONLY.name()).when(user).getOrigin();

            final var actualResult = reactPilotUtil.useReactClient(user, certificateType);

            assertFalse(actualResult);
        }

        @Test
        void shallNotUseReactClientIfOriginUthopp() {
            doReturn(UserOriginType.UTHOPP.name()).when(user).getOrigin();

            final var actualResult = reactPilotUtil.useReactClient(user, certificateType);

            assertFalse(actualResult);
        }

        @Test
        void shallUseReactClientIfOriginDjupintegration() {
            doReturn(UserOriginType.DJUPINTEGRATION.name()).when(user).getOrigin();

            final var actualResult = reactPilotUtil.useReactClient(user, certificateType);

            assertTrue(actualResult);
        }
    }

    @Nested
    class TestBasedOnFeature {

        @BeforeEach
        void setUp() {
            doReturn(UserOriginType.DJUPINTEGRATION.name()).when(user).getOrigin();
        }

        @Test
        void shallNotUseReactClientIfFeatureMissing() {
            final var actualResult = reactPilotUtil.useReactClient(user, certificateType);

            assertFalse(actualResult);
        }

        @Test
        void shallNotUseReactClientIfFeatureNotActive() {
            final var reactPilotFeature = getUseReactWebclientFeature(false);
            features.put(reactPilotFeature.getName(), reactPilotFeature);

            final var actualResult = reactPilotUtil.useReactClient(user, certificateType);

            assertFalse(actualResult);
        }

        @Test
        void shallNotUseReactClientIfFeatureMissingType() {
            final var reactPilotFeature = getUseReactWebclientFeature(true);
            features.put(reactPilotFeature.getName(), reactPilotFeature);

            final var actualResult = reactPilotUtil.useReactClient(user, "anotherType");

            assertFalse(actualResult);
        }

        @Test
        void shallUseReactClientIfFeatureActive() {
            final var reactPilotFeature = getUseReactWebclientFeature(true);
            features.put(reactPilotFeature.getName(), reactPilotFeature);

            final var actualResult = reactPilotUtil.useReactClient(user, certificateType);

            assertTrue(actualResult);
        }

        @Test
        void shallUseReactClientIfFeatureActiveForAllCertificateTypes() {
            final var reactPilotFeature = getUseReactWebclientFeature(true);
            reactPilotFeature.setIntygstyper(Collections.emptyList());
            features.put(reactPilotFeature.getName(), reactPilotFeature);

            final var actualResult = reactPilotUtil.useReactClient(user, certificateType);

            assertTrue(actualResult);
        }
    }
}