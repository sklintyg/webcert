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
package se.inera.intyg.webcert.infra.integration.hsatk.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class HospCredentialsForPerson {

  private String personalIdentityNumber;
  private List<HealthCareProfessionalLicence> healthCareProfessionalLicence = new ArrayList<>();
  private String personalPrescriptionCode;
  private List<HCPSpecialityCodes> healthCareProfessionalLicenceSpeciality = new ArrayList<>();
  private List<NursePrescriptionRight> nursePrescriptionRight = new ArrayList<>();
  private String healthcareProfessionalLicenseIdentityNumber;
  private List<String> educationCode = new ArrayList<>();
  private List<Restriction> restrictions = new ArrayList<>();
  private Boolean feignedPerson;

  @Data
  public static class Restriction {

    private String healthCareProfessionalLicenceCode;
    private String restrictionCode;
    private String restrictionName;
  }
}
