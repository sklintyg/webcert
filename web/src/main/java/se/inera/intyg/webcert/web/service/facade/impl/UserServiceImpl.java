/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.facade.impl;

import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.metadata.Unit;
import se.inera.intyg.common.support.facade.model.user.LoginMethod;
import se.inera.intyg.common.support.facade.model.user.SigningMethod;
import se.inera.intyg.common.support.facade.model.user.User;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Mottagning;
import se.inera.intyg.infra.integration.hsatk.model.legacy.SelectableVardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.infra.security.common.model.AuthenticationMethod;
import se.inera.intyg.webcert.web.service.facade.UserService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@Service
public class UserServiceImpl implements UserService {

    private final WebCertUserService webCertUserService;

    @Autowired
    public UserServiceImpl(WebCertUserService webCertUserService) {
        this.webCertUserService = webCertUserService;
    }

    @Override
    public User getLoggedInUser() {
        final var webCertUser = webCertUserService.getUser();
        final var loggedInCareUnit = getLoggedInCareUnit(webCertUser);
        final var loggedInUnit = getLoggedInUnit(webCertUser);
        final var loggedInCareProvider = getLoggedInCareProvider(webCertUser);
        final var params = webCertUser.getParameters();
        final var isInactiveUnit = params != null && params.isInactiveUnit();

        return User.builder()
            .hsaId(webCertUser.getHsaId())
            .name(webCertUser.getNamn())
            .role(getRole(webCertUser))
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
            .build();
    }

    private SigningMethod getSigningMethod(AuthenticationMethod authenticationMethod) {
        switch (authenticationMethod) {
            case FAKE:
                return SigningMethod.FAKE;
            case SITHS:
            case NET_ID:
                return SigningMethod.DSS;
            case MOBILT_BANK_ID:
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
            case MOBILT_BANK_ID:
                return LoginMethod.BANK_ID;
            default:
                throw new IllegalArgumentException(
                    String.format("Login method '%s' not yet supported ", authenticationMethod));
        }
    }

    private String getRole(WebCertUser webCertUser) {
        try {
            final var roles = webCertUser.getRoles().values();
            return roles.stream().findFirst().orElseThrow().getDesc();

        } catch (NullPointerException | NoSuchElementException e) {
            return "Roll ej angiven";
        }
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
