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

import static se.inera.intyg.webcert.infra.xmldsig.model.FakeSignatureConstants.FAKE_KEYSTORE_ALIAS;
import static se.inera.intyg.webcert.infra.xmldsig.model.FakeSignatureConstants.FAKE_KEYSTORE_NAME;
import static se.inera.intyg.webcert.infra.xmldsig.model.FakeSignatureConstants.FAKE_KEYSTORE_PASSWORD;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import org.springframework.core.io.ClassPathResource;

public class FakeSignatureServiceImpl implements FakeSignatureService {

  private KeyStore ks;

  @PostConstruct
  public void init()
      throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
    this.ks = KeyStore.getInstance("JKS");
    this.ks.load(
        new ClassPathResource(FAKE_KEYSTORE_NAME).getInputStream(),
        FAKE_KEYSTORE_PASSWORD.toCharArray());
  }

  /**
   * Signs the supplied digest using a self-signed cert. Only for fake purposes!!
   *
   * @param digest Base64-encoded string to sign.
   */
  @Override
  public String createSignature(String digest) {
    try {
      KeyStore.PrivateKeyEntry keyEntry =
          (KeyStore.PrivateKeyEntry)
              ks.getEntry(
                  FAKE_KEYSTORE_ALIAS,
                  new KeyStore.PasswordProtection(FAKE_KEYSTORE_PASSWORD.toCharArray()));

      Signature rsa = Signature.getInstance("SHA256withRSA");
      rsa.initSign(keyEntry.getPrivateKey());
      rsa.update(Base64.getDecoder().decode(digest));
      byte[] signatureBytes = rsa.sign();

      return Base64.getEncoder().encodeToString(signatureBytes);
    } catch (Exception e) {
      throw new IllegalStateException("Not possible to sign digest: " + e.getMessage());
    }
  }

  @Override
  public X509Certificate getX509Certificate() {
    try {
      return (X509Certificate) this.ks.getCertificate("1");
    } catch (KeyStoreException e) {
      throw new RuntimeException(e);
    }
  }
}