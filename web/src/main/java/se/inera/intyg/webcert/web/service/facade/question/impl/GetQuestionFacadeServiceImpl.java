/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.facade.question.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeDraft;
import se.inera.intyg.webcert.web.service.arende.ArendeDraftService;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.facade.question.GetQuestionFacadeService;
import se.inera.intyg.webcert.web.service.facade.question.util.QuestionConverter;

@Service
public class GetQuestionFacadeServiceImpl implements GetQuestionFacadeService {

    private final ArendeService arendeService;
    private final ArendeDraftService arendeDraftService;
    private final QuestionConverter questionConverter;

    @Autowired
    public GetQuestionFacadeServiceImpl(ArendeService arendeService,
        ArendeDraftService arendeDraftService, QuestionConverter questionConverter) {
        this.arendeService = arendeService;
        this.arendeDraftService = arendeDraftService;
        this.questionConverter = questionConverter;
    }

    @Override
    public Question get(String questionId) {
        final var question = getQuestion(questionId);
        final var relatedArenden = arendeService.getRelatedArenden(questionId);

        final var answer = getAnswer(questionId, relatedArenden);
        if (hasAnswer(answer)) {
            return questionConverter.convert(question, answer);
        }

        final var answerDraft = getAnswerDraft(questionId, question);
        if (hasAnswerDraft(answerDraft)) {
            return questionConverter.convert(question, answerDraft);
        }

        return questionConverter.convert(question);
    }


    private Arende getQuestion(String questionId) {
        return arendeService.getArende(questionId);
    }

    private Arende getAnswer(String questionId, List<Arende> relatedArenden) {
        return relatedArenden.stream()
            .filter(relatedArende -> questionId.equalsIgnoreCase(relatedArende.getSvarPaId()))
            .findFirst()
            .orElse(null);
    }

    private ArendeDraft getAnswerDraft(String questionId, Arende question) {
        return arendeDraftService.getAnswerDraft(question.getIntygsId(), questionId);
    }

    private boolean hasAnswerDraft(ArendeDraft answerDraft) {
        return answerDraft != null;
    }

    private boolean hasAnswer(Arende arendeSvar) {
        return arendeSvar != null;
    }
}
