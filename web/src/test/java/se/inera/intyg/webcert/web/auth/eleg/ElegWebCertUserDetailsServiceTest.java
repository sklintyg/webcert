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
package se.inera.intyg.webcert.web.auth.eleg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataConstants.DR_KRANSTEGE_PERSON_ID;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import se.inera.intyg.infra.security.authorities.CommonAuthoritiesResolver;
import se.inera.intyg.infra.security.common.model.AuthenticationMethod;
import se.inera.intyg.infra.security.common.model.RequestOrigin;
import se.inera.intyg.webcert.integration.privatepractitioner.dto.PrivatePractitionerValidationResponse;
import se.inera.intyg.webcert.integration.privatepractitioner.dto.PrivatePractitionerValidationResultCode;
import se.inera.intyg.webcert.integration.privatepractitioner.service.PrivatePractitionerIntegrationService;
import se.inera.intyg.webcert.logging.HashUtility;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationTestSetup;
import se.inera.intyg.webcert.web.auth.exceptions.MissingSubscriptionException;
import se.inera.intyg.webcert.web.security.WebCertUserOrigin;
import se.inera.intyg.webcert.web.service.subscription.SubscriptionService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@ExtendWith(MockitoExtension.class)
class ElegWebCertUserDetailsServiceTest extends AuthoritiesConfigurationTestSetup {

    private static final String ELEG_AUTH_SCHEME = "http://id.elegnamnden.se/loa/1.0/loa3";
    private static final AuthenticationMethod AUTH_METHOD = AuthenticationMethod.MOBILT_BANK_ID;

    @Mock
    private SubscriptionService subscriptionService;
    @Mock
    private HashUtility hashUtility;
    @Mock
    private PrivatePractitionerIntegrationService privatePractitionerIntegrationService;
    @Mock
    private CommonAuthoritiesResolver commonAuthoritiesResolver;
    @Mock
    private UnauthorizedPrivatePractitionerService unauthorizedPrivatePractitionerService;
    @Mock
    private AuthorizedPrivatePractitionerService authorizedPrivatePractitionerService;
    @InjectMocks
    private ElegWebCertUserDetailsService elegWebCertUserDetailsService;

    @BeforeEach
    void setup() {
        final var request = mock(HttpServletRequest.class);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        final var userOrigin = mock(WebCertUserOrigin.class);
        lenient().when(userOrigin.resolveOrigin(any(HttpServletRequest.class))).thenReturn("NORMAL");
        ReflectionTestUtils.setField(elegWebCertUserDetailsService, "userOrigin", Optional.of(userOrigin));

        final var requestOrigin = mock(RequestOrigin.class);
        when(requestOrigin.getName()).thenReturn("origin");
        when(commonAuthoritiesResolver.getRequestOrigin(any())).thenReturn(requestOrigin);
    }

    @Test
    void shouldThrowMissingSubscriptionExceptionWhenNoAccoundAndNoSubscription() {
        when(privatePractitionerIntegrationService.validatePrivatePractitioner(DR_KRANSTEGE_PERSON_ID))
            .thenReturn(
                new PrivatePractitionerValidationResponse(PrivatePractitionerValidationResultCode.NO_ACCOUNT, "Test result text"));
        when(subscriptionService.isUnregisteredElegUserMissingSubscription(DR_KRANSTEGE_PERSON_ID)).thenReturn(true);

        assertThrows(MissingSubscriptionException.class,
            () -> elegWebCertUserDetailsService.buildUserPrincipal(DR_KRANSTEGE_PERSON_ID, ELEG_AUTH_SCHEME, AUTH_METHOD)
        );
    }

    @Test
    void shouldUnauthorizedUserWhenNoAccoundAndHaveSubscription() {
        final var expected = mockCreateUnauthorisedUser();
        when(privatePractitionerIntegrationService.validatePrivatePractitioner(DR_KRANSTEGE_PERSON_ID))
            .thenReturn(
                new PrivatePractitionerValidationResponse(PrivatePractitionerValidationResultCode.NO_ACCOUNT, "Test result text"));
        when(subscriptionService.isUnregisteredElegUserMissingSubscription(DR_KRANSTEGE_PERSON_ID)).thenReturn(false);

        final var actual = elegWebCertUserDetailsService.buildUserPrincipal(DR_KRANSTEGE_PERSON_ID, ELEG_AUTH_SCHEME, AUTH_METHOD);
        assertEquals(expected, actual);
    }

    @Test
    void shouldUnauthorizedUserWhenNotAuthorizedAndNoSubscription() {
        final var expected = mockCreateUnauthorisedUser();
        when(privatePractitionerIntegrationService.validatePrivatePractitioner(DR_KRANSTEGE_PERSON_ID))
            .thenReturn(new PrivatePractitionerValidationResponse(PrivatePractitionerValidationResultCode.NOT_AUTHORIZED_IN_HOSP,
                "Test result text"));

        final var actual = elegWebCertUserDetailsService.buildUserPrincipal(DR_KRANSTEGE_PERSON_ID, ELEG_AUTH_SCHEME, AUTH_METHOD);
        assertEquals(expected, actual);
    }

    @Test
    void shouldAuthorizedUserWhenAuthorized() {
        final var expected = mockCreateAuthorizedUser();
        when(privatePractitionerIntegrationService.validatePrivatePractitioner(DR_KRANSTEGE_PERSON_ID))
            .thenReturn(new PrivatePractitionerValidationResponse(PrivatePractitionerValidationResultCode.OK, "Test result text"));

        final var actual = elegWebCertUserDetailsService.buildUserPrincipal(DR_KRANSTEGE_PERSON_ID, ELEG_AUTH_SCHEME, AUTH_METHOD);
        assertEquals(expected, actual);
    }

    @Test
    void shouldDecorateAuthorizedUserWithSubscriptionInfoWhenAuthorized() {
        final var authorizedUser = mockCreateAuthorizedUser();
        when(privatePractitionerIntegrationService.validatePrivatePractitioner(DR_KRANSTEGE_PERSON_ID))
            .thenReturn(new PrivatePractitionerValidationResponse(PrivatePractitionerValidationResultCode.OK, "Test result text"));

        elegWebCertUserDetailsService.buildUserPrincipal(DR_KRANSTEGE_PERSON_ID, ELEG_AUTH_SCHEME, AUTH_METHOD);
        verify(subscriptionService).checkSubscriptions(authorizedUser);
    }

    private WebCertUser mockCreateUnauthorisedUser() {
        final var webcertUser = new WebCertUser("test");
        when(unauthorizedPrivatePractitionerService.create(
            DR_KRANSTEGE_PERSON_ID, "origin", ELEG_AUTH_SCHEME, AUTH_METHOD)).thenReturn(webcertUser);
        return webcertUser;
    }

    private WebCertUser mockCreateAuthorizedUser() {
        final var webcertUser = new WebCertUser("test");
        when(authorizedPrivatePractitionerService.create(
            DR_KRANSTEGE_PERSON_ID, "origin", ELEG_AUTH_SCHEME, AUTH_METHOD)).thenReturn(webcertUser);
        return webcertUser;
    }
}
