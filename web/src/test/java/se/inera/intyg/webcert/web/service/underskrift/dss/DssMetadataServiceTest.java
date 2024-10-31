/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.security.KeyStoreException;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import net.shibboleth.utilities.java.support.xml.ParserPool;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.core.config.ConfigurationService;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.config.XMLObjectProviderRegistry;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class DssMetadataServiceTest {

    private DssMetadataService service;

    @BeforeAll
    public static void init() throws InitializationException, ComponentInitializationException {
        final var registry = new XMLObjectProviderRegistry();
        ConfigurationService.register(XMLObjectProviderRegistry.class, registry);
        registry.setParserPool(getParserPool());
        InitializationService.initialize();
    }

    private static ParserPool getParserPool() throws ComponentInitializationException {
        final var parserPool = new BasicParserPool();
        parserPool.initialize();
        return parserPool;
    }

    @BeforeEach
    void setup() {
        service = new DssMetadataService();
    }

    @Test
    void getDssActionUrl_useMetadata() {
        ReflectionTestUtils.setField(service, "dssServiceMetadataResource", new ClassPathResource("dss/dss_valid_metadata.xml"));
        ReflectionTestUtils.setField(service, "keystorePassword", "password");
        ReflectionTestUtils.setField(service, "dssServiceMetadataEntityId",
            "http://localhost:8020/api/signature/signservice/v1/metadata");

        service.initDssMetadata();
        assertEquals("http://localhost:8020/api/signature/signservice/v1/response", service.getDssActionUrl());

        ReflectionTestUtils.setField(service, "actionUrlProperty", "");
        assertEquals("http://localhost:8020/api/signature/signservice/v1/response", service.getDssActionUrl());

    }

    @Test
    void getDssActionUrl_useProperty() {
        final var actionUrlProperty = "http://test.me";
        ReflectionTestUtils.setField(service, "actionUrlProperty", actionUrlProperty);
        ReflectionTestUtils.setField(service, "keystorePassword", "password");
        ReflectionTestUtils.setField(service, "dssServiceMetadataResource", new ClassPathResource("dss/dss_valid_metadata.xml"));
        ReflectionTestUtils.setField(service, "dssServiceMetadataEntityId",
            "http://localhost:8020/api/signature/signservice/v1/metadata");

        service.initDssMetadata();
        assertEquals(actionUrlProperty, service.getDssActionUrl());

    }

    @Test
    void getDssKeyStore() throws KeyStoreException {
        ReflectionTestUtils.setField(service, "dssServiceMetadataResource", new ClassPathResource("dss/dss_valid_metadata.xml"));
        ReflectionTestUtils.setField(service, "keystorePassword", "password");
        ReflectionTestUtils.setField(service, "dssServiceMetadataEntityId",
            "http://localhost:8020/api/signature/signservice/v1/metadata");

        service.initDssMetadata();

        final var dssKeyStore = service.getDssKeyStore();
        final var dss0 = dssKeyStore.getCertificate("dss0");
        assertNotNull(dss0);
        assertEquals("X.509", dss0.getType(), "Certificate Type");
    }

    @Test
    void getDssKeySelector() {
        ReflectionTestUtils.setField(service, "dssServiceMetadataResource", new ClassPathResource("dss/dss_valid_metadata.xml"));
        ReflectionTestUtils.setField(service, "keystorePassword", "password");
        ReflectionTestUtils.setField(service, "dssServiceMetadataEntityId",
            "http://localhost:8020/api/signature/signservice/v1/metadata");

        service.initDssMetadata();
        assertNotNull(service.getDssKeySelector());
    }

    @Test
    void getClientMetadataAsString() {
        ReflectionTestUtils.setField(service, "keystoreAlias", "localhost");
        ReflectionTestUtils.setField(service, "keystorePassword", "password");
        ReflectionTestUtils.setField(service, "keystoreFile", new ClassPathResource("dss/localhost.p12"));
        ReflectionTestUtils.setField(service, "dssClientEntityHostUrl", "https://wc.localtest.me");
        ReflectionTestUtils.setField(service, "dssClientResponseHostUrl", "https://other.se");
        ReflectionTestUtils.setField(service, "dssClientResponseHostUrl", "https://wc.localtest.me");
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