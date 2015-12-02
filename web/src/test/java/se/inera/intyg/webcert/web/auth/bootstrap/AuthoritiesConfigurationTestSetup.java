package se.inera.intyg.webcert.web.auth.bootstrap;

import org.junit.BeforeClass;
import se.inera.intyg.webcert.web.auth.authorities.AuthoritiesResolver;

/**
 * Created by Magnus Ekstrand on 26/11/15.
 */
public class AuthoritiesConfigurationTestSetup {

    protected static final String CONFIGURATION_LOCATION = "AuthoritiesConfigurationLoaderTest/authorities-test.yaml";

    protected static final AuthoritiesConfigurationLoader CONFIGURATION_LOADER = new AuthoritiesConfigurationLoader(CONFIGURATION_LOCATION);;

    protected static final AuthoritiesResolver AUTHORITIES_RESOLVER = new AuthoritiesResolver();

    @BeforeClass
    public static void setupAuthoritiesConfiguration() throws Exception {
        // Load configuration
        CONFIGURATION_LOADER.afterPropertiesSet();

        // Setup resolver class
        AUTHORITIES_RESOLVER.setConfigurationLoader(CONFIGURATION_LOADER);
    }

}
