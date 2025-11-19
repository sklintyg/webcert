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

import static se.inera.intyg.privatepractitioner.dto.ValidatePrivatePractitionerResultCode.NO_ACCOUNT;
import static se.inera.intyg.privatepractitioner.dto.ValidatePrivatePractitionerResultCode.OK;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import se.inera.intyg.infra.security.authorities.CommonAuthoritiesResolver;
import se.inera.intyg.infra.security.common.model.AuthenticationMethod;
import se.inera.intyg.infra.security.common.model.UserOrigin;
import se.inera.intyg.privatepractitioner.dto.ValidatePrivatePractitionerResultCode;
import se.inera.intyg.webcert.integration.pp.services.PPRestService;
import se.inera.intyg.webcert.integration.privatepractitioner.service.PrivatePractitionerIntegrationService;
import se.inera.intyg.webcert.logging.HashUtility;
import se.inera.intyg.webcert.web.auth.common.AuthConstants;
import se.inera.intyg.webcert.web.auth.exceptions.MissingSubscriptionException;
import se.inera.intyg.webcert.web.auth.exceptions.PrivatePractitionerAuthorizationException;
import se.inera.intyg.webcert.web.privatepractitioner.toggle.PrivatePractitionerServiceProfile;
import se.inera.intyg.webcert.web.service.subscription.SubscriptionService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@Component
@Slf4j
@RequiredArgsConstructor
public class ElegWebCertUserDetailsService {

    private final CommonAuthoritiesResolver commonAuthoritiesResolver;
    private final PPRestService ppRestService;
    private final Optional<UserOrigin> userOrigin;
    private final SubscriptionService subscriptionService;
    private final HashUtility hashUtility;
    private final LegacyAuthorizedPrivatePractitionerService legacyAuthorizedPrivatePractitionerService;
    private final PrivatePractitionerServiceProfile privatePractitionerServiceProfile;
    @Nullable
    private final PrivatePractitionerIntegrationService privatePractitionerIntegrationService;
    @Nullable
    private final UnauthorizedPrivatePractitionerService unauthorizedPrivatePractitionerService;
    @Nullable
    private final AuthorizedPrivatePractitionerService authorizedPrivatePractitionerService;

    public WebCertUser buildFakeUserPrincipal(String personId) {
        return buildUserPrincipal(personId, AuthConstants.FAKE_AUTHENTICATION_ELEG_CONTEXT_REF, AuthenticationMethod.FAKE);
    }

    public WebCertUser buildUserPrincipal(String personId, String authenticationScheme, AuthenticationMethod authenticationMethodMethod) {
        if (privatePractitionerServiceProfile.isEnabled()) {
            return handleWithPrivatePractitionerService(personId, authenticationScheme, authenticationMethodMethod);
        }

        return handleWithPrivatlakarportal(personId, authenticationScheme, authenticationMethodMethod);
    }

    private WebCertUser handleWithPrivatePractitionerService(String personId, String authScheme, AuthenticationMethod authMethod) {
        if (unauthorizedPrivatePractitionerService == null) {
            throw new IllegalStateException("UnauthorizedPrivatePractitionerService is not available");
        }

        if (authorizedPrivatePractitionerService == null) {
            throw new IllegalStateException("AuthorizedPrivatePractitionerService is not available");
        }

        if (privatePractitionerIntegrationService == null) {
            throw new IllegalStateException("PrivatePractitionerIntegrationService is not available");
        }

        final var origin = resolveRequestOrigin();
        final var validationResult = privatePractitionerIntegrationService.validatePrivatePractitioner(personId);
        return switch (validationResult.resultCode()) {
            case OK -> {
                final var user = authorizedPrivatePractitionerService.create(personId, origin, authScheme, authMethod);
                subscriptionService.checkSubscriptions(user);
                yield user;
            }
            case NOT_AUTHORIZED_IN_HOSP -> unauthorizedPrivatePractitionerService.create(personId, origin, authScheme, authMethod);
            case NO_ACCOUNT -> {
                if (subscriptionService.isUnregisteredElegUserMissingSubscription(personId)) {
                    throw missingSubscriptionException(hashUtility.hash(personId));
                }
                yield unauthorizedPrivatePractitionerService.create(personId, origin, authScheme, authMethod);
            }
        };
    }

    private WebCertUser handleWithPrivatlakarportal(String personId, String authScheme, AuthenticationMethod authMethod) {
        final var ppAuthStatus = ppRestService.validatePrivatePractitioner(personId).getResultCode();
        redirectUnregisteredUsers(personId, ppAuthStatus);

        final var user = legacyAuthorizedPrivatePractitionerService.create(personId, resolveRequestOrigin(), authScheme, authMethod);
        assertWebCertUserIsAuthorized(user, ppAuthStatus);
        return user;
    }

    private void redirectUnregisteredUsers(String personId, ValidatePrivatePractitionerResultCode ppAuthStatus) {
        if (ppAuthStatus == NO_ACCOUNT) {
            final var hasSubscription = !subscriptionService.isUnregisteredElegUserMissingSubscription(personId);
            if (hasSubscription) {
                throw privatePractitionerAuthorizationException(hashUtility.hash(personId));
            }
            throw missingSubscriptionException(hashUtility.hash(personId));
        }
    }

    private void assertWebCertUserIsAuthorized(WebCertUser webCertUser, ValidatePrivatePractitionerResultCode ppAuthStatus) {
        final var hasSubscription = subscriptionService.checkSubscriptions(webCertUser);
        if (ppAuthStatus == OK) {
            return;
        }
        if (hasSubscription) {
            throw privatePractitionerAuthorizationException(webCertUser.getHsaId());
        }
        throw missingSubscriptionException(webCertUser.getHsaId());
    }

    private PrivatePractitionerAuthorizationException privatePractitionerAuthorizationException(String hashedPersonIdOrHsaId) {
        return new PrivatePractitionerAuthorizationException("User '" + hashedPersonIdOrHsaId + "' is not authorized to access webcert "
            + "according to private practitioner portal");
    }

    private static MissingSubscriptionException missingSubscriptionException(String hashedPersonIdOrHsaId) {
        return new MissingSubscriptionException("Private practitioner '" + hashedPersonIdOrHsaId + "' was denied access to Webcert due to "
            + "missing subscription.");
    }

    private String resolveRequestOrigin() {
        if (userOrigin.isEmpty()) {
            throw new IllegalStateException("No WebCertUserOrigin present, cannot login user.");
        }
        final var requestOrigin = userOrigin.get().resolveOrigin(getCurrentRequest());
        return commonAuthoritiesResolver.getRequestOrigin(requestOrigin).getName();
    }

    private HttpServletRequest getCurrentRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }
}
