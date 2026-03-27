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
package se.inera.intyg.webcert.infra.integration.hsatk.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class CredentialInformation {

  protected String givenName;
  protected String middleAndSurName;
  protected String personHsaId;
  protected List<String> healthCareProfessionalLicence = new ArrayList<>();
  protected String personalPrescriptionCode;
  protected List<String> groupPrescriptionCode = new ArrayList<>();
  protected List<NursePrescriptionRight> nursePrescriptionRight = new ArrayList<>();
  protected List<HsaSystemRole> hsaSystemRole = new ArrayList<>();
  protected List<String> paTitleCode = new ArrayList<>();
  protected Boolean protectedPerson;
  protected List<Commission> commission = new ArrayList<>();
  protected Boolean feignedPerson;
  protected List<String> healthCareProfessionalLicenceCode = new ArrayList<>();
  protected List<HCPSpecialityCodes> healthCareProfessionalLicenceSpeciality = new ArrayList<>();
  protected List<String> occupationalCode = new ArrayList<>();
  protected String personalIdentity;
  protected String healthcareProfessionalLicenseIdentityNumber;
}
