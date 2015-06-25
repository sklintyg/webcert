package se.inera.auth.eleg;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.intyg.webcert.integration.pp.services.PPService;

/**
 * Created by eriklupander on 2015-06-25.
 */
@RunWith(MockitoJUnitRunner.class)
public class ElegWebCertUserDetailsServiceTest {

    @Mock
    private PPService ppService;

    @InjectMocks
    private ElegWebCertUserDetailsService testee;

    @Test
    public void testSuccessfulLogin() {
        // TODO add
    }

}
