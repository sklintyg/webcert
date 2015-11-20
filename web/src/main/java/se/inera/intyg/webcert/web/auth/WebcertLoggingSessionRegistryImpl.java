package se.inera.intyg.webcert.web.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistryImpl;

import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;

/**
 * Implementation of SessionRegistry that performs audit logging of login and logout.
 *
 * @author npet
 *
 */
public class WebcertLoggingSessionRegistryImpl extends SessionRegistryImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebcertLoggingSessionRegistryImpl.class);

    @Autowired
    private MonitoringLogService monitoringService;

    @Override
    public void registerNewSession(String sessionId, Object principal) {

        LOGGER.debug("Attempting to register new session '{}'", sessionId);

        if (principal != null && principal instanceof WebCertUser) {
            WebCertUser user = (WebCertUser) principal;
            monitoringService.logUserLogin(user.getHsaId(), user.getAuthenticationScheme());
        }

        super.registerNewSession(sessionId, principal);
    }

    @Override
    public void removeSessionInformation(String sessionId) {

        LOGGER.debug("Attempting to remove session '{}'", sessionId);

        SessionInformation sessionInformation = getSessionInformation(sessionId);

        if (sessionInformation == null) {
            super.removeSessionInformation(sessionId);
            return;
        }

        Object principal = sessionInformation.getPrincipal();

        if (principal instanceof WebCertUser) {
            WebCertUser user = (WebCertUser) principal;
            if (sessionInformation.isExpired()) {
                monitoringService.logUserSessionExpired(user.getHsaId(), user.getAuthenticationScheme());
            } else {
                monitoringService.logUserLogout(user.getHsaId(), user.getAuthenticationScheme());
            }
        }

        super.removeSessionInformation(sessionId);
    }

}
