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
package se.inera.intyg.webcert.web.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

/**
 * Implementation of SessionRegistry that performs audit logging of login and logout.
 *
 * @author npet
 */

@Service
public class WebcertLoggingSessionRegistryImpl<T extends Session> extends SpringSessionBackedSessionRegistry<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebcertLoggingSessionRegistryImpl.class);

    private final MonitoringLogService monitoringService;
    private final FindByIndexNameSessionRepository<T> sessionRepository;

    public WebcertLoggingSessionRegistryImpl(MonitoringLogService monitoringService,
        FindByIndexNameSessionRepository<T> sessionRepository) {
        super(sessionRepository);
        this.sessionRepository = sessionRepository;
        this.monitoringService = monitoringService;
    }

    @Override
    public void registerNewSession(String sessionId, Object principal) {
        LOGGER.debug("Attempting to register new session '{}'", sessionId);

        if (!isWebcertUser(principal)) {
            return;
        }

        final var user = (WebCertUser) principal;
        final var userRole = getUserRole(user);
        monitoringService.logUserLogin(user.getHsaId(), userRole, user.getRoleTypeName(), user.getAuthenticationScheme(), user.getOrigin());

        super.registerNewSession(sessionId, principal);
    }

    @Override
    public void removeSessionInformation(String sessionId) {
        LOGGER.debug("Attempting to remove session '{}'", sessionId);

        final var session = sessionRepository.findById(sessionId);
        final var user = getUser(session);
        if (user == null) {
            super.removeSessionInformation(sessionId);
            return;
        }

        if (session.isExpired()) {
            monitoringService.logUserSessionExpired(user.getHsaId(), user.getAuthenticationScheme());
        } else {
            monitoringService.logUserLogout(user.getHsaId(), user.getAuthenticationScheme());
        }

        super.removeSessionInformation(sessionId);
    }

    private WebCertUser getUser(Session session) {
        if (session == null) {
            return null;
        }
        final var authenticator = (SecurityContext) session.getAttribute("SPRING_SECURITY_CONTEXT");
        final var principal = authenticator != null ? authenticator.getAuthentication().getPrincipal() : null;
        return isWebcertUser(principal) ? (WebCertUser) principal : null;
    }

    private boolean isWebcertUser(Object principal) {
        return principal instanceof WebCertUser;
    }

    private static String getUserRole(WebCertUser user) {
        return user.getRoles() != null && user.getRoles().size() == 1 ? user.getRoles().keySet().iterator().next() : "noRole?";
    }
}
