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
package se.inera.intyg.webcert.infra.integration.hsatk.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import se.inera.intyg.webcert.infra.integration.hsatk.model.PersonInformation;
import se.inera.intyg.webcert.infra.integration.hsatk.model.PersonInformation.PaTitle;

/** Helper class for extracting certain HoSP attributes. */
public class HsaAttributeExtractor {

  public List<String> extractSpecialiseringar(List<PersonInformation> hsaUserTypes) {
    Set<String> specSet = new TreeSet<>();

    for (PersonInformation userType : hsaUserTypes) {
      if (userType.getSpecialityName() != null) {
        specSet.addAll(userType.getSpecialityName());
      }
    }

    return new ArrayList<>(specSet);
  }

  public List<String> extractBefattningar(List<PersonInformation> hsaPersonInfo) {
    Set<String> befattningar = new TreeSet<>();

    for (PersonInformation userType : hsaPersonInfo) {
      if (userType.getPaTitle() != null) {
        List<String> hsaTitles =
            userType.getPaTitle().stream()
                .map(PersonInformation.PaTitle::getPaTitleCode)
                .filter(Objects::nonNull)
                .toList();
        if (!hsaTitles.isEmpty()) {
          befattningar.addAll(hsaTitles);
        }
      }
    }
    return new ArrayList<>(befattningar);
  }

  public List<PaTitle> extractBefattningsKoder(List<PersonInformation> hsaPersonInfo) {
    return hsaPersonInfo.stream()
        .filter(pi -> pi.getPaTitle() != null)
        .flatMap(pi -> pi.getPaTitle().stream())
        .filter(pt -> pt.getPaTitleCode() != null)
        .distinct()
        .sorted(Comparator.comparing(PaTitle::getPaTitleCode))
        .toList();
  }

  public List<String> extractLegitimeradeYrkesgrupper(List<PersonInformation> hsaUserTypes) {
    Set<String> lygSet = new TreeSet<>();

    for (PersonInformation userType : hsaUserTypes) {
      if (userType.getHealthCareProfessionalLicence() != null) {
        lygSet.addAll(userType.getHealthCareProfessionalLicence());
      }
    }
    return new ArrayList<>(lygSet);
  }

  /** Tries to use title attribute, otherwise resorts to healthcareProfessionalLicenses. */
  public String extractTitel(List<PersonInformation> hsaPersonInfo) {
    Set<String> titleSet = new HashSet<>();
    for (PersonInformation pit : hsaPersonInfo) {
      if (pit.getTitle() != null && !pit.getTitle().trim().isEmpty()) {
        titleSet.add(pit.getTitle());
      }
      //            else if (pit.getHealthCareProfessionalLicence() != null &&
      // pit.getHealthCareProfessionalLicence().size() > 0) {
      //                titleSet.addAll(pit.getHealthCareProfessionalLicence());
      //            }
    }
    return titleSet.stream().sorted().collect(Collectors.joining(", "));
  }
}
