/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
import se.inera.intyg.webcert.web.csintegration.aggregate.GetIssuingUnitIdAggregator;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.facade.util.ReactUriFactory;

@ExtendWith(MockitoExtension.class)
class LaunchIntegrationControllerTest {

    private static final String CERTIFICATE_ID = "certificateId";
    private static final String UNIT_ID = "unitId";

    @Mock
    private GetIssuingUnitIdAggregator getIssuingUnitIdAggregator;

    @Mock
    private WebCertUserService webCertUserService;

    @Mock
    private ReactUriFactory reactUriFactory;

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
            doReturn(UNIT_ID).when(getIssuingUnitIdAggregator).get(CERTIFICATE_ID);
        }

        @Nested
        class ReactCertificateTest {

            @BeforeEach
            void setup() {
                doReturn(mock(URI.class)).when(reactUriFactory).uriForCertificate(any(), any());
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
                    doReturn(mock(SelectableVardenhet.class)).when(webcertUser).getValdVardenhet();
                    doReturn(mock(SelectableVardenhet.class)).when(webcertUser).getValdVardgivare();
                    doReturn(true).when(webcertUser).changeValdVardenhet(any());
                }

                @Test
                void shallChangeToCorrectUnit() {
                    final var unitIdCaptor = ArgumentCaptor.forClass(String.class);
                    launchIntegrationController.redirectToCertificate(uriInfo, CERTIFICATE_ID, ORIGIN);
                    verify(webcertUser).changeValdVardenhet(unitIdCaptor.capture());
                    assertEquals(UNIT_ID, unitIdCaptor.getValue());
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

        @Nested
        class QuestionsTest {

            @BeforeEach
            void setup() {
                doReturn(mock(URI.class)).when(reactUriFactory).uriForCertificateQuestions(any(), any());
                doReturn(mock(SelectableVardenhet.class)).when(webcertUser).getValdVardenhet();
                doReturn(mock(SelectableVardenhet.class)).when(webcertUser).getValdVardgivare();
                doReturn(true).when(webcertUser).changeValdVardenhet(any());
            }

            @Test
            void shouldDirectToCertificateQuestions() {
                launchIntegrationController.directToCertificateQuestions(uriInfo, CERTIFICATE_ID);

                verify(reactUriFactory).uriForCertificateQuestions(any(), any());
            }
        }
    }
}
