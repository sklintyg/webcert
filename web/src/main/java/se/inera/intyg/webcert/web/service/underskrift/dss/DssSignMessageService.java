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

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilderFactory;
import oasis.names.tc.dss._1_0.core.schema.SignRequest;
import org.apache.commons.io.IOUtils;
import org.apache.xml.security.algorithms.MessageDigestAlgorithm;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.XMLUtils;
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
import se.inera.intyg.infra.xmldsig.service.XMLDSigService;

@Service
public class DssSignMessageService {

    private static final Logger LOG = LoggerFactory.getLogger(DssSignMessageService.class);

    @Value("${dss.client.keystore.alias}")
    private String keystoreAlias;

    @Value("${dss.client.keystore.password}")
    private String keystorePassword;

    @Value("${dss.client.keystore.file}")
    private Resource keystoreFile;

    private XMLDSigService infraXMLDSigService;


    @Autowired
    public DssSignMessageService(XMLDSigService infraXMLDSigService) {
        this.infraXMLDSigService = infraXMLDSigService;
    }

    @PostConstruct
    public void init() {
        org.apache.xml.security.Init.init();
    }

    public String signSignRequest(SignRequest signRequest) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(keystoreFile.getInputStream(), keystorePassword.toCharArray());
            PrivateKey privateKey =
                (PrivateKey) ks.getKey(keystoreAlias, keystorePassword.toCharArray());
            X509Certificate cert =
                (X509Certificate) ks.getCertificate(keystoreAlias);

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);

            Document doc = dbf.newDocumentBuilder().parse(IOUtils.toInputStream(toXmlString(signRequest), StandardCharsets.UTF_8));

            XMLSignature sig =
                new XMLSignature(doc, "", XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256);

            NodeList optionalInputsList = doc.getElementsByTagName("OptionalInputs");
            Node optionalItemNode = optionalInputsList.item(0);
            optionalItemNode.appendChild(sig.getElement());

            Transforms transforms = new Transforms(doc);
            transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
            sig.addDocument("", transforms, MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA256);
            sig.addKeyInfo(cert);
            sig.addKeyInfo(cert.getPublicKey());
            sig.sign(privateKey);

            XMLUtils.outputDOM(doc, os);

            return os.toString();

        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public ValidationResponse validateSignResponseSignature(String signResponseXmlString) {
        return infraXMLDSigService.validateSignatureValidity(signResponseXmlString, true);
    }

    private String toXmlString(SignRequest signRequest) {
        Jaxb2Marshaller marshaller = getJaxb2Marshaller();
        var stringResult = new StringResult();
        marshaller.marshal(signRequest, stringResult);

        return stringResult.toString();
    }

    private Jaxb2Marshaller getJaxb2Marshaller() {
        // TODO Make inte fields
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        String[] packages = {"oasis.names.tc", "org.w3._2000._09.xmldsig_", "org.w3._2001._04.xmlenc_", "se.elegnamnden.id.csig"};
        marshaller.setPackagesToScan(packages);

        marshaller.setMarshallerProperties(new HashMap<String, Object>() {
            {
                put(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, false);
                put(javax.xml.bind.Marshaller.JAXB_ENCODING, "UTF-8");
            }
        });
        return marshaller;
    }


}
