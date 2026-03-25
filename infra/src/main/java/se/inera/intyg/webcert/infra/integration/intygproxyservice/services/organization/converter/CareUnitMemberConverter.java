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
package se.inera.intyg.webcert.infra.integration.intygproxyservice.services.organization.converter;

import static se.inera.intyg.webcert.infra.integration.intygproxyservice.services.organization.OrganizationUtil.getWorkplaceCode;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.infra.integration.hsatk.model.HealthCareUnitMember;
import se.inera.intyg.webcert.infra.integration.hsatk.model.legacy.AgandeForm;
import se.inera.intyg.webcert.infra.integration.hsatk.model.legacy.Mottagning;

@Service
@Slf4j
@RequiredArgsConstructor
public class CareUnitMemberConverter {

  private final UnitAddressConverter unitAddressConverter;

  public Mottagning convert(
      HealthCareUnitMember hsaCareUnitMember, String parentId, AgandeForm parentAgandeForm) {
    final var careUnitMember = getCareUnitMember(hsaCareUnitMember);
    careUnitMember.setParentHsaId(parentId);
    careUnitMember.setAgandeForm(parentAgandeForm);
    careUnitMember.setTelefonnummer(
        String.join(", ", hsaCareUnitMember.getHealthCareUnitMemberTelephoneNumber()));
    careUnitMember.setArbetsplatskod(
        getWorkplaceCode(hsaCareUnitMember.getHealthCareUnitMemberPrescriptionCode()));

    if (hsaCareUnitMember.getHealthCareUnitMemberpostalAddress() != null) {
      updateAddress(
          careUnitMember,
          hsaCareUnitMember.getHealthCareUnitMemberpostalAddress(),
          hsaCareUnitMember.getHealthCareUnitMemberpostalCode());
    }

    return careUnitMember;
  }

  private Mottagning getCareUnitMember(HealthCareUnitMember hsaCareUnitMember) {
    return new Mottagning(
        hsaCareUnitMember.getHealthCareUnitMemberHsaId(),
        hsaCareUnitMember.getHealthCareUnitMemberName(),
        hsaCareUnitMember.getHealthCareUnitMemberStartDate(),
        hsaCareUnitMember.getHealthCareUnitMemberEndDate());
  }

  private void updateAddress(Mottagning unit, List<String> address, String postalCode) {
    unit.setPostadress(unitAddressConverter.convertAddress(address));
    unit.setPostnummer(unitAddressConverter.convertZipCode(address, postalCode));
    unit.setPostort(unitAddressConverter.convertCity(address));
  }
}
