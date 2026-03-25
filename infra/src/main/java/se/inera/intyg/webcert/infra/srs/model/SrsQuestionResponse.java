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
package se.inera.intyg.webcert.infra.srs.model;

import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v3.FragaSvar;

public class SrsQuestionResponse {

  private String questionId;
  private String answerId;

  public static FragaSvar convert(SrsQuestionResponse srsQuestionResponse) {
    FragaSvar fragaSvar = new FragaSvar();
    fragaSvar.setFrageidSrs(srsQuestionResponse.getQuestionId());

    fragaSvar.setSvarsidSrs(srsQuestionResponse.getAnswerId());
    return fragaSvar;
  }

  public static SrsQuestionResponse create(String questionId, String answerId) {
    SrsQuestionResponse sqr = new SrsQuestionResponse();
    sqr.setQuestionId(questionId);
    sqr.setAnswerId(answerId);
    return sqr;
  }

  public String getQuestionId() {
    return questionId;
  }

  public void setQuestionId(String questionId) {
    this.questionId = questionId;
  }

  public String getAnswerId() {
    return answerId;
  }

  public void setAnswerId(String answerId) {
    this.answerId = answerId;
  }
}
