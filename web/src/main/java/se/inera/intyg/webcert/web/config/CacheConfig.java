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
package se.inera.intyg.webcert.web.config;

import static se.inera.intyg.webcert.web.auth.CustomAuthenticationEntrypoint.ELEG_REQUEST_MATCHER;
import static se.inera.intyg.webcert.web.auth.CustomAuthenticationEntrypoint.SITHS_NORMAL_REQUEST_MATCHER;
import static se.inera.intyg.webcert.web.auth.CustomAuthenticationEntrypoint.SITHS_REQUEST_MATCHER;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

@Configuration
@ImportResource({"classpath:basic-cache-config.xml"})
public class CacheConfig {

    @Bean
    StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        return new StringRedisTemplate(redisConnectionFactory);
    }

    @Bean
    public RequestCache requestCache() {
        final var requestCache = new HttpSessionRequestCache();
        requestCache.setRequestMatcher(new OrRequestMatcher(ELEG_REQUEST_MATCHER, SITHS_REQUEST_MATCHER, SITHS_NORMAL_REQUEST_MATCHER));
        requestCache.setMatchingRequestParameterName(null);
        return requestCache;
    }

}
