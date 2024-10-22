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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.xml.ConfigurationException;
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

@RunWith(MockitoJUnitRunner.class)
public class DssSignMessageServiceTest {

    public DssSignMessageServiceTest() {
    }

    @BeforeClass
    public static void init() throws ConfigurationException {
        org.apache.xml.security.Init.init();
        DefaultBootstrap.bootstrap();
    }

    @Test
    public void signSignRequest() throws IOException, XPathExpressionException {

        DssMetadataService dssMetadataService = new DssMetadataService(Configuration.getParserPool());
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
        assertEquals("Signature", ValidationResult.OK, validationResponse.getSignatureValid());
        assertEquals("Reference", ValidationResult.OK, validationResponse.getReferencesValid());

        // Use this block if you need a new valid signed signRequest.
        // Copy paste from System.out doesn't always work due to formatting issues

/*        try (OutputStreamWriter writer =
            new OutputStreamWriter(new FileOutputStream(new File("/temp/signed_valid_signRequest.xml")), StandardCharsets.UTF_8)) {
            writer.write(signedSignRequest);
        }*/

    }

    @Test
    public void validateSignResponseSignature_valid() {
        DssMetadataService dssMetadataService = new DssMetadataService(Configuration.getParserPool());
        ReflectionTestUtils.setField(dssMetadataService, "dssServiceMetadataEntityId",
            "http://localhost:8020/api/signature/signservice/v1/metadata");
        ReflectionTestUtils.setField(dssMetadataService, "dssServiceMetadataResource", new ClassPathResource("dss/dss_valid_metadata.xml"));
        ReflectionTestUtils.setField(dssMetadataService, "keystorePassword", "password");
        dssMetadataService.initDssMetadata();

        DssSignMessageService service = new DssSignMessageService(dssMetadataService);

        ValidationResponse validationResponse = service
            .validateDssMessageSignature(getSignRequestString("dss/signed_valid_signRequest.xml"));
        assertEquals("Signature", ValidationResult.OK, validationResponse.getSignatureValid());
        assertEquals("Reference", ValidationResult.OK, validationResponse.getReferencesValid());

    }

    @Test
    public void validateSignResponseSignature_invalid() {
        DssMetadataService dssMetadataService = new DssMetadataService(Configuration.getParserPool());
        ReflectionTestUtils.setField(dssMetadataService, "dssServiceMetadataEntityId",
            "http://localhost:8020/api/signature/signservice/v1/metadata");
        ReflectionTestUtils.setField(dssMetadataService, "dssServiceMetadataResource", new ClassPathResource("dss/dss_valid_metadata.xml"));
        ReflectionTestUtils.setField(dssMetadataService, "keystorePassword", "password");
        dssMetadataService.initDssMetadata();

        DssSignMessageService service = new DssSignMessageService(dssMetadataService);

        ValidationResponse validationResponse = service
            .validateDssMessageSignature(getSignRequestString("dss/signed_invalid_signRequest.xml"));
        assertEquals("Signature", ValidationResult.INVALID, validationResponse.getSignatureValid());
        assertEquals("Reference", ValidationResult.INVALID, validationResponse.getReferencesValid());

    }

    @Test
    public void validateSignResponseSignature_certMissingInKeyStore() {
        DssMetadataService dssMetadataService = new DssMetadataService(Configuration.getParserPool());
        ReflectionTestUtils.setField(dssMetadataService, "dssServiceMetadataEntityId",
            "http://localhost:8020/api/signature/signservice/v1/metadata");
        ReflectionTestUtils.setField(dssMetadataService, "dssServiceMetadataResource", new ClassPathResource("dss/dss_valid_metadata.xml"));
        ReflectionTestUtils.setField(dssMetadataService, "keystorePassword", "password");
        dssMetadataService.initDssMetadata();

        DssSignMessageService service = new DssSignMessageService(dssMetadataService);

        ValidationResponse validationResponse = service
            .validateDssMessageSignature(getSignRequestString("dss/signed_invalidcert_signRequest.xml"));
        assertEquals("Signature", ValidationResult.INVALID, validationResponse.getSignatureValid());
        assertEquals("Reference", ValidationResult.INVALID, validationResponse.getReferencesValid());

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