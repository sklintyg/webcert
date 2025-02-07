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
package se.inera.intyg.webcert.web.service.facade.user;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.CareProvider;
import se.inera.intyg.common.support.facade.model.CareUnit;
import se.inera.intyg.common.support.facade.model.metadata.Unit;
import se.inera.intyg.common.support.facade.model.user.LoginMethod;
import se.inera.intyg.common.support.facade.model.user.SigningMethod;
import se.inera.intyg.common.support.facade.model.user.User;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Mottagning;
import se.inera.intyg.infra.integration.hsatk.model.legacy.SelectableVardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.infra.security.common.model.AuthenticationMethod;
import se.inera.intyg.webcert.web.service.subscription.dto.SubscriptionAction;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;

@Service
public class UserServiceImpl implements UserService {

    private static final Collator SORT_SWEDISH = Collator.getInstance(new Locale("sv", "SE"));

    private final WebCertUserService webCertUserService;

    @Autowired
    public UserServiceImpl(WebCertUserService webCertUserService) {
        this.webCertUserService = webCertUserService;
        SORT_SWEDISH.setStrength(Collator.PRIMARY);
    }

    @Override
    public User getLoggedInUser() {
        final var webCertUser = webCertUserService.getUser();
        final var loggedInCareUnit = getLoggedInCareUnit(webCertUser);
        final var loggedInUnit = getLoggedInUnit(webCertUser);
        final var loggedInCareProvider = getLoggedInCareProvider(webCertUser);
        final var params = webCertUser.getParameters();
        final var isInactiveUnit = params != null && params.isInactiveUnit();
        final var launchId = getLaunchId(params);

        return User.builder()
            .hsaId(webCertUser.getHsaId())
            .name(webCertUser.getNamn())
            .role(getRole(webCertUser))
            .origin(webCertUser.getOrigin())
            .launchId(launchId)
            .loggedInUnit(
                Unit.builder()
                    .unitName(
                        loggedInUnit.getNamn()
                    )
                    .unitId(
                        loggedInUnit.getId()
                    )
                    .isInactive(
                        isInactiveUnit
                    )
                    .build()
            )
            .loggedInCareUnit(
                Unit.builder()
                    .unitName(
                        loggedInCareUnit.getNamn()
                    )
                    .unitId(
                        loggedInCareUnit.getId()
                    )
                    .build()
            )
            .loggedInCareProvider(
                Unit.builder()
                    .unitName(
                        loggedInCareProvider.getNamn()
                    )
                    .unitId(
                        loggedInCareProvider.getId()
                    )
                    .build()
            )
            .protectedPerson(webCertUser.isSekretessMarkerad())
            .preferences(webCertUser.getAnvandarPreference())
            .loginMethod(getLoginMethod(webCertUser.getAuthenticationMethod()))
            .signingMethod(getSigningMethod(webCertUser.getAuthenticationMethod()))
            .careProviders(getCareProviders(webCertUser))
            .launchFromOrigin(webCertUser.getLaunchFromOrigin())
            .build();
    }

    private static String getLaunchId(IntegrationParameters params) {
        return (params != null && params.getLaunchId() != null) ? params.getLaunchId() : null;
    }

    private List<CareProvider> getCareProviders(WebCertUser webCertUser) {
        return webCertUser.getVardgivare().stream()
            .map(vardgivare -> CareProvider.builder()
                .id(vardgivare.getId())
                .name(vardgivare.getNamn())
                .careUnits(getCareUnits(vardgivare))
                .missingSubscription(isMissingSubscription(webCertUser, vardgivare.getId()))
                .build()
            )
            .sorted(Comparator.comparing(CareProvider::getName, SORT_SWEDISH))
            .collect(Collectors.toList());
    }

    private boolean isMissingSubscription(WebCertUser webCertUser, String careProviderId) {
        final var subscriptionInfo = webCertUser.getSubscriptionInfo();
        return subscriptionInfo != null
            && subscriptionInfo.getSubscriptionAction() == SubscriptionAction.BLOCK
            && subscriptionInfo.getCareProvidersMissingSubscription() != null
            && subscriptionInfo.getCareProvidersMissingSubscription().contains(careProviderId);
    }

    private List<CareUnit> getCareUnits(Vardgivare vardgivare) {
        return vardgivare.getVardenheter().stream()
            .map(vardenhet -> CareUnit.builder()
                .unitId(vardenhet.getId())
                .unitName(vardenhet.getNamn())
                .units(getUnits(vardenhet.getMottagningar()))
                .build()
            )
            .sorted(Comparator.comparing(CareUnit::getUnitName, SORT_SWEDISH))
            .collect(Collectors.toList());
    }

    private List<Unit> getUnits(List<Mottagning> mottagningar) {
        return mottagningar.stream()
            .map(mottagning -> Unit.builder()
                .unitId(mottagning.getId())
                .unitName(mottagning.getNamn())
                .build()
            )
            .sorted(Comparator.comparing(Unit::getUnitName, SORT_SWEDISH))
            .collect(Collectors.toList());
    }

    private SigningMethod getSigningMethod(AuthenticationMethod authenticationMethod) {
        switch (authenticationMethod) {
            case FAKE:
                return SigningMethod.FAKE;
            case SITHS:
            case NET_ID:
                return SigningMethod.DSS;
            case MOBILT_BANK_ID:
                return SigningMethod.MOBILT_BANK_ID;
            case BANK_ID:
                return SigningMethod.BANK_ID;
            default:
                throw new IllegalArgumentException(
                    String.format("Login method '%s' not yet supported with a signing method", authenticationMethod));
        }
    }

    private LoginMethod getLoginMethod(AuthenticationMethod authenticationMethod) {
        switch (authenticationMethod) {
            case FAKE:
                return LoginMethod.FAKE;
            case SITHS:
                return LoginMethod.SITHS;
            case BANK_ID:
                return LoginMethod.BANK_ID;
            case MOBILT_BANK_ID:
                return LoginMethod.BANK_ID_MOBILE;
            default:
                throw new IllegalArgumentException(
                    String.format("Login method '%s' not yet supported ", authenticationMethod));
        }
    }

    private String getRole(WebCertUser webCertUser) {
        final var roles = webCertUser.getRoles();
        if (roles == null || roles.values().isEmpty()) {
            return "Roll ej angiven";
        }

        return roles.values().stream().findFirst().orElseThrow().getDesc();
    }

    private SelectableVardenhet getLoggedInUnit(WebCertUser webCertUser) {
        return hasSelectedLoginUnits(webCertUser) ? webCertUser.getValdVardenhet() : new Vardenhet();
    }

    private SelectableVardenhet getLoggedInCareProvider(WebCertUser webCertUser) {
        return hasSelectedLoginUnits(webCertUser) ? webCertUser.getValdVardgivare() : new Vardgivare();
    }

    private boolean hasSelectedLoginUnits(WebCertUser webCertUser) {
        return webCertUser.getValdVardenhet() != null && webCertUser.getValdVardgivare() != null;
    }

    @Override
    public Vardenhet getLoggedInCareUnit(WebCertUser webCertUser) {
        if (!hasSelectedLoginUnits(webCertUser)) {
            return new Vardenhet();
        }

        final var loggedInCareProviderId = webCertUser.getValdVardgivare().getId();
        final var loggedInUnitId = webCertUser.getValdVardenhet().getId();
        final var currentCareProvider = webCertUser.getVardgivare().stream()
            .filter(careProvider -> careProvider.getId().equalsIgnoreCase(loggedInCareProviderId))
            .findFirst()
            .orElseThrow();

        return findCareUnit(currentCareProvider.getVardenheter(), loggedInUnitId);
    }

    private Vardenhet findCareUnit(List<Vardenhet> careUnits, String loggedInUnitId) {
        return careUnits.stream()
            .filter(careUnit -> careUnit.getId().equalsIgnoreCase(loggedInUnitId) || isSubunit(careUnit.getMottagningar(), loggedInUnitId))
            .findFirst()
            .orElseThrow();
    }

    private boolean isSubunit(List<Mottagning> units, String loggedInUnitId) {
        if (units == null) {
            return false;
        }
        return units.stream().anyMatch(unit -> unit.getId().equalsIgnoreCase(loggedInUnitId));
    }
}
