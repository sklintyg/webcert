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
package se.inera.intyg.webcert.web.auth.bootstrap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import se.inera.intyg.infra.security.authorities.AuthoritiesConfiguration;
import se.inera.intyg.infra.security.authorities.bootstrap.SecurityConfigurationLoader;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.RequestOrigin;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.infra.security.common.model.Title;
import se.inera.intyg.infra.security.common.model.TitleCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * The AuthoritiesDataLoader is not very well suited for unit-testing, given that it has a single entry-point and then
 * creates roles, privileges and titleCodes using three different repositories with interdependent data.
 * <p>
 * A future refactoring may be to extract role, privilege and titleCode creation to separate components which then could
 * expose a domain-specific API much more suitable for unit testing than the AuthoritiesDataLoader.
 * <p>
 * Created by eriklupander on 2015-10-19.
 */
@RunWith(MockitoJUnitRunner.class)
public class SecurityConfigurationLoaderTest {

    private static final String authoritiesConfigurationFile = "AuthoritiesConfigurationLoaderTest/authorities-test.yaml";
    private static final String featuresConfigurationFile = "AuthoritiesConfigurationLoaderTest/features-test.yaml";
    private static final String authoritiesConfigurationOutputFile = "AuthoritiesConfigurationLoaderTest/authorities-output.txt";

    @InjectMocks
    private SecurityConfigurationLoader loader = new SecurityConfigurationLoader(authoritiesConfigurationFile, featuresConfigurationFile);

    @Before
    public void setupAuthoritiesConfiguration() {
        // When
        try {
            loader.afterPropertiesSet();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void loadConfigurationAndAssertTypeOfObjects() {
        AuthoritiesConfiguration configuration = loader.getAuthoritiesConfiguration();

        assertEquals(4,configuration.getRequestOrigins().size());
        assertEquals(9,configuration.getPrivileges().size());
        assertEquals(4,configuration.getRoles().size());
        assertEquals(2,configuration.getTitles().size());
        assertEquals(4,configuration.getTitleCodes().size());

        // Assert that lists are of specific types
        try {
            List<RequestOrigin> requestOrigins = (List<RequestOrigin>) configuration.getRequestOrigins();
            List<Privilege> privileges = (List<Privilege>) configuration.getPrivileges();
            List<Role> roles = (List<Role>) configuration.getRoles();
            List<Title> titles = (List<Title>) configuration.getTitles();
            List<TitleCode> titleCodes = (List<TitleCode>) configuration.getTitleCodes();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    // @Test    // Temporarily disabling this test, it acts weird on OpenShift.
    public void loadConfigurationAndAssertString() {
        AuthoritiesConfiguration configuration = loader.getAuthoritiesConfiguration();

        String actual = configuration.toString().replaceAll("\\s", "").trim();
        String expected = "";

        try {
            Resource resource = getResource(authoritiesConfigurationOutputFile);
            expected = new String(Files.readAllBytes(Paths.get(resource.getURI()))).replaceAll("\\s", "").trim();
        } catch (IOException e) {
            fail(e.getMessage());
        }

        assertEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void loadConfigurationWithBadLocation() {
        new SecurityConfigurationLoader(null, null);
    }

    // ~ Private scope
    // ======================================================================================================

    private Resource getResource(String location) {
        PathMatchingResourcePatternResolver r = new PathMatchingResourcePatternResolver();
        return r.getResource(location);
    }

}
