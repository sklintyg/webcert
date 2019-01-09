/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.fkstub;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswerresponder.v1.AnswerToFkType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.QuestionToFkType;

/**
 * @author andreaskaltenbach
 */
@Component
public class QuestionAnswerStore {

    private final Map<String, QuestionToFkType> questions = new ConcurrentHashMap<>();
    private final Map<String, AnswerToFkType> answers = new ConcurrentHashMap<>();

    public Map<String, QuestionToFkType> getQuestions() {
        return questions;
    }

    public Map<String, AnswerToFkType> getAnswers() {
        return answers;
    }

    public void addQuestion(QuestionToFkType question) {
        questions.put(question.getVardReferensId(), question);
    }

    public void addAnswer(AnswerToFkType answer) {
        answers.put(answer.getVardReferensId(), answer);
    }
}
