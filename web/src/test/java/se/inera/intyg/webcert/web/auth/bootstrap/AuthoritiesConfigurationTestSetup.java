/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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
import se.inera.intyg.infra.security.authorities.CommonAuthoritiesResolver;
import se.inera.intyg.infra.security.authorities.bootstrap.AuthoritiesConfigurationLoader;

/**
 * Created by Magnus Ekstrand on 26/11/15.
 */
public class AuthoritiesConfigurationTestSetup {

    protected static final String CONFIGURATION_LOCATION = "AuthoritiesConfigurationLoaderTest/authorities-test.yaml";

    protected static final AuthoritiesConfigurationLoader CONFIGURATION_LOADER = new AuthoritiesConfigurationLoader(CONFIGURATION_LOCATION);
    protected static final CommonAuthoritiesResolver AUTHORITIES_RESOLVER = new CommonAuthoritiesResolver();

    @BeforeClass
    public static void setupAuthoritiesConfiguration() throws Exception {
        // Load configuration
        CONFIGURATION_LOADER.afterPropertiesSet();

        // Setup resolver class
        AUTHORITIES_RESOLVER.setConfigurationLoader(CONFIGURATION_LOADER);
    }

}
