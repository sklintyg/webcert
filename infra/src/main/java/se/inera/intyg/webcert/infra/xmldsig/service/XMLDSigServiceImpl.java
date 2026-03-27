/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.infra.xmldsig.service;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3._2000._09.xmldsig_.KeyInfoType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import se.inera.intyg.webcert.infra.xmldsig.factory.PartialSignatureFactory;
import se.inera.intyg.webcert.infra.xmldsig.model.CertificateInfo;
import se.inera.intyg.webcert.infra.xmldsig.model.ValidationResponse;
import se.inera.intyg.webcert.infra.xmldsig.model.ValidationResult;
import se.inera.intyg.webcert.infra.xmldsig.util.X509KeySelector;

/**
 * Provides Intyg-specific functionality for preparing XMLDSig signatures.
 *
 * @author eriklupander
 */
@Service
public class XMLDSigServiceImpl implements XMLDSigService {

  private static final Logger LOG = LoggerFactory.getLogger(XMLDSigServiceImpl.class);
  private static final String CANONICALIZER_ALGORITHM = CanonicalizationMethod.EXCLUSIVE;
  private static final String DIGEST_ALGORITHM = "SHA-256";
  public static final String BEGIN_CERTIFICATE_STRING = "-----BEGIN CERTIFICATE-----\n";
  public static final String END_CERTIFICATE_STRING = "-----END CERTIFICATE-----";

  @PostConstruct
  public void init() {
    org.apache.xml.security.Init.init();
  }

  /**
   * Builds a <KeyInfo/> element with the supplied certificate put into a child X509Certificate
   * element.
   */
  @Override
  public KeyInfoType buildKeyInfoForCertificate(String certificate) {
    return PartialSignatureFactory.buildKeyInfo(certificate);
  }

  @Override
  public ValidationResponse validateSignatureValidity(
      String xmlWithSignedIntyg, boolean checkReferences) {
    XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);

    try {
      Document doc =
          dbf.newDocumentBuilder()
              .parse(IOUtils.toInputStream(xmlWithSignedIntyg, Charset.forName("UTF-8")));
      NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
      if (nl.getLength() != 1) {
        throw new Exception("Cannot find exactly one Signature element");
      }
      return verifySignature(checkReferences, fac, nl.item(0));
    } catch (Exception e) {
      LOG.error("Caught {} validating signature. Msg: {}", e.getClass().getName(), e.getMessage());
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public Map<String, ValidationResponse> validateSignatureValidityMulti(
      String xml, boolean checkReferences) {
    Map<String, ValidationResponse> map = new HashMap<>();
    XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);

    try {
      Document doc =
          dbf.newDocumentBuilder().parse(IOUtils.toInputStream(xml, Charset.forName("UTF-8")));
      NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
      if (nl.getLength() == 0) {
        return new HashMap<>();
      }

      for (int index = 0; index < nl.getLength(); index++) {
        Node node = nl.item(index);
        String intygsId = extractIntygsId(node);

        ValidationResponse validationResponse = verifySignature(checkReferences, fac, node);
        map.put(intygsId, validationResponse);
      }
      return map;
    } catch (Exception e) {
      LOG.error(
          "Caught {} validating signature(s). Msg: {}", e.getClass().getName(), e.getMessage());
    }
    return map;
  }

  private String extractIntygsId(Node node) {
    // Find intygs-id
    XPathFactory xpathFactory = XPathFactory.newInstance();
    XPath xpath = xpathFactory.newXPath();
    try {
      Node intygsIdNode =
          (Node)
              xpath
                  .compile("//*[local-name() = 'intygs-id']/*[local-name() = 'extension']")
                  .evaluate(node, XPathConstants.NODE);
      return intygsIdNode.getTextContent();
    } catch (Exception e) {
      LOG.error("Error extracting intygs-id/extension from XML: " + e.getMessage());
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public Map<String, CertificateInfo> extractCertificateInfo(String xmlWithSignedIntyg) {
    HashMap<String, CertificateInfo> map = new HashMap<>();
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);

    try {
      Document doc =
          dbf.newDocumentBuilder()
              .parse(IOUtils.toInputStream(xmlWithSignedIntyg, StandardCharsets.UTF_8));
      NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
      if (nl.getLength() == 0) {
        throw new Exception("Cannot find Signature element");
      }

      for (int index = 0; index < nl.getLength(); index++) {
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        try {
          Node intygsIdNode =
              (Node)
                  xpath
                      .compile("//*[local-name() = 'intygs-id']/*[local-name() = 'extension']")
                      .evaluate(nl.item(index), XPathConstants.NODE);
          String intygsId = intygsIdNode.getTextContent();

          Node certificateNode =
              (Node)
                  xpath
                      .compile("//*[local-name() = 'X509Certificate']")
                      .evaluate(nl.item(index), XPathConstants.NODE);
          X509Certificate x509Certificate = convertToX509Cert(certificateNode.getTextContent());
          if (x509Certificate == null) {
            continue;
          }
          map.put(
              intygsId,
              CertificateInfo.CertificateInfoBuilder.aCertificateInfo()
                  .withSubject(x509Certificate.getSubjectDN().getName())
                  .withIssuer(x509Certificate.getIssuerDN().getName())
                  .withAlg(x509Certificate.getSigAlgName())
                  .withCertificateType(x509Certificate.getType())
                  .build());

        } catch (Exception e) {
          throw new IllegalArgumentException(
              "Unable to process X509Certificate from Signature: " + e.getMessage());
        }
      }

    } catch (Exception e) {
      LOG.error(
          "Caught {} extracting signature from from signed intyg. Msg: {}",
          e.getClass().getName(),
          e.getMessage());
      throw new IllegalArgumentException(
          "Unable to process Signature from document: " + e.getMessage());
    }
    return map;
  }

  private X509Certificate convertToX509Cert(final String certificateString)
      throws CertificateException {
    try {
      if (certificateString != null && !certificateString.trim().isEmpty()) {

        // Remove
        String cleanedString =
            certificateString
                .replace(BEGIN_CERTIFICATE_STRING, "")
                .replace(END_CERTIFICATE_STRING, "");
        CertificateFactory cf = CertificateFactory.getInstance("X509");
        return (X509Certificate)
            cf.generateCertificate(
                new ByteArrayInputStream(Base64.getDecoder().decode(cleanedString)));
      }
    } catch (CertificateException e) {
      LOG.error("Unable to extract X509Certificat from string representation: {}", e.getMessage());
      throw new CertificateException(e);
    }
    return null;
  }

  private ValidationResponse verifySignature(
      boolean checkReferences, XMLSignatureFactory fac, Node node)
      throws MarshalException, XMLSignatureException {
    // Create a DOMValidateContext and specify a KeySelector
    // and document context.
    DOMValidateContext valContext = new DOMValidateContext(new X509KeySelector(), node);

    // Unmarshal the XMLSignature.
    XMLSignature sig = fac.unmarshalXMLSignature(valContext);

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
  }
}
