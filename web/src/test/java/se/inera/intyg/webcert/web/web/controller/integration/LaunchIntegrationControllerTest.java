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

package se.inera.intyg.webcert.web.web.controller.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.integration.hsatk.model.legacy.SelectableVardenhet;
import se.inera.intyg.infra.security.authorities.CommonAuthoritiesResolver;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygTypeInfo;
import se.inera.intyg.webcert.web.web.controller.facade.util.ReactPilotUtil;
import se.inera.intyg.webcert.web.web.controller.facade.util.ReactUriFactory;

@ExtendWith(MockitoExtension.class)
class LaunchIntegrationControllerTest {

    private static final String CERTIFICATE_ID = "certificateId";

    @Mock
    private IntygService intygService;

    @Mock
    private WebCertUserService webCertUserService;

    @Mock
    private ReactUriFactory reactUriFactory;

    @Mock
    private ReactPilotUtil reactPilotUtil;

    @Mock
    private CommonAuthoritiesResolver commonAuthoritiesResolver;

    @InjectMocks
    private LaunchIntegrationController launchIntegrationController;

    private UriInfo uriInfo;
    private WebCertUser webcertUser;
    private static final String ORIGIN = "normal";


    @Test
    void shouldReturnArrayOfGrantedRoles() {
        final var expectedRoles = List.of(
            AuthoritiesConstants.ROLE_ADMIN, AuthoritiesConstants.ROLE_LAKARE, AuthoritiesConstants.ROLE_TANDLAKARE,
            AuthoritiesConstants.ROLE_SJUKSKOTERSKA, AuthoritiesConstants.ROLE_BARNMORSKA);
        final var grantedRoles = launchIntegrationController.getGrantedRoles();
        expectedRoles.forEach(role -> assertTrue(Arrays.asList(grantedRoles).contains(role)));
    }


    @Nested
    class TestRedirect {

        @BeforeEach
        void setup() {
            final var roles = new HashMap<>();
            roles.put("LAKARE", new Role());
            webcertUser = mock(WebCertUser.class);
            doReturn("NORMAL").when(webcertUser).getOrigin();
            doReturn(roles).when(webcertUser).getRoles();
            doReturn(webcertUser).when(webCertUserService).getUser();
            doReturn(mock(IntygTypeInfo.class)).when(intygService).getIntygTypeInfo(anyString());
        }

        @Nested
        class AngularTest {

            @BeforeEach
            void setup() {
                final var uriBuilder = mock(UriBuilder.class);
                uriInfo = mock(UriInfo.class);
                doReturn(uriBuilder).when(uriInfo).getBaseUriBuilder();
                doReturn(uriBuilder).when(uriBuilder).replacePath(any());
                doReturn(uriBuilder).when(uriBuilder).fragment(any());
                doReturn(mock(URI.class)).when(uriBuilder).buildFromMap(any());
                doReturn(false).when(reactPilotUtil).useReactClientFristaende(any(), any());
                doReturn(mock(SelectableVardenhet.class)).when(webcertUser).getValdVardenhet();
                doReturn(mock(SelectableVardenhet.class)).when(webcertUser).getValdVardgivare();
                doReturn(true).when(webcertUser).changeValdVardenhet(any());
            }

            @Test
            void shouldNotUseReactIfFeatureIsInactivated() {
                launchIntegrationController.redirectToCertificate(uriInfo, CERTIFICATE_ID, ORIGIN);

                verify(reactUriFactory, never()).uriForCertificate(any(), any());
            }

            @Test
            void shouldSetLaunchFromOriginOnUser() {
                launchIntegrationController.redirectToCertificate(uriInfo, CERTIFICATE_ID, ORIGIN);

                verify(webcertUser).setLaunchFromOrigin(ORIGIN);
            }

        }

        @Nested
        class ReactTest {

            @BeforeEach
            void setup() {
                doReturn(mock(URI.class)).when(reactUriFactory).uriForCertificate(any(), any());
                doReturn(true).when(reactPilotUtil).useReactClientFristaende(any(), any());
                doReturn(mock(SelectableVardenhet.class)).when(webcertUser).getValdVardenhet();
                doReturn(mock(SelectableVardenhet.class)).when(webcertUser).getValdVardgivare();
                doReturn(true).when(webcertUser).changeValdVardenhet(any());
            }

            @Test
            void shouldUseReactIfFeatureIsActivated() {
                launchIntegrationController.redirectToCertificate(uriInfo, CERTIFICATE_ID, ORIGIN);

                verify(reactUriFactory).uriForCertificate(any(), any());
            }

            @Test
            void shouldSetLaunchFromOriginOnUser() {
                launchIntegrationController.redirectToCertificate(uriInfo, CERTIFICATE_ID, ORIGIN);

                verify(webcertUser).setLaunchFromOrigin(ORIGIN);
            }
        }

        @Nested
        class ChangeUnit {

            @Nested
            class SuccessfullyChangedUnit {

                @BeforeEach
                void setUp() {
                    doReturn(mock(URI.class)).when(reactUriFactory).uriForCertificate(any(), any());
                    doReturn(true).when(reactPilotUtil).useReactClientFristaende(any(), any());
                    doReturn(mock(SelectableVardenhet.class)).when(webcertUser).getValdVardenhet();
                    doReturn(mock(SelectableVardenhet.class)).when(webcertUser).getValdVardgivare();
                    doReturn(true).when(webcertUser).changeValdVardenhet(any());
                }

                @Test
                void shallChangeToCorrectUnit() {
                    final var expectedUnitId = "unitId";
                    doReturn(expectedUnitId).when(intygService).getIssuingVardenhetHsaId(any(), any());
                    final var unitIdCaptor = ArgumentCaptor.forClass(String.class);
                    launchIntegrationController.redirectToCertificate(uriInfo, CERTIFICATE_ID, ORIGIN);
                    verify(webcertUser).changeValdVardenhet(unitIdCaptor.capture());
                    assertEquals(expectedUnitId, unitIdCaptor.getValue());
                }

                @Test
                void shallUpdateFeatures() {
                    launchIntegrationController.redirectToCertificate(uriInfo, CERTIFICATE_ID, ORIGIN);
                    verify(webcertUser).setFeatures(any());
                }
            }


            @Nested
            class FailedChangedUnit {

                @Test
                void shallThrowExceptionIfChangeUnitFails() {
                    doReturn(false).when(webcertUser).changeValdVardenhet(any());
                    assertThrows(WebCertServiceException.class, () ->
                        launchIntegrationController.redirectToCertificate(uriInfo, CERTIFICATE_ID, ORIGIN)
                    );
                }
            }
        }
    }
}
