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
package se.inera.intyg.webcert.infra.xmldsig.factory;

import jakarta.xml.bind.JAXBElement;
import java.io.IOException;
import java.util.Base64;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Transform;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.springframework.core.io.ClassPathResource;
import org.w3._2000._09.xmldsig_.CanonicalizationMethodType;
import org.w3._2000._09.xmldsig_.DigestMethodType;
import org.w3._2000._09.xmldsig_.KeyInfoType;
import org.w3._2000._09.xmldsig_.ObjectFactory;
import org.w3._2000._09.xmldsig_.ReferenceType;
import org.w3._2000._09.xmldsig_.SignatureMethodType;
import org.w3._2000._09.xmldsig_.SignatureType;
import org.w3._2000._09.xmldsig_.SignatureValueType;
import org.w3._2000._09.xmldsig_.SignedInfoType;
import org.w3._2000._09.xmldsig_.TransformType;
import org.w3._2000._09.xmldsig_.TransformsType;
import org.w3._2000._09.xmldsig_.X509DataType;
import org.w3._2002._06.xmldsig_filter2.XPathType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public final class PartialSignatureFactory {

  public static final String DEFAULT_SIGNATURE_ALGORITHM =
      "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";
  private static final String TRANSFORM_ALGORITHM =
      "http://www.w3.org/2000/09/xmldsig#enveloped-signature";
  private static final String FILTER_INTERSECT = "intersect";
  private static final String XPATH_PART1 = "//extension[text()='";
  private static final String XPATH_PART2 = "']/../..";
  private static final String XSLT_STRIP_NAMESPACE = "transforms/stripnamespace.xslt";
  private static final String FILTER_SUBTRACT = "subtract";
  private static final String XPATH_SUBTRACT_METADATA_EXPRESSION =
      "//*[local-name() = 'skickatTidpunkt']|"
          + "//*[local-name() = 'relation']|//*[local-name() = 'status']|//*[local-name() = 'underskrift']";

  private PartialSignatureFactory() {}

  /**
   * Builds a partially populated {@link SignatureType}.
   *
   * <p>Contains appropriate algorithms and elements for subsequent population of digest, signature
   * value and keyinfo.
   *
   * @param intygsId used in reference transform
   * @param signatureAlgorithm specifies the signature algorithm to be used
   * @return A SignatureType with the SignedInfo that should be signed
   */
  public static SignatureType buildSignature(
      String intygsId, byte[] digestBytes, String signatureAlgorithm) {
    SignatureType signature = new SignatureType();
    SignedInfoType signedInfo = new SignedInfoType();

    CanonicalizationMethodType canonType = new CanonicalizationMethodType();
    canonType.setAlgorithm(CanonicalizationMethod.EXCLUSIVE);
    signedInfo.setCanonicalizationMethod(canonType);

    SignatureMethodType signatureMethod = new SignatureMethodType();
    signatureMethod.setAlgorithm(signatureAlgorithm);

    signedInfo.setSignatureMethod(signatureMethod);

    ReferenceType referenceType = new ReferenceType();
    DigestMethodType digestMethodType = new DigestMethodType();
    digestMethodType.setAlgorithm(DigestMethod.SHA256);
    referenceType.setDigestMethod(digestMethodType);
    referenceType.setURI("");

    TransformType envelopedTransform = new TransformType();
    envelopedTransform.setAlgorithm(TRANSFORM_ALGORITHM);

    TransformType canonicalizationTransform = new TransformType();
    canonicalizationTransform.setAlgorithm("http://www.w3.org/2001/10/xml-exc-c14n#");

    // Add XSLT stylesheet that runs local() on all nodes to strip namespaces and prefixes.
    TransformType intygCanonicalizationTransform = new TransformType();
    intygCanonicalizationTransform.setAlgorithm(Transform.XSLT);
    intygCanonicalizationTransform.getContent().add(loadXsltElement(XSLT_STRIP_NAMESPACE));

    TransformType xpathFilterTransform = new TransformType();
    xpathFilterTransform.setAlgorithm(Transform.XPATH2);
    XPathType xp = new XPathType();
    xp.setFilter(FILTER_INTERSECT);
    xp.setValue(XPATH_PART1 + intygsId + XPATH_PART2);
    xpathFilterTransform
        .getContent()
        .add(new org.w3._2002._06.xmldsig_filter2.ObjectFactory().createXPath(xp));

    TransformType xpathRemoveUnwantedTransform = new TransformType();
    xpathRemoveUnwantedTransform.setAlgorithm(Transform.XPATH2);
    XPathType xp2 = new XPathType();
    xp2.setFilter(FILTER_SUBTRACT);
    xp2.setValue(XPATH_SUBTRACT_METADATA_EXPRESSION);
    xpathRemoveUnwantedTransform
        .getContent()
        .add(new org.w3._2002._06.xmldsig_filter2.ObjectFactory().createXPath(xp2));

    // The order here IS significant!! Otherwise, validation will not produce the expected digest.
    TransformsType transforms = new TransformsType();
    transforms
        .getTransform()
        .add(envelopedTransform); // Having envelopedTransform makes sure the <Signature> element is
    // removed when
    // digesting.
    transforms.getTransform().add(intygCanonicalizationTransform);
    transforms.getTransform().add(xpathFilterTransform);
    transforms.getTransform().add(xpathRemoveUnwantedTransform);
    transforms
        .getTransform()
        .add(
            canonicalizationTransform); // Canonicalization makes sure tags are not self-closed etc.

    referenceType.setTransforms(transforms);

    // Set the raw digest bytes.
    referenceType.setDigestValue(Base64.getDecoder().decode(digestBytes));

    signedInfo.getReference().add(referenceType);
    signature.setSignedInfo(signedInfo);

    SignatureValueType signatureValue = new SignatureValueType();
    signature.setSignatureValue(signatureValue);
    return signature;
  }

  /**
   * Builds a {@link KeyInfoType} element with the supplied certificate added into a
   * X509Data->X509Certificate element.
   *
   * @param certificate Base64-encoded string of a x509 certificate.
   * @return A KeyInfoType object.
   */
  public static KeyInfoType buildKeyInfo(String certificate) {
    KeyInfoType keyInfo = new KeyInfoType();

    ObjectFactory objectFactory = new ObjectFactory();
    X509DataType x509DataType = objectFactory.createX509DataType();
    JAXBElement<byte[]> x509cert =
        objectFactory.createX509DataTypeX509Certificate(Base64.getDecoder().decode(certificate));
    x509DataType.getX509IssuerSerialOrX509SKIOrX509SubjectName().add(x509cert);
    keyInfo.getContent().add(objectFactory.createX509Data(x509DataType));
    return keyInfo;
  }

  private static Element loadXsltElement(String path) {

    ClassPathResource cpr = new ClassPathResource(path);
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);

    try {
      Document doc = dbf.newDocumentBuilder().parse(cpr.getInputStream());
      return doc.getDocumentElement();
    } catch (SAXException | IOException | ParserConfigurationException e) {
      throw new RuntimeException(e.getCause());
    }
  }
}
