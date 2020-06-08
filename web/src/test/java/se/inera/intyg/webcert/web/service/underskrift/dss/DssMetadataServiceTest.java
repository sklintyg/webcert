/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.underskrift.dss;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.xml.ConfigurationException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

public class DssMetadataServiceTest {

    @BeforeClass
    public static void init() throws ConfigurationException {
        DefaultBootstrap.bootstrap();
    }

    @Test
    public void getDssActionUrl_useMetadata() {

        DssMetadataService service = new DssMetadataService(Configuration.getParserPool());
        ReflectionTestUtils.setField(service, "actionUrlProperty", "");
        ReflectionTestUtils.setField(service, "dssServiceMetadataEntityId",
            "https://esign.v2.st.signatureservice.se/signservice-frontend/metadata/4321a111111");
        ReflectionTestUtils.setField(service, "dssServiceMetadataResource", new ClassPathResource("dss/dss-valid-metadata.xml"));

        service.initDssMetadata();

        assertEquals("https://esign.v2.st.signatureservice.se/signservice-frontend/consumeassertion", service.getDssActionUrl());

    }

    @Test
    public void getDssActionUrl_useProperty() {

        String actionUrlProperty = "http://test.me";

        DssMetadataService service = new DssMetadataService(Configuration.getParserPool());
        ReflectionTestUtils.setField(service, "actionUrlProperty", actionUrlProperty);
        ReflectionTestUtils.setField(service, "dssServiceMetadataEntityId",
            "https://esign.v2.st.signatureservice.se/signservice-frontend/metadata/4321a111111");
        ReflectionTestUtils.setField(service, "dssServiceMetadataResource", new ClassPathResource("dss/dss-valid-metadata.xml"));

        service.initDssMetadata();

        assertEquals(actionUrlProperty, service.getDssActionUrl());

    }

    @Test
    public void getDssCertificate() {

    }

    @Test
    public void getClientMetadataAsString() {
        DssMetadataService service = new DssMetadataService(Configuration.getParserPool());

        ReflectionTestUtils.setField(service, "keystoreAlias", "localhost");
        ReflectionTestUtils.setField(service, "keystorePassword", "password");
        ReflectionTestUtils.setField(service, "keystoreFile", new ClassPathResource("dss/localhost.p12"));
        ReflectionTestUtils.setField(service, "webcertHostUrl", "https://wc.localtest.me:9088");

        ReflectionTestUtils.setField(service, "organizationName", "Inera AB");
        ReflectionTestUtils.setField(service, "organizationDisplayName", "Webcert");
        ReflectionTestUtils.setField(service, "organizationUrl", "https://inera.se");
        ReflectionTestUtils.setField(service, "organizationEmail", "teknik.intyg@inera.se");

        service.initClientKeyManager();
        service.initClientMetadata();

        assertNotNull(service.getClientMetadataAsString());
    }
}