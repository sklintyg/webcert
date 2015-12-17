/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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
