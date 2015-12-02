package se.inera.intyg.webcert.web.auth.bootstrap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import se.inera.intyg.webcert.web.auth.authorities.AuthoritiesConfiguration;
import se.inera.intyg.webcert.web.auth.authorities.Privilege;
import se.inera.intyg.webcert.web.auth.authorities.Role;
import se.inera.intyg.webcert.web.auth.authorities.Title;
import se.inera.intyg.webcert.web.auth.authorities.TitleCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * The AuthoritiesDataLoader is not very well suited for unit-testing, given that it has a single entry-point and then
 * creates roles, privileges and titleCodes using three different repositories with interdependent data.
 *
 * A future refactoring may be to extract role, privilege and titleCode creation to separate components which then could
 * expose a domain-specific API much more suitable for unit testing than the AuthoritiesDataLoader.
 *
 * Created by eriklupander on 2015-10-19.
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthoritiesConfigurationLoaderTest {

    private static final String authoritiesConfigurationFile = "AuthoritiesConfigurationLoaderTest/authorities-test.yaml";
    private static final String authoritiesConfigurationOutputFile = "AuthoritiesConfigurationLoaderTest/authorities-output.txt";

    @InjectMocks
    AuthoritiesConfigurationLoader loader = new AuthoritiesConfigurationLoader(authoritiesConfigurationFile);

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
        AuthoritiesConfiguration configuration = loader.getConfiguration();

        assertTrue(configuration.getRoles().size() == 4);
        assertTrue(configuration.getPrivileges().size() == 3);
        assertTrue(configuration.getTitles().size() == 2);
        assertTrue(configuration.getTitleCodes().size() == 4);

        // Assert that lists are of specific types
        try {
            List<Role> roles = (List<Role>) configuration.getRoles();
            List<Privilege> privileges = (List<Privilege>) configuration.getPrivileges();
            List<Title> titles = (List<Title>) configuration.getTitles();
            List<TitleCode> titleCodes = (List<TitleCode>) configuration.getTitleCodes();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void loadConfigurationAndAssertString() {
        AuthoritiesConfiguration configuration = loader.getConfiguration();

        String actual = configuration.toString().replaceAll("\\s","");
        String expected = "";

        try {
            Resource resource = getResource(authoritiesConfigurationOutputFile);
            expected = new String(Files.readAllBytes(Paths.get(resource.getURI()))).replaceAll("\\s","");
        } catch (IOException e) {
            fail(e.getMessage());
        }

        assertEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void loadConfigurationWithBadLocation() {
        AuthoritiesConfigurationLoader loader = new AuthoritiesConfigurationLoader(null);
    }


    // ~ Private scope
    // ======================================================================================================

    private Resource getResource(String location) {
        PathMatchingResourcePatternResolver r = new PathMatchingResourcePatternResolver();
        return r.getResource(location);
    }

}
