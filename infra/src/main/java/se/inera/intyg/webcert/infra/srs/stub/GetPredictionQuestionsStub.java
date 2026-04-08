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
package se.inera.intyg.webcert.infra.srs.stub;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.cxf.annotations.SchemaValidation;
import se.inera.intyg.clinicalprocess.healthcond.srs.getpredictionquestions.v1.GetPredictionQuestionsRequestType;
import se.inera.intyg.clinicalprocess.healthcond.srs.getpredictionquestions.v1.GetPredictionQuestionsResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.srs.getpredictionquestions.v1.GetPredictionQuestionsResponseType;
import se.inera.intyg.clinicalprocess.healthcond.srs.getpredictionquestions.v1.Prediktionsfraga;
import se.inera.intyg.clinicalprocess.healthcond.srs.getpredictionquestions.v1.Svarsalternativ;

@SchemaValidation(type = SchemaValidation.SchemaValidationType.BOTH)
public class GetPredictionQuestionsStub implements GetPredictionQuestionsResponderInterface {

  private static final double MAX_PRIORITY = 10;

  @Override
  public GetPredictionQuestionsResponseType getPredictionQuestions(
      GetPredictionQuestionsRequestType getPredictionQuestionsRequestType) {
    GetPredictionQuestionsResponseType response = new GetPredictionQuestionsResponseType();
    AtomicInteger i = new AtomicInteger(1);
    response.getPrediktionsfraga().add(createPrediktionsFraga(i.getAndIncrement()));
    response.getPrediktionsfraga().add(createPrediktionsFraga(i.getAndIncrement()));
    response.getPrediktionsfraga().add(createPrediktionsFraga(i.getAndIncrement()));
    return response;
  }

  private Prediktionsfraga createPrediktionsFraga(int id) {
    Prediktionsfraga question = new Prediktionsfraga();
    AtomicInteger i = new AtomicInteger(1);
    question.getSvarsalternativ().add(createAnswer(i.getAndIncrement()));
    question.getSvarsalternativ().add(createAnswer(i.getAndIncrement()));
    question.getSvarsalternativ().add(createAnswer(i.getAndIncrement()));
    question.getSvarsalternativ().add(createAnswer(i.getAndIncrement()));

    question.setFrageid(BigInteger.valueOf(id));
    question.setFrageidSrs(String.valueOf(id));
    question.setFragetext("Fragetext " + id);
    question.setHjalptext("Hjälptext " + id);
    question.setPrioritet(BigInteger.valueOf((int) (Math.random() * MAX_PRIORITY) + 1));
    return question;
  }

  private Svarsalternativ createAnswer(int id) {
    Svarsalternativ answer = new Svarsalternativ();
    answer.setDefault(id == 1);
    answer.setPrioritet(BigInteger.valueOf((int) (Math.random() * MAX_PRIORITY) + 1));
    answer.setSvarsid(BigInteger.valueOf(id));
    answer.setSvarsidSrs("stud");
    answer.setSvarstext("Svarsalternativ " + id);
    return answer;
  }
}
