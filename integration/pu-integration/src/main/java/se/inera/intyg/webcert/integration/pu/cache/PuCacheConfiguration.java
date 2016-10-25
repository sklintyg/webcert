package se.inera.intyg.webcert.integration.pu.cache;

import org.apache.ignite.cache.spring.SpringCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

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
public class PuCacheConfiguration implements se.inera.intyg.common.cache.core.ConfigurableCache {

    public static final String PERSON_CACHE_NAME = "personCache";

    private static final String PU_CACHE_EXPIRY = "pu.cache.expiry";

    @Value("${" + PU_CACHE_EXPIRY + "}")
    private String personCacheExpirySeconds;

    @Autowired
    private SpringCacheManager cacheManager;

    @PostConstruct
    public void init() {
        Duration duration = buildDuration(personCacheExpirySeconds, PU_CACHE_EXPIRY);

        cacheManager.getDynamicCacheConfiguration().setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(duration));
        cacheManager.getCache(PERSON_CACHE_NAME);
    }

}
