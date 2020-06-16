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

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import javax.xml.crypto.KeySelector;
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
        ReflectionTestUtils.setField(service, "dssServiceMetadataEntityId",
            "http://localhost:9088/api/signature/signservice/v1/metadata");
        ReflectionTestUtils.setField(service, "dssServiceMetadataResource", new ClassPathResource("dss/dss_valid_metadata.xml"));
        ReflectionTestUtils.setField(service, "keystorePassword", "password");

        service.initDssMetadata();
        assertEquals("http://localhost:9088/api/signature/signservice/v1/response", service.getDssActionUrl());

        ReflectionTestUtils.setField(service, "actionUrlProperty", "");
        assertEquals("http://localhost:9088/api/signature/signservice/v1/response", service.getDssActionUrl());

    }

    @Test
    public void getDssActionUrl_useProperty() {

        String actionUrlProperty = "http://test.me";

        DssMetadataService service = new DssMetadataService(Configuration.getParserPool());
        ReflectionTestUtils.setField(service, "actionUrlProperty", actionUrlProperty);
        ReflectionTestUtils.setField(service, "dssServiceMetadataEntityId",
            "http://localhost:9088/api/signature/signservice/v1/metadata");
        ReflectionTestUtils.setField(service, "dssServiceMetadataResource", new ClassPathResource("dss/dss_valid_metadata.xml"));
        ReflectionTestUtils.setField(service, "keystorePassword", "password");

        service.initDssMetadata();
        assertEquals(actionUrlProperty, service.getDssActionUrl());

    }

    @Test
    public void getDssKeyStore() throws KeyStoreException {
        DssMetadataService service = new DssMetadataService(Configuration.getParserPool());
        ReflectionTestUtils.setField(service, "keystorePassword", "password");
        ReflectionTestUtils.setField(service, "dssServiceMetadataEntityId",
            "http://localhost:9088/api/signature/signservice/v1/metadata");
        ReflectionTestUtils.setField(service, "dssServiceMetadataResource", new ClassPathResource("dss/dss_valid_metadata.xml"));

        service.initDssMetadata();
        KeyStore dssKeyStore = service.getDssKeyStore();
        Certificate dss0 = dssKeyStore.getCertificate("dss0");

        assertNotNull(dss0);
        assertEquals("Certificate Type", "X.509", dss0.getType());
    }

    @Test
    public void getDssKeySelector() throws KeyStoreException {
        DssMetadataService service = new DssMetadataService(Configuration.getParserPool());
        ReflectionTestUtils.setField(service, "keystorePassword", "password");
        ReflectionTestUtils.setField(service, "dssServiceMetadataEntityId",
            "http://localhost:9088/api/signature/signservice/v1/metadata");
        ReflectionTestUtils.setField(service, "dssServiceMetadataResource", new ClassPathResource("dss/dss_valid_metadata.xml"));

        service.initDssMetadata();
        KeySelector dssKeySelector = service.getDssKeySelector();
        assertNotNull(dssKeySelector);

    }

    @Test
    public void getClientMetadataAsString() {
        DssMetadataService service = new DssMetadataService(Configuration.getParserPool());

        ReflectionTestUtils.setField(service, "keystoreAlias", "localhost");
        ReflectionTestUtils.setField(service, "keystorePassword", "password");
        ReflectionTestUtils.setField(service, "keystoreFile", new ClassPathResource("dss/localhost.p12"));
        ReflectionTestUtils.setField(service, "webcertHostUrl", "http://localhost:9088");

        ReflectionTestUtils.setField(service, "organizationName", "Inera AB");
        ReflectionTestUtils.setField(service, "organizationDisplayName", "Webcert");
        ReflectionTestUtils.setField(service, "organizationUrl", "https://inera.se");
        ReflectionTestUtils.setField(service, "organizationEmail", "teknik.intyg@inera.se");

        service.initClientMetadata();

        String clientMetadataAsString = service.getClientMetadataAsString();
        assertNotNull(clientMetadataAsString);

        // Use this block if you need a new valid metadata xml
        // Copy paste from System.out doesn't always work due to formatting issues

/*        try (OutputStreamWriter writer =
            new OutputStreamWriter(new FileOutputStream(new File("/temp/localhost_sign_md.xml")), StandardCharsets.UTF_8)) {
            writer.write(clientMetadataAsString);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/
    }
}