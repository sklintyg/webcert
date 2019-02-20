/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
import org.springframework.stereotype.Component;

/**
 * Intyg custom RequestCache for storing and retrieving saved requests from Redis, used for redirects
 * and {@link se.inera.intyg.webcert.web.security.WebCertUserOrigin} resolution.
 *
 * This class is necessary when running Spring Session @ Redis,
 * the {@link org.springframework.security.web.savedrequest.HttpSessionRequestCache} does not work nicely with
 * {@link org.springframework.session.data.redis.RedisOperationsSessionRepository}.
 *
 * @author eriklupander
 */
@Component
public class RedisSavedRequestCache implements RequestCache {

    private final Log logger = LogFactory.getLog(this.getClass());

    // Prefix all keys in redis with this.
    private static final String SAVED_REQ_REDIS_PREFIX = "webcert:savedrequests:";

    // Expire unused saved requests after 15 minutes.
    private static final long TIMEOUT = 15L;

    private PortResolver portResolver = new PortResolverImpl();
    private RequestMatcher requestMatcher = AnyRequestMatcher.INSTANCE;

    @Autowired
    @Qualifier("rediscache")
    private RedisTemplate<Object, Object> redisTemplate;

    // inject the template as ValueOperations
    @Resource(name = "rediscache")
    private ValueOperations<String, DefaultSavedRequest> valueOps;


    /**
     * Stores the current request, provided the configuration properties allow it.
     *
     * Tries to use the session-id from Cookie: Session=session-id if possible. If thie is the very first request being
     * served, then the browser won't have the Cookie yet and in that case we use the ID from the HttpSession instead.
     * This works since {@link org.springframework.session.data.redis.RedisOperationsSessionRepository} uses the very same
     * id when creating the SESSION and responding with the Set-Cookie.
     */
    @Override
    public void saveRequest(HttpServletRequest request, HttpServletResponse response) {
        if (requestMatcher.matches(request)) {
            DefaultSavedRequest savedRequest = new DefaultSavedRequest(request,
                    portResolver);
            String requestedSessionId = getSessionId(request);

            if (requestedSessionId != null) {
                // Store the HTTP request itself. Used by
                // AbstractAuthenticationProcessingFilter
                // for redirection after successful authentication (SEC-29)
                valueOps.set(buildKey(requestedSessionId), savedRequest, TIMEOUT,
                        TimeUnit.MINUTES);
                logger.debug("DefaultSavedRequest added to Redis: " + savedRequest);
            }
        } else {
            logger.debug("Request not saved as configured RequestMatcher did not match");
        }
    }

    /**
     * Retrieves any saved request from Redis for the current session.
     */
    @Override
    public SavedRequest getRequest(HttpServletRequest currentRequest,
            HttpServletResponse response) {

        String requestedSessionId = getSessionId(currentRequest);

        if (requestedSessionId != null) {
            return valueOps.get(buildKey(requestedSessionId));
        }
        return null;
    }

    /**
     * Removes any saved request from Redis for the current session. Typically invoked
     * by {@link org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler} or
     * similar after "consuming" the SavedRequest when redirecting.
     */
    @Override
    public void removeRequest(HttpServletRequest currentRequest,
            HttpServletResponse response) {
        String requestedSessionId = getSessionId(currentRequest);

        if (requestedSessionId != null) {
            logger.debug("Removing DefaultSavedRequest from session if present");
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

    private String getSessionId(HttpServletRequest request) {
        return request.getSession().getId();
    }
}
