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
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Created by marced on 08/02/16. */
public class DiagnosKategori {

  static final Pattern EXTRACT_DIAGNOSKATEGORI_REGEXP = Pattern.compile("^([A-Z]{1})([0-9]{2}).?");
  private static final int KATEGORI_CHAR = 1;
  private static final int KATEGORI_NUMBER = 2;

  private char letter;
  private int number;

  public DiagnosKategori(char letter, int number) {
    this.letter = letter;
    this.number = number;
  }

  public DiagnosKategori() {}

  public char getLetter() {
    return letter;
  }

  public int getNumber() {
    return number;
  }

  public String getId() {
    if (isNullOrEmpty(String.valueOf(this.letter).trim())) {
      return "";
    }
    return (String.valueOf(this.letter) + String.format("%02d", this.number)).trim();
  }

  public static Optional<DiagnosKategori> extractFromString(String diagnosKod) {
    if (!isNullOrEmpty(diagnosKod)) {
      Matcher matcher = EXTRACT_DIAGNOSKATEGORI_REGEXP.matcher(diagnosKod);
      if (matcher.find()) {
        return Optional.of(
            new DiagnosKategori(
                matcher.group(KATEGORI_CHAR).charAt(0),
                Integer.parseInt(matcher.group(KATEGORI_NUMBER))));
      }
    }
    return Optional.empty();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DiagnosKategori)) {
      return false;
    }
    DiagnosKategori that = (DiagnosKategori) o;
    return letter == that.letter && number == that.number;
  }

  @Override
  public int hashCode() {
    return Objects.hash(letter, number);
  }

  private static boolean isNullOrEmpty(String string) {
    return string == null || string.length() == 0;
  }

  @Override
  public String toString() {
    return "DiagnosKategori{" + "letter=" + letter + ", number=" + number + '}';
  }
}
