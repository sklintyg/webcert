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

import org.w3._2000._09.xmldsig_.SignatureType;
import se.inera.intyg.webcert.infra.xmldsig.model.IntygXMLDSignature;
import se.inera.intyg.webcert.infra.xmldsig.model.TransformAndDigestResponse;

public interface PrepareSignatureService {

  /**
   * Prepares an XMLDSig signature, a canonicalized SignedInfo and the canonicalized XML that the
   * digest is based on.
   *
   * <p>Given the supplied XML, the XML is canonicalized and a SHA-256 digest is created and
   * Base64-encoded into the DigestValue field.
   *
   * <p>Also, relevant algorithms for digest, signature and canonicalization method are specified on
   * the body of the returned {@link SignatureType}.
   *
   * @param intygXml XML document to be canonicalized and digested.
   * @param intygsId The ID of the intyg is required for the XPath expression selecting the content
   *     to be digested.
   * @param signatureAlgorithm specifies the signature algorithm to be used.
   * @return IntygXMLDSignature
   */
  IntygXMLDSignature prepareSignature(String intygXml, String intygsId, String signatureAlgorithm);

  /**
   * Writes the <SignatureValue> element into the Signature.
   *
   * @param signatureType which will be appended to xml
   * @param xml for Signature to be appended to
   * @return The signed XML
   */
  String encodeSignatureIntoSignedXml(SignatureType signatureType, String xml);

  /**
   * Performs intygs specific transform and calculates digest on the transformed data.
   *
   * @param intygXml Untransformed data
   * @param intygsId IntygsId
   * @return the Digest of the transformed intyg as Base64 encoded, and the transformed intygsXml
   */
  TransformAndDigestResponse transformAndGenerateDigest(String intygXml, String intygsId);
}
