package se.inera.webcert.service.intyg.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.Contains;

import se.inera.certificate.integration.json.CustomObjectMapper;

public class SendIntygConfigurationManagerTest {
        
    private SendIntygConfigurationManagerImpl configurationManager;
    
    @Before
    public void setup() {
        configurationManager = new SendIntygConfigurationManagerImpl();
        configurationManager.setObjectMapper(new CustomObjectMapper());
    }
    
    @Test
    public void testMarshall() {
        
        String res = configurationManager.createAndMarshallSendConfig("FK", true);
        assertNotNull(res);
    }
    
    @Test
    public void testUnmarshallWithConsent() {
        
        String configAsJson = "{\"recipient\":\"FK\",\"patientConsent\":true}";
        
        SendIntygConfiguration config = configurationManager.unmarshallSendConfig(configAsJson);
        
        assertNotNull(config);
        assertEquals("FK", config.getRecipient());
        assertTrue(config.isPatientConsent());
        assertThat(config.getPatientConsentMessage(), containsString("mottagare FK med "));
    }
    
    @Test
    public void testUnmarshallWithoutConsent() {
        
        String configAsJson = "{\"recipient\":\"TS\",\"patientConsent\":false}";
        
        SendIntygConfiguration config = configurationManager.unmarshallSendConfig(configAsJson);
        
        assertNotNull(config);
        assertEquals("TS", config.getRecipient());
        assertFalse(config.isPatientConsent());
        assertThat(config.getPatientConsentMessage(), containsString("mottagare TS utan "));
    }
}
