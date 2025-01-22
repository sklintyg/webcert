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

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.io.IOUtils;
import org.apache.xml.security.algorithms.MessageDigestAlgorithm;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.transforms.Transforms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;
import org.springframework.xml.transform.StringResult;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import se.inera.intyg.infra.xmldsig.model.ValidationResponse;
import se.inera.intyg.infra.xmldsig.model.ValidationResult;
import se.inera.intyg.webcert.dss.xsd.dsscore.SignRequest;

@Service
public class DssSignMessageService {

    private static final Logger LOG = LoggerFactory.getLogger(DssSignMessageService.class);
    public static final String DEFAULT_SIGN_ALGORITHM = XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256;

    @Value("${dss.client.keystore.alias}")
    private String keystoreAlias;

    @Value("${dss.client.keystore.password}")
    private String keystorePassword;

    @Value("${dss.client.keystore.file}")
    private Resource keystoreFile;

    private DssMetadataService dssMetadataService;

    private Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

    private KeyStore clientKeyStore;

    @Autowired
    public DssSignMessageService(DssMetadataService dssMetadataService) {
        this.dssMetadataService = dssMetadataService;

        String[] packages = {"se.inera.intyg.webcert.dss.xsd"};
        marshaller.setPackagesToScan(packages);
        marshaller.setMarshallerProperties(Map.of(
            jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, false,
            jakarta.xml.bind.Marshaller.JAXB_ENCODING, "UTF-8")
        );
    }

    @PostConstruct
    public void init() {
        org.apache.xml.security.Init.init();

        try {
            clientKeyStore = KeyStore.getInstance("JKS");
            clientKeyStore.load(keystoreFile.getInputStream(), keystorePassword.toCharArray());
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException exception) {
            throw new RuntimeException(exception);
        }
    }

    public String signSignRequest(SignRequest signRequest) {
        try {

            PrivateKey privateKey =
                (PrivateKey) clientKeyStore.getKey(keystoreAlias, keystorePassword.toCharArray());
            X509Certificate cert =
                (X509Certificate) clientKeyStore.getCertificate(keystoreAlias);

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);

            Document doc = dbf.newDocumentBuilder().parse(IOUtils.toInputStream(toXmlString(signRequest), StandardCharsets.UTF_8));

            XMLSignature sig =
                new XMLSignature(doc, "", DEFAULT_SIGN_ALGORITHM, Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

            NodeList optionalInputsList = doc.getElementsByTagNameNS("urn:oasis:names:tc:dss:1.0:core:schema", "OptionalInputs");
            Node optionalItemNode = optionalInputsList.item(0);
            optionalItemNode.appendChild(sig.getElement());

            Transforms transforms = new Transforms(doc);
            transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
            sig.addDocument("", transforms, MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA256);
            sig.addKeyInfo(cert);
            sig.sign(privateKey);

            return documentToString(doc);

        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public ValidationResponse validateDssMessageSignature(String signResponseXmlString) {
        return verifySignature(signResponseXmlString);
    }

    private String toXmlString(SignRequest signRequest) {
        var stringResult = new StringResult();
        marshaller.marshal(signRequest, stringResult);

        return stringResult.toString();
    }

    private String documentToString(Document document) {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer trans = tf.newTransformer();
            StringWriter sw = new StringWriter();
            trans.transform(new DOMSource(document), new StreamResult(sw));
            return sw.toString();
        } catch (TransformerException tEx) {
            tEx.printStackTrace();
        }
        return null;
    }

    private ValidationResponse verifySignature(String signedXml) {
        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        try {
            Document doc = dbf.newDocumentBuilder().parse(IOUtils.toInputStream(signedXml, Charset.forName("UTF-8")));
            NodeList nl = doc.getElementsByTagNameNS(javax.xml.crypto.dsig.XMLSignature.XMLNS, "Signature");
            if (nl.getLength() != 1) {
                throw new Exception("Cannot find exactly one Signature element");
            }
            return verifySignature(true, fac, nl.item(0));
        } catch (Exception e) {
            LOG.error("Caught {} validating signature. Msg: {}", e.getClass().getName(), e.getMessage());
            throw new IllegalArgumentException(e);
        }
    }

    private ValidationResponse verifySignature(boolean checkReferences, XMLSignatureFactory fac, Node node)
        throws MarshalException, XMLSignatureException {
        // Create a DOMValidateContext and specify a KeySelector
        // and document context.
        DOMValidateContext valContext = new DOMValidateContext(dssMetadataService.getDssKeySelector(), node);

        // Unmarshal the XMLSignature.
        javax.xml.crypto.dsig.XMLSignature sig = fac.unmarshalXMLSignature(valContext);

        try {
            if (checkReferences) {
                boolean result = sig.validate(valContext);
                return ValidationResponse.ValidationResponseBuilder.aValidationResponse()
                    .withSignatureValid(result ? ValidationResult.OK : ValidationResult.INVALID)
                    .withReferencesValid(result ? ValidationResult.OK : ValidationResult.INVALID)
                    .build();
            } else {
                boolean result = sig.getSignatureValue().validate(valContext);
                return ValidationResponse.ValidationResponseBuilder.aValidationResponse()
                    .withSignatureValid(result ? ValidationResult.OK : ValidationResult.INVALID)
                    .withReferencesValid(ValidationResult.NOT_CHCEKED)
                    .build();
            }
        } catch (XMLSignatureException xmlSignatureException) {
            // Check if keyselectoer failed to find a valid matching certificate
            // and treat this as an invalid signature
            var cause = xmlSignatureException.getCause();
            if (cause instanceof KeySelectorException) {
                LOG.warn("Invalid signature. Key in XML doesn't match metadata. {}", cause.getMessage());
                return ValidationResponse.ValidationResponseBuilder.aValidationResponse()
                    .withSignatureValid(ValidationResult.INVALID)
                    .withReferencesValid(ValidationResult.INVALID)
                    .build();
            } else {
                throw xmlSignatureException;
            }

        }
    }

}
