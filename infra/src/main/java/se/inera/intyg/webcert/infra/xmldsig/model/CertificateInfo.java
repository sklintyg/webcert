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

public class CertificateInfo {

  private String subject;
  private String issuer;
  private String alg;
  private String certificateType;

  public String getSubject() {
    return subject;
  }

  public String getIssuer() {
    return issuer;
  }

  public String getAlg() {
    return alg;
  }

  public String getCertificateType() {
    return certificateType;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public void setIssuer(String issuer) {
    this.issuer = issuer;
  }

  public void setAlg(String alg) {
    this.alg = alg;
  }

  public void setCertificateType(String certificateType) {
    this.certificateType = certificateType;
  }

  public static final class CertificateInfoBuilder {

    private String subject;
    private String issuer;
    private String alg;
    private String certificateType;

    private CertificateInfoBuilder() {}

    public static CertificateInfoBuilder aCertificateInfo() {
      return new CertificateInfoBuilder();
    }

    public CertificateInfoBuilder withSubject(String subject) {
      this.subject = subject;
      return this;
    }

    public CertificateInfoBuilder withIssuer(String issuer) {
      this.issuer = issuer;
      return this;
    }

    public CertificateInfoBuilder withAlg(String alg) {
      this.alg = alg;
      return this;
    }

    public CertificateInfoBuilder withCertificateType(String certificateType) {
      this.certificateType = certificateType;
      return this;
    }

    public CertificateInfo build() {
      CertificateInfo certificateInfo = new CertificateInfo();
      certificateInfo.setSubject(subject);
      certificateInfo.setIssuer(issuer);
      certificateInfo.setAlg(alg);
      certificateInfo.setCertificateType(certificateType);
      return certificateInfo;
    }
  }
}
