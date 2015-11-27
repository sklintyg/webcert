package se.inera.intyg.webcert.web.service.intyg.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.containsString;

import org.junit.Before;
import org.junit.Test;

import se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper;

public class IntygServiceConfigurationManagerTest {

    private IntygServiceConfigurationManagerImpl configurationManager;

    @Before
    public void setup() {
        configurationManager = new IntygServiceConfigurationManagerImpl();
        configurationManager.setObjectMapper(new CustomObjectMapper());
    }

    @Test
    public void testUnmarshallForSendIntygWithConsent() {

        String configAsJson = "{\"recipient\":\"FK\",\"patientConsent\":true}";

        SendIntygConfiguration config = configurationManager.unmarshallConfig(configAsJson, SendIntygConfiguration.class);

        assertNotNull(config);
        assertEquals("FK", config.getRecipient());
        assertTrue(config.isPatientConsent());
        assertThat(config.getPatientConsentMessage(), containsString("mottagare FK med "));
    }

    @Test
    public void testUnmarshallForSendIntygWithoutConsent() {

        String configAsJson = "{\"recipient\":\"TS\",\"patientConsent\":false}";

        SendIntygConfiguration config = configurationManager.unmarshallConfig(configAsJson, SendIntygConfiguration.class);

        assertNotNull(config);
        assertEquals("TS", config.getRecipient());
        assertFalse(config.isPatientConsent());
        assertThat(config.getPatientConsentMessage(), containsString("mottagare TS utan "));
    }

}
