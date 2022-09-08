package se.inera.intyg.webcert.web.web.controller.api.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.inera.intyg.infra.rediscache.core.RedisCacheOptionsSetter;

@Configuration
public class LaunchIdCacheConfiguration {
     @Value("${app.name:webcert}")
     private String appName;

     @Value("${launchId.cache.expiry}")
     private String launchCacheExpirySeconds;

     @Autowired
     private RedisCacheOptionsSetter redisCacheOptionsSetter;

     @Bean
     public Cache launchIdCache() {
         return redisCacheOptionsSetter.createCache("launchIdCache:" + appName, launchCacheExpirySeconds);

    }
}
