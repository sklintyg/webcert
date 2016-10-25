package se.inera.intyg.webcert.integration.pu.cache;

import org.apache.ignite.cache.spring.SpringCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;

/**
 * While the cacheManager.getCache(...) isn't strictly necessary for creating the cache used by
 * {@link se.inera.intyg.webcert.integration.pu.services.PUServiceImpl}, this class provides us with the capability
 * of configuring individual caches based on the current state of the
 * {@link org.apache.ignite.cache.spring.SpringCacheManager#dynamicCacheCfg}
 *
 * Created by eriklupander on 2016-10-20.
 */
@Component
public class PuCacheConfiguration {

    public static final String PERSON_CACHE_NAME = "personCache";

    private Duration defaultPersonCacheExpiry = Duration.ONE_HOUR;

    @Autowired
    private SpringCacheManager cacheManager;

    @PostConstruct
    public void init() {
        cacheManager.getDynamicCacheConfiguration().setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(defaultPersonCacheExpiry));
        cacheManager.getCache(PERSON_CACHE_NAME);
    }

}
