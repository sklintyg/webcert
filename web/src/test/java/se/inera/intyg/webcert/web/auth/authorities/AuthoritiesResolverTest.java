package se.inera.intyg.webcert.web.auth.authorities;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.intyg.common.integration.hsa.services.HsaPersonService;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationLoader;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class AuthoritiesResolverTest {

    private String configurationLocation = "AuthoritiesConfigurationLoaderTest/authorities-test.yaml";

    @Mock
    private HsaPersonService hsaPersonService;

    @Spy
    private AuthoritiesConfigurationLoader configurationLoader = new AuthoritiesConfigurationLoader(configurationLocation);

    @InjectMocks
    private AuthoritiesResolver authoritiesResolver = new AuthoritiesResolver();

    @Before
    public void setup() throws Exception {
        configurationLoader.afterPropertiesSet();
    }

    @Test
    public void lookupUserRoleWhenTitleIsDoctor() throws Exception {
        // given
        List<String> titles = Collections.singletonList("Läkare");
        // when
        Role role = authoritiesResolver.lookupUserRoleByLegitimeradeYrkesgrupper(titles);
        // then
        assertTrue(role.getName().equalsIgnoreCase(AuthoritiesConstants.ROLE_LAKARE));
    }

    @Test
    public void lookupUserRoleWhenMultipleTitlesAndOneIsDoctor() {
        // given
        List<String> titles = Arrays.asList("Läkare", "Barnmorska", "Sjuksköterska");
        // when
        Role role = authoritiesResolver.lookupUserRoleByLegitimeradeYrkesgrupper(titles);
        // then
        assertTrue(role.getName().equalsIgnoreCase(AuthoritiesConstants.ROLE_LAKARE));
    }

    @Test
    public void lookupUserRoleWhenMultipleTitlesAndNoDoctor() {
        // given
        List<String> titles = Arrays.asList("Barnmorska", "Sjuksköterska");
        // when
        Role userRole = authoritiesResolver.lookupUserRoleByLegitimeradeYrkesgrupper(titles);
        // then
        assertNull(userRole);
    }

    @Test
    public void lookupUserRoleWhenTitleCodeIs204010() {
        // given
        List<String> befattningsKoder = Collections.singletonList("204010");
        // when
        Role role = authoritiesResolver.lookupUserRoleByBefattningskod(befattningsKoder);
        // then
        assertTrue(role.getName().equalsIgnoreCase(AuthoritiesConstants.ROLE_LAKARE));
    }

    @Test
    public void lookupUserRoleWhenTitleCodeIsNot204010() {
        // given
        List<String> befattningsKoder = Arrays.asList("203090", "204090", "", null);
        // when
        Role role = authoritiesResolver.lookupUserRoleByBefattningskod(befattningsKoder);
        // then
        assertNull(role);
    }

    @Test
    public void lookupUserRoleByTitleCodeAndGroupPrescriptionCode() {
        // given
        List<String> befattningsKoder = Arrays.asList("204010", "203090", "204090");
        List<String> gruppforskrivarKoder = Arrays.asList("9300005", "9100009");

        Role[][] roleMatrix = new Role[3][2];

        // when
        for (int i = 0; i < befattningsKoder.size(); i++) {
            for (int j = 0; j < gruppforskrivarKoder.size(); j++) {
                Role role = authoritiesResolver.lookupUserRoleByBefattningskodAndGruppforskrivarkod(befattningsKoder.get(i), gruppforskrivarKoder.get(j));
                roleMatrix[i][j] = role;
                //System.err.println("[" + i + "," + j + "] " + (role == null ? "null" : role.getName()));
            }
        }

        // then

        /* Expected matrix:
            [0,0] null
            [0,1] null
            [1,0] LAKARE
            [1,1] null
            [2,0] null
            [2,1] LAKARE
         */

        for (int i = 0; i < befattningsKoder.size(); i++) {
            for (int j = 0; j < gruppforskrivarKoder.size(); j++) {
                if ((i == 0) && ((j == 0) || (j == 1))) {
                    assertNull(roleMatrix[i][j]);
                } else if ((i == 2) && (j == 0)) {
                    assertNull(roleMatrix[i][j]);
                } else if ((i == 1) && (j == 1)) {
                    assertNull(roleMatrix[i][j]);
                } else {
                    assertTrue(roleMatrix[i][j].getName().equalsIgnoreCase(AuthoritiesConstants.ROLE_LAKARE));
                }
            }
        }
    }
}
