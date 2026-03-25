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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class UnitAddressConverter {

  private static final int CITY_START_INDEX = 6;

  public String convertAddress(List<String> addressLines) {
    if (addressLines == null) {
      return null;
    }

    if (addressLines.isEmpty()) {
      return "";
    }

    final var includedAddressLines = addressLines.subList(0, addressLines.size() - 1);

    return includedAddressLines.stream().filter(Objects::nonNull).collect(Collectors.joining(" "));
  }

  public String convertZipCode(List<String> addressLines, String zipCode) {
    if (zipCode != null && !zipCode.trim().isEmpty()) {
      return zipCode;
    }

    final var lastLine = getLastLine(addressLines);
    final boolean shouldIncludeCity = hasMoreInfoThanAddress(lastLine);

    if (shouldIncludeCity) {
      return lastLine.substring(0, CITY_START_INDEX).trim();
    }

    return "";
  }

  public String convertCity(List<String> addressLines) {
    final var lastLine = getLastLine(addressLines);
    final var shouldIncludeCity = hasMoreInfoThanAddress(lastLine);

    if (shouldIncludeCity) {
      return lastLine.substring(CITY_START_INDEX).trim();
    }

    return lastLine != null ? lastLine.trim() : "";
  }

  private static boolean hasMoreInfoThanAddress(String lastLine) {
    return lastLine != null
        && lastLine.length() > CITY_START_INDEX + 1
        && Character.isDigit(lastLine.charAt(0));
  }

  private static String getLastLine(List<String> addressLines) {
    return !addressLines.isEmpty() ? addressLines.get(addressLines.size() - 1) : null;
  }
}
