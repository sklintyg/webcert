package se.inera.intyg.webcert.web.auth.authorities;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.intyg.common.integration.hsa.services.HsaPersonService;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationLoader;

/**
 * @author Magnus Ekstrand on 2016-05-13.
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthoritiesHelperTest {

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
    public void testGetIntygstyperForPrivilege() throws Exception {

    }



}