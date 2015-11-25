package se.inera.intyg.webcert.web.service.feature;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import se.inera.intyg.common.support.modules.support.feature.ModuleFeaturesFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.*;

public class WebcertFeatureTest {

    @Test
    public void testAllWebcertFeaturesAreSetInWebcertPropertiesFile() throws Exception {
        //Given
        final String propFile = "/features.properties";

        //When
        Resource resource = new ClassPathResource(propFile);
        final Properties properties = PropertiesLoaderUtils.loadProperties(resource);

        //Then
        final WebcertFeature[] values = WebcertFeature.values();
        assertEquals("All features should be defined in webcert properties file", values.length, properties.size());
        for (WebcertFeature feature : values) {
            assertTrue(properties.containsKey(feature.getName()));
        }
    }

}
