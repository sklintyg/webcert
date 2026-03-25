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
package se.inera.intyg.webcert.infra.xmldsig.util;

import java.security.PublicKey;
import java.security.cert.X509Certificate;
import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.X509Data;

/**
 * A <code>KeySelector</code> that returns {@link PublicKey}s of trusted {@link X509Certificate}s.
 *
 * <p>Will only handle X509Certificates passed from a XMLDSig structure.
 *
 * @author eriklupander
 */
public class X509KeySelector extends KeySelector {

  @Override
  public KeySelectorResult select(
      KeyInfo keyInfo, Purpose purpose, AlgorithmMethod method, XMLCryptoContext context)
      throws KeySelectorException {
    for (Object o1 : keyInfo.getContent()) {
      XMLStructure info = (XMLStructure) o1;
      if (!(info instanceof X509Data)) {
        continue;
      }

      X509Data x509Data = (X509Data) info;

      for (Object o : x509Data.getContent()) {
        if (!(o instanceof X509Certificate)) {
          continue;
        }

        final PublicKey key = ((X509Certificate) o).getPublicKey();
        // Make sure the algorithm is compatible
        // with the method.
        if (algEquals(method.getAlgorithm(), key.getAlgorithm())) {
          return () -> key;
        }
      }
    }
    throw new KeySelectorException("No key found!");
  }

  private static boolean algEquals(String algURI, String algName) {
    var lastSignMethodPart = algURI.substring(algURI.lastIndexOf("#") + 1);

    if ("EC".equalsIgnoreCase(algName) && lastSignMethodPart.startsWith("ecdsa")) {
      return true;
    } else if ("RSA".equalsIgnoreCase(algName) && lastSignMethodPart.startsWith("rsa")) {
      return true;
    } else if ("DSA".equalsIgnoreCase(algName) && lastSignMethodPart.startsWith("dsa")) {
      return true;
    } else {
      return false;
    }
  }
}
