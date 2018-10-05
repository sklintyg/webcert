/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.web.PortResolver;
import org.springframework.security.web.PortResolverImpl;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.session.web.http.CookieHttpSessionStrategy;
import org.springframework.session.web.http.MultiHttpSessionStrategy;
import org.springframework.stereotype.Component;

@Component
public class RedisSavedRequestCache implements RequestCache {

    private static final String SAVED_REQ_REDIS_PREFIX = "webcert:savedrequests:";

    private static final long TIMEOUT = 15L;

    private final Log logger = LogFactory.getLog(this.getClass());

    @Autowired
    @Qualifier("rediscache")
    private RedisTemplate<Object, Object> redisTemplate;

    // inject the template as ValueOperations
    @Resource(name = "rediscache")
    private ValueOperations<String, DefaultSavedRequest> valueOps;

    private PortResolver portResolver = new PortResolverImpl();
    private RequestMatcher requestMatcher = AnyRequestMatcher.INSTANCE;

    // Used to extract the sessionId from the SESSION cookie.
    private MultiHttpSessionStrategy httpSessionStrategy = new CookieHttpSessionStrategy();

    /**
     * Stores the current request, provided the configuration properties allow it.
     */
    @Override
    public void saveRequest(HttpServletRequest request, HttpServletResponse response) {
        if (requestMatcher.matches(request)) {
            DefaultSavedRequest savedRequest = new DefaultSavedRequest(request,
                    portResolver);
            String requestedSessionId = httpSessionStrategy.getRequestedSessionId(request);

            // SESSION (from Spring Session) not started yet, use httpSession.getId() instead (they should be identical)
            // This will typically happen on the very first request,
            // before the browser have gotten Set-Cookie: Session=<session-id> back.
            if (requestedSessionId == null) {
                requestedSessionId = request.getSession().getId();
            }

            if (requestedSessionId != null) {
                // Store the HTTP request itself. Used by
                // AbstractAuthenticationProcessingFilter
                // for redirection after successful authentication (SEC-29)
                valueOps.set(buildKey(requestedSessionId), savedRequest, TIMEOUT,
                        TimeUnit.MINUTES);
                logger.info("DefaultSavedRequest added to Redis: " + savedRequest);
            }
        } else {
            logger.info("Request not saved as configured RequestMatcher did not match");
        }
    }

    @Override
    public SavedRequest getRequest(HttpServletRequest currentRequest,
            HttpServletResponse response) {

        // Ignore requests if they don't match what our requestMatcher has configured.
        if (!requestMatcher.matches(currentRequest)) {
            return null;
        }
        String requestedSessionId = httpSessionStrategy.getRequestedSessionId(currentRequest);

        if (requestedSessionId != null) {
            return valueOps.get(buildKey(requestedSessionId));
        }
        return null;
    }

    @Override
    public void removeRequest(HttpServletRequest currentRequest,
            HttpServletResponse response) {
        String requestedSessionId = httpSessionStrategy.getRequestedSessionId(currentRequest);

        if (requestedSessionId != null) {
            logger.info("Removing DefaultSavedRequest from session if present");
            redisTemplate.delete(buildKey(requestedSessionId));
        }
    }

    @Override
    public HttpServletRequest getMatchingRequest(HttpServletRequest request,
            HttpServletResponse response) {
        DefaultSavedRequest saved = (DefaultSavedRequest) getRequest(request, response);

        if (saved == null) {
            return null;
        }

        if (!saved.doesRequestMatch(request, portResolver)) {
            logger.debug("saved request doesn't match");
            return null;
        }

        removeRequest(request, response);

        return new HttpServletRequestWrapper(request);
    }

    /**
     * Allows selective use of saved requests for a subset of requests. By default any
     * request will be cached by the {@code saveRequest} method.
     * <p>
     * If set, only matching requests will be cached.
     *
     * @param requestMatcher
     *            a request matching strategy which defines which requests
     *            should be cached.
     */
    public void setRequestMatcher(RequestMatcher requestMatcher) {
        this.requestMatcher = requestMatcher;
    }

    private String buildKey(String requestedSessionId) {
        return SAVED_REQ_REDIS_PREFIX + requestedSessionId;
    }

}
