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

package se.inera.intyg.webcert.web.web.controller.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Feature;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.facade.util.ReactPilotUtil;
import se.inera.intyg.webcert.web.web.controller.facade.util.ReactUriFactory;
import se.inera.intyg.webcert.web.web.controller.legacyintegration.CertificateIntegrationController;

@ExtendWith(MockitoExtension.class)
public class CertificateIntegrationControllerTest {

    @Mock
    private IntygService intygService;

    @Mock
    private WebCertUserService webCertUserService;

    @Mock
    private ReactUriFactory reactUriFactory;

    @Mock
    private ReactPilotUtil reactPilotUtil;

    @InjectMocks
    private CertificateIntegrationController certificateIntegrationController;

    private Map<String, Feature> features = new HashMap<>();
    private Map<String, Role> roles = new HashMap<>();
    private WebCertUser user;

    @BeforeEach
    void setup() {
        user = mock(WebCertUser.class);
        roles.put("LAKARE", new Role());
        doReturn("NORMAL").when(user).getOrigin();
        doReturn(features).when(user).getFeatures();
        doReturn(roles).when(user).getRoles();

        when(webCertUserService.getUser()).thenReturn(user);
    }

    @Test
    public void shouldUseReactIfFeatureIsActivated() {
        final var reactPilotFeature = getUseReactWebclientFeature(true);
        features.put(reactPilotFeature.getName(), reactPilotFeature);

        certificateIntegrationController.redirectToIntyg(null, "certificateId", "unitId");
        verify(reactUriFactory.uriForCertificate(any(), any()));
    }

    private Feature getUseReactWebclientFeature(boolean global) {
        final var feature = new Feature();
        feature.setName(AuthoritiesConstants.FEATURE_USE_REACT_WEBCLIENT_FRISTAENDE);
        feature.setIntygstyper(List.of("lisjp"));
        feature.setGlobal(global);
        return feature;
    }

}
