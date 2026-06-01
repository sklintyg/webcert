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
package se.inera.intyg.webcert.web.web.controller.facade.util;

import jakarta.validation.ConstraintValidatorContext;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public abstract class EncodingValidatorBase {

  private static final Set<Character> ALLOWED_CHAR_SET = Set.of(new Character[] {'\n', '\r', '\t'});
  private static final CharsetEncoder ISO_8859_1_ENCODER = StandardCharsets.ISO_8859_1.newEncoder();

  protected boolean isValid(
      String stringToCheck, ConstraintValidatorContext context, String customErrorMessage) {
    if (stringToCheck == null || stringToCheck.isEmpty()) {
      return true;
    }

    final var invalidChars =
        stringToCheck
            .codePoints()
            .filter(c -> !ISO_8859_1_ENCODER.canEncode((char) c) || isInvalidControlCharacter(c))
            .distinct()
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();

    if (invalidChars.isEmpty()) {
      return true;
    }

    context.disableDefaultConstraintViolation();
    context
        .buildConstraintViolationWithTemplate(
            customErrorMessage + ": " + String.join(", ", invalidChars))
        .addConstraintViolation();

    return false;
  }

  private static boolean isInvalidControlCharacter(int character) {
    return Character.isISOControl(character) && !(ALLOWED_CHAR_SET.contains((char) character));
  }
}
