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
package se.inera.intyg.webcert.infra.srs.model;

import com.google.common.collect.ImmutableList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import se.inera.intyg.clinicalprocess.healthcond.srs.getpredictionquestions.v1.Prediktionsfraga;
import se.inera.intyg.clinicalprocess.healthcond.srs.getpredictionquestions.v1.Svarsalternativ;

public final class SrsQuestion {

  private String questionId;
  private String text;
  private String helpText;
  private int priority;
  private ImmutableList<AnswerOption> answerOptions;

  public SrsQuestion(
      String questionId,
      String text,
      List<AnswerOption> answerOptions,
      String helpText,
      int priority) {
    this.questionId = questionId;
    this.text = text;
    this.helpText = helpText;
    this.answerOptions =
        ImmutableList.copyOf(
            answerOptions.stream()
                .sorted(Comparator.comparing(AnswerOption::getPriority))
                .collect(Collectors.toList()));
    this.priority = priority;
  }

  public static SrsQuestion convert(Prediktionsfraga source) {
    return new SrsQuestion(
        source.getFrageidSrs(),
        source.getFragetext(),
        source.getSvarsalternativ().stream()
            .map(AnswerOption::convert)
            .collect(Collectors.toList()),
        source.getHjalptext(),
        source.getPrioritet().intValueExact());
  }

  public String getQuestionId() {
    return questionId;
  }

  public String getText() {
    return text;
  }

  public String getHelpText() {
    return helpText;
  }

  public List<AnswerOption> getAnswerOptions() {
    return answerOptions;
  }

  public int getPriority() {
    return priority;
  }

  public static final class AnswerOption {

    private final String text;
    private final String id;
    private final int priority;
    private final boolean defaultValue;

    public AnswerOption(String id, String text, int priority, boolean defaultValue) {
      this.text = text;
      this.id = id;
      this.priority = priority;
      this.defaultValue = defaultValue;
    }

    public static AnswerOption convert(Svarsalternativ source) {
      return new AnswerOption(
          source.getSvarsidSrs(),
          source.getSvarstext(),
          source.getPrioritet().intValueExact(),
          source.isDefault());
    }

    public String getText() {
      return text;
    }

    public String getId() {
      return id;
    }

    public int getPriority() {
      return priority;
    }

    public boolean isDefaultValue() {
      return defaultValue;
    }
  }
}
