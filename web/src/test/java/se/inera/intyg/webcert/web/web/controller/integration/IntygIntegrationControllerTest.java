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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.http.HttpHeaders;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.infra.security.authorities.CommonAuthoritiesResolver;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Feature;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.RequestOrigin;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.facade.util.ReactUriFactory;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;
import se.inera.intyg.webcert.web.web.controller.integration.dto.PrepareRedirectToIntyg;

@ExtendWith(MockitoExtension.class)
class IntygIntegrationControllerTest {

    private static final String ALTERNATE_SSN = "19010101-0101";
    private static final String INTYGSTYP = "lisjp";
    private static final String INTYGSTYPVERSION = "1.0";
    private static final String INTYGSID_POST = "6ce5fa9f-58d6-4a43-bc65-841c9646eb78";
    private static final String INTYGSID = "A1234-B5678-C90123-D4567";
    private static final String ENHETSID = "11111";
    private static final String LAUNCHID = "97f279ba-7d2b-4b0a-8665-7adde08f26f4";
    private static final String SESSION_ID = "12345";
    private UriInfo uriInfo;

    @Mock
    private WebCertUserService webCertUserService;

    @Mock
    private IntegrationService integrationService;

    @Mock
    private CommonAuthoritiesResolver authoritiesResolver;

    @Mock
    private ReactUriFactory reactUriFactory;

    @Mock
    private @Context
    HttpServletRequest httpServletRequest;

    @Mock
    private Cache redisCacheLaunchId;
    @Mock
    private CertificateAIPrefillService certificateAIPrefillService;
    @InjectMocks
    private IntygIntegrationController intygIntegrationController;

    @Test
    void testSavedRequestGETHandlerRequiresIntegrationParameters() {
        final var user = mock(WebCertUser.class);

        when(user.getParameters()).thenReturn(null);
        when(webCertUserService.getUser()).thenReturn(user);

        assertThrows(
            IllegalStateException.class,
            () -> intygIntegrationController.getRedirectToIntyg(null, null, INTYGSID, null),
            "Expected getRedirectToIntyg() to throw, but it didn't"
        );
    }

    private PrepareRedirectToIntyg createPrepareRedirectToIntyg() {
        PrepareRedirectToIntyg redirect = new PrepareRedirectToIntyg();
        redirect.setIntygId(INTYGSID);
        redirect.setIntygTyp(INTYGSTYP);
        redirect.setIntygTypeVersion(INTYGSTYPVERSION);
        redirect.setUtkast(true);
        return redirect;
    }

    private RequestOrigin createRequestOrigin(String name, List<String> intygstyper) {
        RequestOrigin o = new RequestOrigin();
        o.setName(name);
        o.setIntygstyper(intygstyper);
        return o;
    }

    private Privilege createPrivilege(List<String> intygsTyper, List<RequestOrigin> requestOrigins) {
        Privilege p = new Privilege();
        p.setName(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG);
        p.setIntygstyper(intygsTyper);
        p.setRequestOrigins(requestOrigins);
        return p;
    }

    private WebCertUser createUser(Privilege p, Map<String, Feature> features, String origin) {
        WebCertUser user = new WebCertUser();

        HashMap<String, Privilege> pMap = new HashMap<>();
        pMap.put(p.getName(), p);
        user.setAuthorities(pMap);

        user.setOrigin(origin);
        user.setFeatures(features);

        HashMap<String, Role> rMap = new HashMap<>();
        Role role = new Role();
        role.setName(AuthoritiesConstants.ROLE_LAKARE);
        rMap.put(AuthoritiesConstants.ROLE_LAKARE, role);

        user.setRoles(rMap);

        Vardgivare vg = new Vardgivare();
        vg.setId("vg1");
        Vardenhet ve = new Vardenhet();
        ve.setVardgivareHsaId("vg1");
        ve.setId(ENHETSID);
        vg.setVardenheter(List.of(ve));
        user.setVardgivare(List.of(vg));
        return user;
    }

    private WebCertUser createDefaultUser() {
        return createUser(
            createPrivilege(
                Arrays.asList("lisjp", "ts-bas"), // p1 is restricted to these intygstyper
                Arrays.asList(
                    createRequestOrigin(UserOriginType.DJUPINTEGRATION.name(), List.of("lisjp")),
                    createRequestOrigin(UserOriginType.DJUPINTEGRATION.name(), List.of("ts-bas")))),
            Stream.of(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST, "base_feature")
                .collect(Collectors.toMap(Function.identity(), s -> {
                    Feature feature = new Feature();
                    feature.setName(s);
                    feature.setIntygstyper(List.of("lisjp"));
                    return feature;
                })),
            UserOriginType.DJUPINTEGRATION.name());
    }

    private WebCertUser createDefaultUserWithIntegrationParameters() {
        final var user = createDefaultUser();
        user.setParameters(new IntegrationParameters(null, null, ALTERNATE_SSN, null, null, null, null,
            null, null, false, false, false, false, null, null));
        return user;
    }

    private WebCertUser createDefaultUserWithIntegrationParametersAndLaunchId1() {
        final var user = createDefaultUser();
        user.setParameters(IntegrationParameters.of(null, null, ALTERNATE_SSN, null, null, null, null,
            null, null, false, false, false, false, LAUNCHID, null));

        return user;
    }

    @Nested
    class LaunchIdPostVerification {

        private WebCertUser user;

        @BeforeEach
        void setup() {
            uriInfo = mock(UriInfo.class);
            when(integrationService.prepareRedirectToIntyg(any(), any()))
                .thenReturn(createPrepareRedirectToIntyg());
            when(authoritiesResolver.getFeatures(any())).thenReturn(new HashMap<>());
            this.user = createDefaultUser();
            when(webCertUserService.getUser()).thenReturn(user);
            doReturn(URI.create("https://wc.localtest.me/certificate/xxxx-yyyyy-zzzzz-qqqqq")).when(reactUriFactory)
                .uriForCertificate(uriInfo, INTYGSID);

            final var session = mock(HttpSession.class);
            when(httpServletRequest.getSession()).thenReturn(session);
            when(httpServletRequest.getSession().getId()).thenReturn("12345");
        }

        @Nested
        class PostiviteLaunchIdVerification {

            @Test
            void launchIdShouldAppearOnUserIfProvidedInPostWhenJumpIsExecuted() {
                intygIntegrationController.postRedirectToIntyg(uriInfo, httpServletRequest, INTYGSID_POST, "", "", "",
                    "", "", "", "", "", "", true, "", false,
                    false, true, LAUNCHID, null);

                assertEquals(LAUNCHID, user.getParameters().getLaunchId());
            }

            @Test
            void assertThatRedisAddsLaunchIdToCache() {
                intygIntegrationController.postRedirectToIntyg(uriInfo, httpServletRequest, INTYGSID_POST, "", "", "",
                    "", "", "", "", "", "", true, "", false,
                    false, true, LAUNCHID, null);

                verify(redisCacheLaunchId).put(anyString(), anyString());
            }

        }

        @Nested
        class NegativeLaunchIdVerification {

            @Test
            void assertThatLaunchIdIsNotGuid() {
                assertThrows(
                    IllegalArgumentException.class,
                    () -> {
                        intygIntegrationController.postRedirectToIntyg(uriInfo, httpServletRequest, INTYGSID_POST, "", "", "",
                            "", "", "", "", "", "", true, "", false,
                            false, true, "LAUNCH_ID_1", null);
                    },
                    "Provided launchId is not guid: LAUNCH_ID_1"
                );
                assertDoesNotThrow(() ->
                    intygIntegrationController.postRedirectToIntyg(uriInfo, httpServletRequest, INTYGSID_POST, "", "", "",
                        "", "", "", "", "", "", true, "", false,
                        false, true, "", null)
                );
            }

            @Test
            void handleLaunchIdThatIsNull() {
                assertDoesNotThrow(() ->
                    intygIntegrationController.postRedirectToIntyg(uriInfo, httpServletRequest, INTYGSID_POST, "", "", "",
                        "", "", "", "", "", "", true, "", false,
                        false, true, null, null)
                );
            }

            @Test
            void launchIdShouldBeAddedEvenIfNotProvided() {
                intygIntegrationController.postRedirectToIntyg(uriInfo, httpServletRequest, INTYGSID_POST, "", "", "",
                    "", "", "", "", "", "", true, "", false,
                    false, true, "", null);

                assertNull(user.getParameters().getLaunchId());
            }
        }
    }

    @Nested
    class LaunchIdSavedRequestVerification {

        @BeforeEach
        void setup() {
            uriInfo = mock(UriInfo.class);
            final var uriBuilder = UriBuilder.fromUri("https://wc.localtest.me/");

            when(integrationService.prepareRedirectToIntyg(any(), any()))
                .thenReturn(createPrepareRedirectToIntyg());
            when(authoritiesResolver.getFeatures(any())).thenReturn(new HashMap<>());
            WebCertUser user = createDefaultUserWithIntegrationParametersAndLaunchId1();
            when(webCertUserService.getUser()).thenReturn(user);
            doReturn(URI.create("https://wc.localtest.me/certificate/xxxx-yyyyy-zzzzz-qqqqq")).when(reactUriFactory)
                .uriForCertificate(uriInfo, INTYGSID);

            final var session = mock(HttpSession.class);
            when(httpServletRequest.getSession()).thenReturn(session);
            when(httpServletRequest.getSession().getId()).thenReturn(SESSION_ID);
        }

        @Nested
        class PostiviteLaunchIdVerification {

            @Test
            void assertThatRedisAddsLaunchIdToCache() {
                intygIntegrationController.getRedirectToIntyg(httpServletRequest, uriInfo, INTYGSID, ENHETSID);

                verify(redisCacheLaunchId).put(LAUNCHID, Base64.getEncoder().encodeToString(SESSION_ID.getBytes()));
            }
        }
    }

    @Nested
    class RedirectToCertificate {

        @BeforeEach
        void setup() {
            uriInfo = mock(UriInfo.class);
            when(integrationService.prepareRedirectToIntyg(any(), any()))
                .thenReturn(createPrepareRedirectToIntyg());
            when(authoritiesResolver.getFeatures(any())).thenReturn(new HashMap<>());

            final var session = mock(HttpSession.class);
            when(httpServletRequest.getSession()).thenReturn(session);
            when(httpServletRequest.getSession().getId()).thenReturn("12345");
        }

        @Test
        void shallRedirectWithStatusSeeOtherAsTheRedirectShouldAlwaysBeAGET() {
            final var user = createDefaultUserWithIntegrationParameters();

            when(webCertUserService.getUser()).thenReturn(user);
            doReturn(URI.create("https://wc.localtest.me/certificate/xxxx-yyyyy-zzzzz-qqqqq")).when(reactUriFactory)
                .uriForCertificate(uriInfo, INTYGSID);

            final var redirectToIntyg = intygIntegrationController.getRedirectToIntyg(httpServletRequest, uriInfo, INTYGSID, ENHETSID);

            assertEquals(Response.Status.SEE_OTHER.getStatusCode(), redirectToIntyg.getStatus());
        }

        @Test
        void shallRedirectToCertificate() {
            final var user = createDefaultUserWithIntegrationParameters();

            when(webCertUserService.getUser()).thenReturn(user);
            doReturn(URI.create("https://wc.localtest.me/certificate/A1234-B5678-C90123-D4567")).when(reactUriFactory)
                .uriForCertificate(uriInfo, INTYGSID);

            final var redirectToIntyg = intygIntegrationController.getRedirectToIntyg(httpServletRequest, uriInfo, INTYGSID, ENHETSID);

            assertEquals("https://wc.localtest.me/certificate/" + INTYGSID,
                redirectToIntyg.getMetadata().get(HttpHeaders.LOCATION).get(0).toString());
        }
    }

    @Nested
    class GrantedRoleTest {

        @Test
        void shouldReturnLakare() {
            assertEquals(AuthoritiesConstants.ROLE_LAKARE, intygIntegrationController.getGrantedRoles()[0]);
        }

        @Test
        void shouldReturnAdmin() {
            assertEquals(AuthoritiesConstants.ROLE_TANDLAKARE, intygIntegrationController.getGrantedRoles()[1]);
        }

        @Test
        void shouldReturnTandlakare() {
            assertEquals(AuthoritiesConstants.ROLE_ADMIN, intygIntegrationController.getGrantedRoles()[2]);
        }

        @Test
        void shouldReturnBarnmorska() {
            assertEquals(AuthoritiesConstants.ROLE_SJUKSKOTERSKA, intygIntegrationController.getGrantedRoles()[3]);
        }

        @Test
        void shouldReturnSjukskoterska() {
            assertEquals(AuthoritiesConstants.ROLE_BARNMORSKA, intygIntegrationController.getGrantedRoles()[4]);
        }
    }
}
