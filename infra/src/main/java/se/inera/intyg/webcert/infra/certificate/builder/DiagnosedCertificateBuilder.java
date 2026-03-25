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
package se.inera.intyg.webcert.infra.certificate.builder;

import java.time.LocalDateTime;
import java.util.List;
import se.inera.intyg.infra.certificate.dto.DiagnosedCertificate;

public class DiagnosedCertificateBuilder {

  DiagnosedCertificate certificate;

  public DiagnosedCertificateBuilder(String certificateId) {
    this.certificate = new DiagnosedCertificate();
    certificate.setCertificateId(certificateId);
  }

  public DiagnosedCertificateBuilder certificateType(String certificateType) {
    certificate.setCertificateType(certificateType);
    return this;
  }

  public DiagnosedCertificateBuilder personId(String civicRegistrationNumber) {
    certificate.setPersonId(civicRegistrationNumber);
    return this;
  }

  public DiagnosedCertificateBuilder patientFullName(String patientFullName) {
    certificate.setPatientFullName(patientFullName);
    return this;
  }

  public DiagnosedCertificateBuilder careProviderId(String careProviderId) {
    certificate.setCareProviderId(careProviderId);
    return this;
  }

  public DiagnosedCertificateBuilder careUnitId(String careUnitId) {
    certificate.setCareUnitId(careUnitId);
    return this;
  }

  public DiagnosedCertificateBuilder careUnitName(String careUnitName) {
    certificate.setCareUnitName(careUnitName);
    return this;
  }

  public DiagnosedCertificateBuilder personalHsaId(String personalHsaId) {
    certificate.setPersonalHsaId(personalHsaId);
    return this;
  }

  public DiagnosedCertificateBuilder signingDoctorName(String signingDoctorName) {
    certificate.setPersonalFullName(signingDoctorName);
    return this;
  }

  public DiagnosedCertificateBuilder signingDateTime(LocalDateTime signingDateTime) {
    certificate.setSigningDateTime(signingDateTime);
    return this;
  }

  public DiagnosedCertificateBuilder diagnoseCode(String diagnoseCode) {
    certificate.setDiagnoseCode(diagnoseCode);
    return this;
  }

  public DiagnosedCertificateBuilder secondaryDiagnoseCodes(List<String> diagnoseCodes) {
    certificate.setSecondaryDiagnoseCodes(diagnoseCodes);
    return this;
  }

  public DiagnosedCertificateBuilder deleted(boolean deleted) {
    certificate.setDeleted(deleted);
    return this;
  }

  public DiagnosedCertificateBuilder testCertificate(boolean isTestCertificate) {
    certificate.setTestCertificate(isTestCertificate);
    return this;
  }

  public DiagnosedCertificate build() {
    return certificate;
  }
}
