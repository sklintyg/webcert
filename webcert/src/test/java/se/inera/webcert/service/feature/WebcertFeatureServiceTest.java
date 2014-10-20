package se.inera.webcert.service.feature;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class WebcertFeatureServiceTest {

    private WebcertFeatureServiceImpl featureService;

    @Before
    public void setup() {
        Properties props = new Properties();
        props.setProperty(Features.HANTERA_INTYGSUTKAST.getName(), "true");
        props.setProperty(Features.HANTERA_FRAGOR.getName(), "true");

        featureService = new WebcertFeatureServiceImpl();
        featureService.setFeatures(props);
        featureService.initFeaturesMap();
    }

    @Test
    public void testFeatureMapIsNotNull() {
        assertNotNull(featureService.getFeaturesMap());
    }

    @Test
    public void testIsFeatureActive() {
        assertTrue(featureService.isFeatureActive(Features.HANTERA_INTYGSUTKAST.getName()));
        assertFalse(featureService.isFeatureActive(Features.KOPIERA_INTYG.getName()));
    }
    
    @Test
    public void testGetActiveFeatures() {
        
        Set<String> res = featureService.getActiveFeatures();
        
        assertThat(res, contains("hanteraFragor", "hanteraIntygsutkast"));
    }

}
