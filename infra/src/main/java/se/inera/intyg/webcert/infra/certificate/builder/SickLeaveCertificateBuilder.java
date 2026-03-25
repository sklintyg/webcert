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
import se.inera.intyg.infra.certificate.dto.SickLeaveCertificate;

public class SickLeaveCertificateBuilder {

  SickLeaveCertificate certificate;

  public SickLeaveCertificateBuilder(String certificateId) {
    this.certificate = new SickLeaveCertificate();
    certificate.setCertificateId(certificateId);
  }

  public SickLeaveCertificateBuilder certificateType(String certificateType) {
    certificate.setCertificateType(certificateType);
    return this;
  }

  public SickLeaveCertificateBuilder personId(String civicRegistrationNumber) {
    certificate.setPersonId(civicRegistrationNumber);
    return this;
  }

  public SickLeaveCertificateBuilder patientFullName(String patientFullName) {
    certificate.setPatientFullName(patientFullName);
    return this;
  }

  public SickLeaveCertificateBuilder careProviderId(String careProviderId) {
    certificate.setCareProviderId(careProviderId);
    return this;
  }

  public SickLeaveCertificateBuilder careUnitId(String careUnitId) {
    certificate.setCareUnitId(careUnitId);
    return this;
  }

  public SickLeaveCertificateBuilder careUnitName(String careUnitName) {
    certificate.setCareUnitName(careUnitName);
    return this;
  }

  public SickLeaveCertificateBuilder personalHsaId(String personalHsaId) {
    certificate.setPersonalHsaId(personalHsaId);
    return this;
  }

  public SickLeaveCertificateBuilder signingDoctorName(String signingDoctorName) {
    certificate.setPersonalFullName(signingDoctorName);
    return this;
  }

  public SickLeaveCertificateBuilder signingDateTime(LocalDateTime signingDateTime) {
    certificate.setSigningDateTime(signingDateTime);
    return this;
  }

  public SickLeaveCertificateBuilder diagnoseCode(String diagnoseCode) {
    certificate.setDiagnoseCode(diagnoseCode);
    return this;
  }

  public SickLeaveCertificateBuilder secondaryDiagnoseCodes(List<String> diagnoseCodes) {
    certificate.setSecondaryDiagnoseCodes(diagnoseCodes);
    return this;
  }

  public SickLeaveCertificateBuilder deleted(boolean deleted) {
    certificate.setDeleted(deleted);
    return this;
  }

  public SickLeaveCertificateBuilder workCapacityList(
      List<SickLeaveCertificate.WorkCapacity> workCapacities) {
    certificate.setWorkCapacityList(workCapacities);
    return this;
  }

  public SickLeaveCertificateBuilder occupation(String occupation) {
    certificate.setOccupation(occupation);
    return this;
  }

  public SickLeaveCertificateBuilder testCertificate(boolean isTestCertificate) {
    certificate.setTestCertificate(isTestCertificate);
    return this;
  }

  public SickLeaveCertificate build() {
    return certificate;
  }
}
