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

package se.inera.intyg.webcert.web.service.launchid;

import com.itextpdf.xmp.impl.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.InvalidateRequest;


@Service
public class InvalidateSessionServiceImpl implements InvalidateSessionService {

    private static final Logger LOG = LoggerFactory.getLogger(InvalidateSessionServiceImpl.class);
    @Autowired
    private Cache redisCacheLaunchId;
    @Autowired
    private FindByIndexNameSessionRepository<?> sessionRepository;

    @Override
    public void invalidateSessionIfActive(InvalidateRequest invalidateRequest) {
        if (launchIdMissing(invalidateRequest.getLaunchId()) || sessionIsMissing(invalidateRequest.getLaunchId())) {
            LOG.info(String.format("InvalidateSessionServiceImpl called - launchId: %s - has no ongoing session stored in redis.",
                invalidateRequest.getLaunchId()));
            return;
        }
        final var user = getUserStoredInRedisSession(invalidateRequest.getLaunchId());
        if (valuesMatchWithSession(user, invalidateRequest)) {
            LOG.info(String.format(
                "InvalidateSessionServiceImpl called - launchId: %s - has ongoing session stored in redis with matching session"
                    + " - launchId on user is: %s - session will be removed",
                invalidateRequest.getLaunchId(),
                getUserStoredInRedisSession(invalidateRequest.getLaunchId()).getParameters().getLaunchId()));
            removeSession(invalidateRequest.getLaunchId());
        } else {
            LOG.info(String.format(
                "InvalidateSessionServiceImpl called - launchId: %s has no session stored in redis with matching session,"
                    + " session is not removed",
                invalidateRequest.getLaunchId()));
        }
    }

    private boolean sessionIsMissing(String launchId) {
        final var sessionKey = Base64.decode(getSessionKey(launchId));
        return sessionRepository.findById(sessionKey) == null;
    }

    private boolean launchIdMissing(String launchId) {
        return redisCacheLaunchId.get(launchId, String.class) == null;
    }

    private WebCertUser getUserStoredInRedisSession(String launchId) {
        final var sessionKey = Base64.decode(getSessionKey(launchId));
        final var session = sessionRepository.findById(sessionKey);
        final var authenticator = (SecurityContext) session.getAttribute("SPRING_SECURITY_CONTEXT");
        return (WebCertUser) authenticator.getAuthentication().getPrincipal();
    }

    private boolean valuesMatchWithSession(WebCertUser user, InvalidateRequest invalidateRequest) {
        return user.getParameters().getLaunchId().equals(invalidateRequest.getLaunchId()) && user.getHsaId()
            .equals(invalidateRequest.getUserHsaId());
    }

    private void removeSession(String launchId) {
        final var sessionKey = Base64.decode(getSessionKey(launchId));
        sessionRepository.deleteById(sessionKey);
    }

    private String getSessionKey(String launchId) {
        return redisCacheLaunchId.get(launchId, String.class);
    }
}
