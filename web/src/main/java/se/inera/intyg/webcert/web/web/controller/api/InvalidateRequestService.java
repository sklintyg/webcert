package se.inera.intyg.webcert.web.web.controller.api;

import com.itextpdf.xmp.impl.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.InvalidateRequest;


@Service
public class InvalidateRequestService {

    private final Cache launchIdCache;
    private final FindByIndexNameSessionRepository  sessionRepository;
    private final String SPRING_SECURITY_CONTEXT = "SPRING_SECURITY_CONTEXT";


    @Autowired
    public InvalidateRequestService(Cache launchIdCache, FindByIndexNameSessionRepository sessionRepository) {
        this.launchIdCache = launchIdCache;
        this.sessionRepository = sessionRepository;
    }

    public boolean checkIfLaunchIdMatchesWithCurrentSession(String launchId, String userLaunchId) {
        return launchId.equals(userLaunchId);
    }
    private String getCachedSessionId(String launchId) {
        return launchIdCache.get(launchId, String.class);
    }

    private String getHsaIdFromSession(String sessionId ){
        final var session = sessionRepository.findById(sessionId);
        if(session != null){
            SecurityContext authenticator = session.getAttribute(SPRING_SECURITY_CONTEXT);
            var user = (WebCertUser) authenticator.getAuthentication().getPrincipal();
            return user.getHsaId();
        }
        return "not found";
    }
    private String getLaunchIdFromSessionRedis(String sessionId ){
        final var session = sessionRepository.findById(sessionId);
        if(session != null){
            SecurityContext authenticator = session.getAttribute(SPRING_SECURITY_CONTEXT);
            var user = (WebCertUser) authenticator.getAuthentication().getPrincipal();
            return user.getParameters().getLaunchId();
        }
        return "not found";
    }

    public String getHsaIdFromRedisSession(String launchId) {
        final var sessionId = Base64.decode(getCachedSessionId(launchId));
        return getHsaIdFromSession(sessionId);
    }

    public boolean checkIfHsaIdMatchesWithSession(String hsaIdOnCurrentSession, String userHsaId) {
        return userHsaId.equals(hsaIdOnCurrentSession);
    }
    public String getLaunchIdStoredInRedis(String launchId){
        final var sessionId = Base64.decode(getCachedSessionId(launchId));
        return getLaunchIdFromSessionRedis(sessionId);
    }
    public void invalidateSession(String launchId){
        final var sessionId = Base64.decode(getCachedSessionId(launchId));
        sessionRepository.deleteById(sessionId);
    }
}
