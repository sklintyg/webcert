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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.inera.intyg.common.support.facade.model.question.Question;

class EncodingValidatorQuestionTest {

  private EncodingValidatorQuestion validator;
  private ConstraintValidatorContext context;
  private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

  @BeforeEach
  void setUp() {
    validator = new EncodingValidatorQuestion();
    context = mock(ConstraintValidatorContext.class);
    violationBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
    when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
  }

  @Test
  void shallReturnFalseForNullQuestion() {
    assertFalse(validator.isValid(null, context));
  }

  @Test
  void shallReturnTrueForNullMessage() {
    final var question = Question.builder().message(null).build();
    assertTrue(validator.isValid(question, context));
  }

  @Test
  void shallReturnTrueForEmptyMessage() {
    final var question = Question.builder().message("").build();
    assertTrue(validator.isValid(question, context));
  }

  @Test
  void shallReturnTrueForValidIso88591Message() {
    final var question = Question.builder().message("Hello World åäö").build();
    assertTrue(validator.isValid(question, context));
  }

  @Test
  void shallReturnTrueForAllowedControlCharacters() {
    final var question = Question.builder().message("Line1\nLine2\rLine3\tEnd").build();
    assertTrue(validator.isValid(question, context));
  }

  @Test
  void shallReturnFalseForNonIso88591Characters() {
    final var question = Question.builder().message("Hello 世界").build();
    assertFalse(validator.isValid(question, context));
  }

  @Test
  void shallReturnFalseForInvalidControlCharacters() {
    final var question = Question.builder().message("Hello\u0000World").build();
    assertFalse(validator.isValid(question, context));
  }

  @Test
  void shallReturnFalseForEmoji() {
    final var question = Question.builder().message("Hello 😀").build();
    assertFalse(validator.isValid(question, context));
  }
}
