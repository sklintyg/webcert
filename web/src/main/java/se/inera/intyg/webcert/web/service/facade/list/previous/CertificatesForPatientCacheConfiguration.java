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
package se.inera.intyg.webcert.web.service.facade.list.previous;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.inera.intyg.infra.rediscache.core.RedisCacheOptionsSetter;

@Configuration
public class CertificatesForPatientCacheConfiguration {

    @Value("${app.name:webcert}")
    private String appName;
    @Value("${certificatesForPatientCache.cache.expiry}")
    private String cacheExpirySeconds;

    @Autowired
    private RedisCacheOptionsSetter redisCacheOptionsSetter;

    @Bean
    public Cache certificatesForPatientCache() {
        return redisCacheOptionsSetter.createCache("certificatesForPatientCache:" + appName, cacheExpirySeconds);
    }
}
