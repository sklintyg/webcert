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

package se.inera.intyg.webcert.web.web.controller.facade.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Feature;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@ExtendWith(MockitoExtension.class)
class AngularClientUtilTest {

    private static final String DJUPINTEGRATION = "DJUPINTEGRATION";
    private static final String REHABSTOD_LAUNCH_ORIGIN = "rs";
    private static final String ORIGIN_NORMAL = "ORIGIN_NORMAL";
    @Mock
    private WebCertUser user;
    @Mock
    private Feature feature;
    @InjectMocks
    private AngularClientUtil angularClientUtil;

    @Test
    void shallReturnFalseIfUserIsNotDjupintegration() {
        doReturn(ORIGIN_NORMAL).when(user).getOrigin();
        assertFalse(angularClientUtil.useAngularClient(user));
    }

    @Test
    void shallReturnFalseIfUserIsDjupintegrationButDoesNotHaveFeatureAngularClient() {
        doReturn(DJUPINTEGRATION).when(user).getOrigin();
        doReturn(Collections.emptyMap()).when(user).getFeatures();
        assertFalse(angularClientUtil.useAngularClient(user));
    }

    @Test
    void shallReturnFalseIfUserIsDjupintegrationAndHaveFeatureAngularClientButGlobalIsFalse() {
        doReturn(DJUPINTEGRATION).when(user).getOrigin();
        doReturn(Map.of(AuthoritiesConstants.FEATURE_USE_ANGULAR_WEBCLIENT, feature)).when(user).getFeatures();
        doReturn(false).when(feature).getGlobal();
        assertFalse(angularClientUtil.useAngularClient(user));
    }

    @Test
    void shallReturnTrueIfUsersLaunchOriginIsRSAndHaveFeatureAngularClientAndGlobalIsTrue() {
        doReturn(ORIGIN_NORMAL).when(user).getOrigin();
        doReturn(REHABSTOD_LAUNCH_ORIGIN).when(user).getLaunchFromOrigin();
        doReturn(Map.of(AuthoritiesConstants.FEATURE_USE_ANGULAR_WEBCLIENT, feature)).when(user).getFeatures();
        doReturn(true).when(feature).getGlobal();
        assertTrue(angularClientUtil.useAngularClient(user));
    }

    @Test
    void shallReturnTrueIfUserIsDjupintegrationAndHaveFeatureAngularClientAndGlobalIsTrue() {
        doReturn(DJUPINTEGRATION).when(user).getOrigin();
        doReturn(Map.of(AuthoritiesConstants.FEATURE_USE_ANGULAR_WEBCLIENT, feature)).when(user).getFeatures();
        doReturn(true).when(feature).getGlobal();
        assertTrue(angularClientUtil.useAngularClient(user));
    }
}
