/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import net.shibboleth.utilities.java.support.xml.ParserPool;
import org.apache.commons.io.IOUtils;
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
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import se.inera.intyg.infra.xmldsig.model.ValidationResponse;
import se.inera.intyg.infra.xmldsig.model.ValidationResult;
import se.inera.intyg.webcert.dss.xsd.dsscore.SignRequest;
import se.inera.intyg.webcert.dss.xsd.dssext.SignRequestExtensionType;

@ExtendWith(MockitoExtension.class)
public class DssSignMessageServiceTest {

    private DssMetadataService dssMetadataService;

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
        dssMetadataService = new DssMetadataService();
    }

    @Test
    void signSignRequest() throws IOException, XPathExpressionException {
        ReflectionTestUtils.setField(dssMetadataService, "dssServiceMetadataEntityId",
            "http://localhost:8020/api/signature/signservice/v1/metadata");
        ReflectionTestUtils.setField(dssMetadataService, "dssServiceMetadataResource", new ClassPathResource("dss/dss_valid_metadata.xml"));
        ReflectionTestUtils.setField(dssMetadataService, "keystorePassword", "password");
        dssMetadataService.initDssMetadata();

        DssSignMessageService service = new DssSignMessageService(dssMetadataService);

        ReflectionTestUtils.setField(service, "keystoreAlias", "localhost");
        ReflectionTestUtils.setField(service, "keystorePassword", "password");
        ReflectionTestUtils.setField(service, "keystoreFile", new ClassPathResource("dss/localhost.p12"));
        service.init();

        String signedSignRequest = service.signSignRequest(getSignRequest("dss/unsigned_signRequest.xml"));

        assertNotNull(signedSignRequest);

        // Signature should be in the correct node
        XPathFactory factory = XPathFactory.newInstance();
        XPath path = factory.newXPath();
        Node node = (Node) path
            .evaluate("//*[local-name()='OptionalInputs']/*[local-name()='Signature']", convertStringToXMLDocument(signedSignRequest),
                XPathConstants.NODE);

        assertNotNull(node);

        ValidationResponse validationResponse = service.validateDssMessageSignature(signedSignRequest);
        assertEquals(ValidationResult.OK, validationResponse.getSignatureValid(), "Signature");
        assertEquals(ValidationResult.OK, validationResponse.getReferencesValid(), "Reference");

        // Use this block if you need a new valid signed signRequest.
        // Copy paste from System.out doesn't always work due to formatting issues

        /*try (final var writer =
            new OutputStreamWriter(new FileOutputStream("/temp/signed_valid_signRequest.xml"), StandardCharsets.UTF_8)) {
            writer.write(signedSignRequest);
        }*/

    }

    @Test
    void validateSignResponseSignature_valid() {
        ReflectionTestUtils.setField(dssMetadataService, "dssServiceMetadataEntityId",
            "http://localhost:8020/api/signature/signservice/v1/metadata");
        ReflectionTestUtils.setField(dssMetadataService, "dssServiceMetadataResource", new ClassPathResource("dss/dss_valid_metadata.xml"));
        ReflectionTestUtils.setField(dssMetadataService, "keystorePassword", "password");
        dssMetadataService.initDssMetadata();

        DssSignMessageService service = new DssSignMessageService(dssMetadataService);

        ValidationResponse validationResponse = service
            .validateDssMessageSignature(getSignRequestString("dss/signed_valid_signRequest.xml"));
        assertEquals(ValidationResult.OK, validationResponse.getSignatureValid(), "Signature");
        assertEquals(ValidationResult.OK, validationResponse.getReferencesValid(), "Reference");

    }

    @Test
    void validateSignResponseSignature_invalid() {
        ReflectionTestUtils.setField(dssMetadataService, "dssServiceMetadataEntityId",
            "http://localhost:8020/api/signature/signservice/v1/metadata");
        ReflectionTestUtils.setField(dssMetadataService, "dssServiceMetadataResource", new ClassPathResource("dss/dss_valid_metadata.xml"));
        ReflectionTestUtils.setField(dssMetadataService, "keystorePassword", "password");
        dssMetadataService.initDssMetadata();

        DssSignMessageService service = new DssSignMessageService(dssMetadataService);

        ValidationResponse validationResponse = service
            .validateDssMessageSignature(getSignRequestString("dss/signed_invalid_signRequest.xml"));
        assertEquals(ValidationResult.INVALID, validationResponse.getSignatureValid(), "Signature");
        assertEquals(ValidationResult.INVALID, validationResponse.getReferencesValid(), "Reference");

    }

    @Test
    void validateSignResponseSignature_certMissingInKeyStore() {
        ReflectionTestUtils.setField(dssMetadataService, "dssServiceMetadataEntityId",
            "http://localhost:8020/api/signature/signservice/v1/metadata");
        ReflectionTestUtils.setField(dssMetadataService, "dssServiceMetadataResource", new ClassPathResource("dss/dss_valid_metadata.xml"));
        ReflectionTestUtils.setField(dssMetadataService, "keystorePassword", "password");
        dssMetadataService.initDssMetadata();

        DssSignMessageService service = new DssSignMessageService(dssMetadataService);

        ValidationResponse validationResponse = service
            .validateDssMessageSignature(getSignRequestString("dss/signed_invalidcert_signRequest.xml"));
        assertEquals(ValidationResult.INVALID, validationResponse.getSignatureValid(), "Signature");
        assertEquals(ValidationResult.INVALID, validationResponse.getReferencesValid(), "Reference");

    }

    private String getSignRequestString(String resource) {
        try {
            var xmlAsString = IOUtils.toString(new ClassPathResource(resource).getInputStream(), StandardCharsets.UTF_8);
            return xmlAsString;
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private SignRequest getSignRequest(String resource) {
        try {
            JAXBContext jaxbContext = JAXBContext
                .newInstance(SignRequest.class, SignRequestExtensionType.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return unmarshaller.unmarshal(new StreamSource(new ClassPathResource(resource).getInputStream()),
                SignRequest.class).getValue();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private Document convertStringToXMLDocument(String xmlString) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        try {
            return dbf.newDocumentBuilder().parse(new InputSource(new StringReader(xmlString)));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
