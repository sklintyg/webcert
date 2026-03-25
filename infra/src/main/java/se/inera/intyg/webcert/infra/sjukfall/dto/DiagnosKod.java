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
package se.inera.intyg.webcert.infra.sjukfall.dto;

import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

/** Created by martin on 10/02/16. */
public class DiagnosKod {

  private static final int KOD_LENGTH = 7;

  private String originalCode;
  private String cleanedCode;
  private String name;

  public static DiagnosKod create(String originalCode) {
    final var diagnosKod = new DiagnosKod();
    if (StringUtils.isBlank(originalCode)) {
      throw new IllegalArgumentException(
          "Argument 'originalCode' in call to DiagnosKod is either empty, null or blank");
    }
    diagnosKod.originalCode = originalCode;
    if (diagnosKod.originalCode.length() >= KOD_LENGTH) {
      diagnosKod.cleanedCode = cleanKod(diagnosKod.originalCode.substring(0, KOD_LENGTH));
      diagnosKod.name = diagnosKod.originalCode.substring(KOD_LENGTH).trim();
    } else {
      diagnosKod.cleanedCode = cleanKod(diagnosKod.originalCode);
    }
    return diagnosKod;
  }

  public String getOriginalCode() {
    return originalCode;
  }

  public String getName() {
    return name;
  }

  public String getCleanedCode() {
    return cleanedCode;
  }

  public static String cleanKod(String kod) {
    String cleanedKod = kod.trim().toUpperCase();
    return cleanedKod.replaceAll("[^A-Z0-9\\-]", "");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final DiagnosKod that = (DiagnosKod) o;
    return Objects.equals(originalCode, that.originalCode)
        && Objects.equals(cleanedCode, that.cleanedCode)
        && Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(originalCode, cleanedCode, name);
  }

  @Override
  public String toString() {
    return "DiagnosKod{"
        + "originalCode='"
        + originalCode
        + '\''
        + ", cleanedCode='"
        + cleanedCode
        + '\''
        + ", name='"
        + name
        + '\''
        + '}';
  }
}
