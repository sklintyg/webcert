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

import java.security.KeyStore;
import java.security.KeyStoreException;
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
 * A <code>KeySelector</code> that returns {@link PublicKey}s of trusted
 * {@link X509Certificate}s.
 *
 * Certificates from KeyInfo must be found in the KeySelector's KeyStore
 * to pass as a valid certificate.
 */
public class KeyStoreKeySelector extends KeySelector {


    private KeyStore keyStore;

    /**
     * Creates an <code>KeyStoreKeySelector</code>.
     *
     * @param keyStore the keystore
     * @throws KeyStoreException if the keystore has not been initialized
     * @throws NullPointerException if <code>keyStore</code> is
     * <code>null</code>
     */
    public KeyStoreKeySelector(KeyStore keyStore) throws KeyStoreException {
        if (keyStore == null) {
            throw new NullPointerException("keyStore is null");
        }
        this.keyStore = keyStore;
        // test to see if KeyStore has been initialized
        this.keyStore.size();
    }

    @Override
    public KeySelectorResult select(KeyInfo keyInfo,
        Purpose purpose,
        AlgorithmMethod method,
        XMLCryptoContext context)
        throws KeySelectorException {

        for (Object o1 : keyInfo.getContent()) {
            XMLStructure info = (XMLStructure) o1;

            if (info instanceof X509Data) {
                for (Object o : ((X509Data) info).getContent()) {
                    if (o instanceof X509Certificate) {
                        try {
                            KeySelectorResult keySelectorResult = certSelect((X509Certificate) o, method);
                            if (keySelectorResult != null) {
                                return keySelectorResult;
                            }
                        } catch (KeyStoreException e) {
                            throw new KeySelectorException("KeyStore not initialized", e);
                        }
                    }

                }
            }

        }
        throw new KeySelectorException("No key found!");
    }

    /**
     * Searches the specified keystore for a certificate that matches the
     * specified X509Certificate and contains a public key that is compatible
     * with the specified SignatureMethod.
     *
     * @return a KeySelectorResult containing the cert's public key if there
     * is a match; otherwise null
     */
    private KeySelectorResult certSelect(X509Certificate x509Certificate,
        AlgorithmMethod algorithmMethod) throws KeyStoreException {
        // skip non-signer certs
        boolean[] keyUsage = x509Certificate.getKeyUsage();
        if (keyUsage == null || keyUsage[0] == false) {
            return null;
        }
        String alias = keyStore.getCertificateAlias(x509Certificate);
        if (alias != null) {
            PublicKey publikKey = keyStore.getCertificate(alias).getPublicKey();
            // make sure algorithm is compatible with method
            if (algEquals(algorithmMethod.getAlgorithm(), publikKey.getAlgorithm())) {
                return () -> publikKey;
            }
        }
        return null;
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
