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
package se.inera.intyg.webcert.infra.integration.hsatk.model.legacy;

import java.util.ArrayList;
import java.util.List;
import se.inera.intyg.webcert.infra.integration.hsatk.model.HsaSystemRole;

/**
 * Storage for user-related info from GetCredentials HSA response.
 *
 * <p>A number of data fields from HSA that are related to the persons "medarbetaruppdrag" is
 * returned on the {@link se.riv.infrastructure.directory.v1.CredentialInformationType}, which we
 * need to provide to callers of the HSA services.
 */
public class UserCredentials {

  private String personalPrescriptionCode;
  private List<String> groupPrescriptionCode;
  private List<HsaSystemRole> hsaSystemRole;
  private List<String> paTitleCode;

  public String getPersonalPrescriptionCode() {
    return personalPrescriptionCode;
  }

  public void setPersonalPrescriptionCode(String personalPrescriptionCode) {
    this.personalPrescriptionCode = personalPrescriptionCode;
  }

  public List<String> getGroupPrescriptionCode() {
    if (groupPrescriptionCode == null) {
      groupPrescriptionCode = new ArrayList<>();
    }
    return groupPrescriptionCode;
  }

  public List<HsaSystemRole> getHsaSystemRole() {
    if (hsaSystemRole == null) {
      hsaSystemRole = new ArrayList<>();
    }
    return hsaSystemRole;
  }

  public List<String> getPaTitleCode() {
    if (paTitleCode == null) {
      paTitleCode = new ArrayList<>();
    }
    return paTitleCode;
  }
}
