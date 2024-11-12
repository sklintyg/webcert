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

package se.inera.intyg.webcert.web.web.controller.legacyintegration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.integration.hsatk.model.legacy.SelectableVardenhet;
import se.inera.intyg.infra.security.authorities.CommonAuthoritiesResolver;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.facade.util.ReactUriFactory;

@ExtendWith(MockitoExtension.class)
public class CertificateIntegrationControllerTest {

    private static final String CERTIFICATE_ID = "certificateId";
    private static final String UNIT_ID = "unitId";
    private static final String CERTIFICATE_TYPE = "type";
    private static final String CERTIFICATE_TYPE_VERSION = "typeVersion";

    @Mock
    private IntygService intygService;

    @Mock
    private WebCertUserService webCertUserService;

    @Mock
    private ReactUriFactory reactUriFactory;

    @Mock
    private CommonAuthoritiesResolver commonAuthoritiesResolver;

    @InjectMocks
    private CertificateIntegrationController certificateIntegrationController;

    private UriInfo uriInfo;
    private WebCertUser webcertUser;

    @BeforeEach
    void setup() {
        final var roles = new HashMap<>();
        roles.put("LAKARE", new Role());
        webcertUser = mock(WebCertUser.class);
        doReturn("NORMAL").when(webcertUser).getOrigin();
        doReturn(roles).when(webcertUser).getRoles();

        when(webCertUserService.getUser()).thenReturn(webcertUser);
        when(webcertUser.changeValdVardenhet(any())).thenReturn(true);

        final var mockedSelectableVardenhet = mock(SelectableVardenhet.class);
        doReturn(mockedSelectableVardenhet).when(webcertUser).getValdVardenhet();
        doReturn(mockedSelectableVardenhet).when(webcertUser).getValdVardgivare();
        doReturn(Collections.emptyMap()).when(commonAuthoritiesResolver).getFeatures(anyList());
    }

    @Nested
    class ReactTest {

        @BeforeEach
        void setup() {
            doReturn(mock(URI.class)).when(reactUriFactory).uriForCertificate(any(), any());
        }

        @Test
        void shouldUseReactIfFeatureIsActivatedFk7263() {
            certificateIntegrationController.redirectToIntyg(uriInfo, CERTIFICATE_ID, UNIT_ID);
            verify(reactUriFactory).uriForCertificate(any(), any());
        }

        @Test
        void shouldUseReactIfFeatureIsActivated() {
            certificateIntegrationController.redirectToIntyg(uriInfo, CERTIFICATE_TYPE, CERTIFICATE_ID, UNIT_ID);

            verify(reactUriFactory).uriForCertificate(any(), any());
        }

        @Test
        void shouldUpdateFeaturesForLoggedInUser() {
            certificateIntegrationController.redirectToIntyg(uriInfo, CERTIFICATE_TYPE, CERTIFICATE_ID, UNIT_ID);

            verify(webcertUser).setFeatures(anyMap());
        }
    }
}
