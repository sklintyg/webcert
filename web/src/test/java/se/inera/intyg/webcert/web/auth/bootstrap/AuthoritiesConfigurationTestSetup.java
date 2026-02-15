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
package se.inera.intyg.webcert.web.auth.bootstrap;

import org.junit.BeforeClass;
import org.junit.jupiter.api.BeforeAll;
import se.inera.intyg.infra.security.authorities.CommonAuthoritiesResolver;
import se.inera.intyg.infra.security.authorities.bootstrap.SecurityConfigurationLoader;

/**
 * Created by Magnus Ekstrand on 26/11/15.
 */
public abstract class AuthoritiesConfigurationTestSetup {

    public static final String AUTHORITIES_CONFIGURATION_LOCATION = "classpath:AuthoritiesConfigurationLoaderTest/authorities-test.yaml";
    public static final String FEATURES_CONFIGURATION_LOCATION = "classpath:AuthoritiesConfigurationLoaderTest/features-test.yaml";
    public static final Integer DEFAULT_MAX_ALIASES_FOR_COLLECTIONS = 300;

    protected static final SecurityConfigurationLoader CONFIGURATION_LOADER = new SecurityConfigurationLoader(
        AUTHORITIES_CONFIGURATION_LOCATION, FEATURES_CONFIGURATION_LOCATION, DEFAULT_MAX_ALIASES_FOR_COLLECTIONS);
    public static final CommonAuthoritiesResolver AUTHORITIES_RESOLVER = new CommonAuthoritiesResolver();

    @BeforeAll
    public static void setupAuthoritiesConfiguration() throws Exception {
        // Load configuration
        CONFIGURATION_LOADER.afterPropertiesSet();

        // Setup resolver class
        AUTHORITIES_RESOLVER.setConfigurationLoader(CONFIGURATION_LOADER);
    }

    @BeforeClass
    public static void setupForJunit4() throws Exception {
        // Load configuration
        CONFIGURATION_LOADER.afterPropertiesSet();

        // Setup resolver class
        AUTHORITIES_RESOLVER.setConfigurationLoader(CONFIGURATION_LOADER);
    }

}
