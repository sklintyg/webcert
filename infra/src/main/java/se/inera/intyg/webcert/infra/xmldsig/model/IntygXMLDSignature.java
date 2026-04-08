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
package se.inera.intyg.webcert.infra.xmldsig.model;

import org.w3._2000._09.xmldsig_.SignatureType;

public class IntygXMLDSignature implements IntygSignature {

  private SignatureType signatureType;
  private String canonicalizedIntygXml;
  private String signedInfoForSigning;
  private String intygJson;

  public SignatureType getSignatureType() {
    return signatureType;
  }

  @Override
  public String getCanonicalizedIntyg() {
    return canonicalizedIntygXml;
  }

  @Override
  public String getSigningData() {
    return signedInfoForSigning;
  }

  @Override
  public String getIntygJson() {
    return intygJson;
  }

  public void setSignatureType(SignatureType signatureType) {
    this.signatureType = signatureType;
  }

  public String getCanonicalizedIntygXml() {
    return canonicalizedIntygXml;
  }

  public void setCanonicalizedIntygXml(String canonicalizedIntygXml) {
    this.canonicalizedIntygXml = canonicalizedIntygXml;
  }

  public String getSignedInfoForSigning() {
    return signedInfoForSigning;
  }

  public void setSignedInfoForSigning(String signedInfoForSigning) {
    this.signedInfoForSigning = signedInfoForSigning;
  }

  public void setIntygJson(String intygJson) {
    this.intygJson = intygJson;
  }

  public static final class IntygXMLDSignatureBuilder {

    private SignatureType signatureType;
    private String canonicalizedIntygXml;
    private String signedInfoForSigning;
    private String intygJson;

    private IntygXMLDSignatureBuilder() {}

    public static IntygXMLDSignatureBuilder anIntygXMLDSignature() {
      return new IntygXMLDSignatureBuilder();
    }

    public IntygXMLDSignatureBuilder withSignatureType(SignatureType signatureType) {
      this.signatureType = signatureType;
      return this;
    }

    public IntygXMLDSignatureBuilder withCanonicalizedIntygXml(String canonicalizedIntygXml) {
      this.canonicalizedIntygXml = canonicalizedIntygXml;
      return this;
    }

    public IntygXMLDSignatureBuilder withSignedInfoForSigning(String signedInfoForSigning) {
      this.signedInfoForSigning = signedInfoForSigning;
      return this;
    }

    public IntygXMLDSignatureBuilder withIntygJson(String intygJson) {
      this.intygJson = intygJson;
      return this;
    }

    public IntygXMLDSignature build() {
      IntygXMLDSignature intygXMLDSignature = new IntygXMLDSignature();
      intygXMLDSignature.intygJson = this.intygJson;
      intygXMLDSignature.canonicalizedIntygXml = this.canonicalizedIntygXml;
      intygXMLDSignature.signedInfoForSigning = this.signedInfoForSigning;
      intygXMLDSignature.signatureType = this.signatureType;
      return intygXMLDSignature;
    }
  }
}
