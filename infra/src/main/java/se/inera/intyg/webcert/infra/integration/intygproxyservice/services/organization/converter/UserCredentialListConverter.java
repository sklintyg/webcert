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
package se.inera.intyg.webcert.infra.integration.intygproxyservice.services.organization.converter;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import se.inera.intyg.webcert.infra.integration.hsatk.model.CredentialInformation;
import se.inera.intyg.webcert.infra.integration.hsatk.model.legacy.UserCredentials;

@Component
public class UserCredentialListConverter {

  public UserCredentials convert(List<CredentialInformation> credentialInformation) {
    final var lastCredential =
        credentialInformation.isEmpty()
            ? null
            : credentialInformation.get(credentialInformation.size() - 1);
    final var userCredentials = new UserCredentials();
    final var prescriptionCodes =
        toList(credentialInformation, CredentialInformation::getGroupPrescriptionCode);
    final var paTitleCodes = toList(credentialInformation, CredentialInformation::getPaTitleCode);
    final var hsaSystemRoles =
        toList(credentialInformation, CredentialInformation::getHsaSystemRole);

    userCredentials.getGroupPrescriptionCode().addAll(prescriptionCodes);
    userCredentials.getPaTitleCode().addAll(paTitleCodes);
    userCredentials.getHsaSystemRole().addAll(hsaSystemRoles);
    userCredentials.setPersonalPrescriptionCode(
        lastCredential == null ? null : lastCredential.getPersonalPrescriptionCode());

    return userCredentials;
  }

  private static <T> List<T> toList(
      List<CredentialInformation> credentialInformation,
      Function<CredentialInformation, List<T>> mapper) {
    return credentialInformation.stream()
        .flatMap(c -> mapper.apply(c).stream())
        .collect(Collectors.toList());
  }
}
