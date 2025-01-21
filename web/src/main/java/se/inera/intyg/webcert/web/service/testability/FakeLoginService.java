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

package se.inera.intyg.webcert.web.service.testability;

import com.google.common.base.Strings;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Mottagning;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.infra.security.authorities.AuthoritiesException;
import se.inera.intyg.infra.security.authorities.CommonAuthoritiesResolver;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.auth.WebcertUserDetailsService;
import se.inera.intyg.webcert.web.auth.eleg.ElegWebCertUserDetailsService;
import se.inera.intyg.webcert.web.auth.fake.FakeAuthenticationToken;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.FakeLoginDTO;

@Service
@Profile("!prod")
@RequiredArgsConstructor
public class FakeLoginService {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final ElegWebCertUserDetailsService elegWebCertUserDetailsService;
    private final WebcertUserDetailsService webcertUserDetailsService;
    private final CommonAuthoritiesResolver authoritiesResolver;

    public void login(FakeLoginDTO fakeProps, HttpServletRequest request) {
        final var oldSession = request.getSession(false);
        Optional.ofNullable(oldSession).ifPresent(HttpSession::invalidate);

        WebCertUser webCertUser;
        final var personId = Personnummer.createPersonnummer(fakeProps.getHsaId());

        if (personId.isPresent()) {
            final var userId = personId.get().getPersonnummerWithDash();
            webCertUser = elegWebCertUserDetailsService.buildFakeUserPrincipal(userId);
        } else {
            webCertUser = webcertUserDetailsService.buildFakeUserPrincipal(fakeProps.getHsaId());
        }

        updateUserWithFakeloginProperties(webCertUser, fakeProps);

        final var fakeAuthenticationToken = new FakeAuthenticationToken(webCertUser);
        final var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(fakeAuthenticationToken);
        SecurityContextHolder.setContext(context);

        final var newSession = request.getSession(true);
        newSession.setAttribute(
            HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context
        );

        applicationEventPublisher.publishEvent(
            new InteractiveAuthenticationSuccessEvent(
                fakeAuthenticationToken, this.getClass()
            )
        );
    }

    public void logout(HttpSession session) {
        if (session == null) {
            return;
        }

        session.invalidate();

        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityContextHolder.getContext().setAuthentication(null);
        SecurityContextHolder.clearContext();

        if (authentication != null) {
            applicationEventPublisher.publishEvent(new LogoutSuccessEvent(authentication));
        }
    }

    private void updateUserWithFakeloginProperties(WebCertUser user, FakeLoginDTO fakeProps) {
        setName(user, fakeProps.getForNamn(), fakeProps.getEfterNamn());
        setUnit(user, fakeProps.getEnhetId());
        setProtectedPerson(user, fakeProps.getSekretessMarkerad());
        setOrigin(user, fakeProps.getOrigin());
        setFeatures(user);
    }

    private void setName(WebCertUser user, String forNamn, String efternamn) {
        if (!Strings.isNullOrEmpty(user.getName())) {
            return;
        }
        user.setNamn(forNamn + " " + efternamn);
    }

    private void setUnit(WebCertUser user, String enhetId) {
        if (Strings.isNullOrEmpty(enhetId)) {
            return;
        }
        setVardenhetById(enhetId, user);
        setVardgivareByVardenhetId(enhetId, user);
    }

    private void setProtectedPerson(WebCertUser user, Boolean protectedPerson) {
        if (protectedPerson == null) {
            return;
        }
        user.setSekretessMarkerad(protectedPerson);
    }

    private void setFeatures(WebCertUser user) {
        if (user.getValdVardenhet() == null || user.getValdVardgivare() == null) {
            return;
        }
        final var unitList = List.of(user.getValdVardenhet().getId(), user.getValdVardgivare().getId());
        final var features =  authoritiesResolver.getFeatures(unitList);
        user.setFeatures(features);
    }

    private void setOrigin(WebCertUser user, String origin) {
        user.setOrigin(origin);
    }

    private void setVardgivareByVardenhetId(String enhetId, IntygUser intygUser) {
        for (Vardgivare vg : intygUser.getVardgivare()) {
            for (Vardenhet ve : vg.getVardenheter()) {
                if (ve.getId().equals(enhetId)) {
                    intygUser.setValdVardgivare(vg);
                    return;
                } else if (ve.getMottagningar() != null) {
                    for (Mottagning m : ve.getMottagningar()) {
                        if (m.getId().equals(enhetId)) {
                            intygUser.setValdVardgivare(vg);
                            return;
                        }
                    }
                }
            }
        }
        throw new AuthoritiesException("Could not select a Vårdgivare given the fake credentials, not logging in.");
    }

    private void setVardenhetById(String enhetId, IntygUser intygUser) {
        for (Vardgivare vg : intygUser.getVardgivare()) {
            for (Vardenhet ve : vg.getVardenheter()) {
                if (ve.getId().equals(enhetId)) {
                    intygUser.setValdVardenhet(ve);
                    return;
                } else if (ve.getMottagningar() != null) {
                    for (Mottagning m : ve.getMottagningar()) {
                        if (m.getId().equals(enhetId)) {
                            intygUser.setValdVardenhet(m);
                            return;
                        }
                    }
                }
            }
        }
        throw new AuthoritiesException("Could not select a Vardenhet given the fake credentials, not logging in.");
    }

}
