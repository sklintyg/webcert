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
package se.inera.intyg.webcert.infra.xmldsig.filter;

import static se.inera.intyg.infra.xmldsig.model.FakeSignatureConstants.FAKE_KEYSTORE_ALIAS;
import static se.inera.intyg.infra.xmldsig.model.FakeSignatureConstants.FAKE_KEYSTORE_NAME;
import static se.inera.intyg.infra.xmldsig.model.FakeSignatureConstants.FAKE_KEYSTORE_PASSWORD;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.crypto.dsig.spec.XPathFilter2ParameterSpec;
import javax.xml.crypto.dsig.spec.XPathType;
import javax.xml.crypto.dsig.spec.XSLTTransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import se.inera.intyg.infra.xmldsig.util.X509KeySelector;

@Disabled("Temporarily disabled 2023-11-18 while experimenting with jakart and kjava 17")
class WithFilterTest {

  // CHECKSTYLE:OFF LineLength
  private static final String xml =
      "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><wrapper><intyg><intygs-id><root>TSTNMT2321000156-1077</root><extension>9f02dd2f-f57c-4a73-8190-2fe602cd6e27</extension></intygs-id></intyg></wrapper>";
  private static final String xpath =
      "//extension[text() = '9f02dd2f-f57c-4a73-8190-2fe602cd6e27']/../..";

  // CHECKSTYLE:ON LineLength

  @Test
  void test() throws Exception {

    org.apache.xml.security.Init.init();
    System.setProperty(
        "javax.xml.transform.TransformerFactory",
        "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");

    // Create a DOM XMLSignatureFactory that will be used to
    // generate the enveloped signature.
    final XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

    // Create a Reference to the enveloped document (in this case,
    // you are signing the whole document, so a URI of "" signifies
    // that, and also specify the SHA1 digest algorithm and
    // the ENVELOPED Transform.
    final List<XPathType> xpaths =
        new ArrayList<XPathType>() {
          {
            add(new XPathType(xpath, XPathType.Filter.INTERSECT));
          }
        };
    List<Transform> transforms = new ArrayList<>();

    XMLStructure sheet = new DOMStructure(loadXsltElement("transforms/stripall.xslt"));

    transforms.add(
        fac.newTransform("http://www.w3.org/2001/10/xml-exc-c14n#", (TransformParameterSpec) null));
    transforms.add(fac.newTransform(Transform.XSLT, new XSLTTransformParameterSpec(sheet)));
    transforms.add(fac.newTransform(Transform.XPATH2, new XPathFilter2ParameterSpec(xpaths)));
    transforms.add(fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null));

    Reference ref =
        fac.newReference(
            "", fac.newDigestMethod(DigestMethod.SHA256, null), transforms, null, null);

    // Create the SignedInfo.
    SignedInfo si =
        fac.newSignedInfo(
            fac.newCanonicalizationMethod(
                CanonicalizationMethod.EXCLUSIVE, (C14NMethodParameterSpec) null),
            fac.newSignatureMethod("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", null),
            Collections.singletonList(ref));

    // Load the KeyStore and get the signing key and certificate.
    KeyStore ks = KeyStore.getInstance("JKS");
    ks.load(
        new ClassPathResource(FAKE_KEYSTORE_NAME).getInputStream(),
        FAKE_KEYSTORE_PASSWORD.toCharArray());
    KeyStore.PrivateKeyEntry keyEntry =
        (KeyStore.PrivateKeyEntry)
            ks.getEntry(
                FAKE_KEYSTORE_ALIAS,
                new KeyStore.PasswordProtection(FAKE_KEYSTORE_PASSWORD.toCharArray()));
    X509Certificate cert = (X509Certificate) keyEntry.getCertificate();

    // Create the KeyInfo containing the X509Data.
    KeyInfoFactory kif = fac.getKeyInfoFactory();
    List<X509Certificate> x509Content = new ArrayList<>();
    x509Content.add(cert);
    X509Data xd = kif.newX509Data(x509Content);
    KeyInfo ki = kif.newKeyInfo(Collections.singletonList(xd));

    // Instantiate the document to be signed.
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    Document doc = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes()));

    // Create a DOMSignContext and specify the RSA PrivateKey and
    // location of the resulting XMLSignature's parent element.
    DOMSignContext dsc = new DOMSignContext(keyEntry.getPrivateKey(), doc.getDocumentElement());

    // Create the XMLSignature, but don't sign it yet.
    XMLSignature signature = fac.newXMLSignature(si, ki);

    // Marshal, generate, and sign the enveloped signature.
    signature.sign(dsc);

    // Output the resulting document.
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer trans = tf.newTransformer();
    trans.transform(new DOMSource(doc), new StreamResult(System.out));

    validate(fac, doc);
  }

  private static void validate(XMLSignatureFactory fac, Document doc) throws Exception {

    // Find Signature element.
    NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
    if (nl.getLength() == 0) {
      throw new Exception("Cannot find Signature element");
    }

    // Create a DOMValidateContext and specify a KeySelector
    // and document context.
    DOMValidateContext valContext = new DOMValidateContext(new X509KeySelector(), nl.item(0));

    // Unmarshal the XMLSignature.
    XMLSignature sig = fac.unmarshalXMLSignature(valContext);

    // Validate the XMLSignature.
    boolean coreValidity = sig.validate(valContext);

    // Check core validation status.
    if (!coreValidity) {
      System.err.println("Signature failed core validation");
      boolean sv = sig.getSignatureValue().validate(valContext);
      System.out.println("signature validation status: " + sv);
      if (!sv) {
        // Check the validation status of each Reference.
        Iterator i = sig.getSignedInfo().getReferences().iterator();
        for (int j = 0; i.hasNext(); j++) {
          boolean refValid = ((Reference) i.next()).validate(valContext);
          System.out.println("ref[" + j + "] validity status: " + refValid);
        }
      }
    } else {
      System.out.println("Signature passed core validation");
    }
  }

  private static Element loadXsltElement(String path) {

    ClassPathResource cpr = new ClassPathResource(path);
    // Append the SignatureElement as last element of the xml.
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
