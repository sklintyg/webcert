package se.inera.webcert;

import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/webcert-config.xml" })
@ActiveProfiles({"dev"})
public class SpringContextTest {

    private static final String CREDENTIALS = "credentials.file";
    private static final String WEBCERT_CONFIG = "webcert.config.file";

    @BeforeClass
    public static void setProps() {
        //System.setProperty(CREDENTIALS, "foo");
        System.setProperty(WEBCERT_CONFIG, "classpath:/webcert-dev.properties");
    }
    
    @AfterClass
    public static void removeProps() {
        System.clearProperty(CREDENTIALS);
        System.clearProperty(WEBCERT_CONFIG);
    }
    
    @Test
    public void testContext() {
        assertTrue(true);
    }
    
}
