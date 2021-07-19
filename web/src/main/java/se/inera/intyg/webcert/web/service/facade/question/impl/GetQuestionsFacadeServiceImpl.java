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
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeDraft;
import se.inera.intyg.webcert.web.service.arende.ArendeDraftService;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.facade.question.GetQuestionsFacadeService;
import se.inera.intyg.webcert.web.service.facade.question.util.QuestionConverter;

@Service
public class GetQuestionsFacadeServiceImpl implements GetQuestionsFacadeService {

    private final ArendeService arendeService;
    private final ArendeDraftService arendeDraftService;
    private final QuestionConverter questionConverter;

    @Autowired
    public GetQuestionsFacadeServiceImpl(ArendeService arendeService,
        ArendeDraftService arendeDraftService, QuestionConverter questionConverter) {
        this.arendeService = arendeService;
        this.arendeDraftService = arendeDraftService;
        this.questionConverter = questionConverter;
    }

    @Override
    public List<Question> getQuestions(String certificateId) {
        final var arendenInternal = arendeService.getArendenInternal(certificateId);
        final var arendeDraft = arendeDraftService.listAnswerDrafts(certificateId);

        final var answersMap = getAnswersMap(arendenInternal);

        final var answersDraftMap = getAnswersDraftMap(arendeDraft);

        final var questionList = getQuestionList(arendenInternal, answersMap, answersDraftMap);

        final var questionDraft = getQuestionDraft(certificateId);
        if (questionDraft != null) {
            questionList.add(questionDraft);
        }

        return questionList;
    }


    private Map<String, ArendeDraft> getAnswersDraftMap(List<ArendeDraft> arendeDraft) {
        return arendeDraft.stream()
            .collect(Collectors.toMap(ArendeDraft::getQuestionId, Function.identity()));
    }


    private Map<String, Arende> getAnswersMap(List<Arende> arendenInternal) {
        return arendenInternal.stream()
            .filter(isAnswer())
            .collect(Collectors.toMap(Arende::getSvarPaId, Function.identity()));
    }

    private List<Question> getQuestionList(List<Arende> questions, Map<String, Arende> answersMap,
        Map<String, ArendeDraft> answersDraftMap) {
        return questions.stream()
            .filter(isQuestion())
            .map(question -> convertQuestion(
                question,
                answersDraftMap.get(question.getMeddelandeId()),
                answersMap.get(question.getMeddelandeId()))
            )
            .collect(Collectors.toList());
    }

    private Question convertQuestion(Arende question, ArendeDraft answerDraft, Arende answer) {
        if (answer != null) {
            return questionConverter.convert(question, answer);
        }

        if (answerDraft != null) {
            return questionConverter.convert(question, answerDraft);
        }

        return questionConverter.convert(question);
    }

    private Question getQuestionDraft(String certificateId) {
        final var questionDraft = arendeDraftService.getQuestionDraft(certificateId);
        if (questionDraft != null) {
            return questionConverter.convert(questionDraft);
        }
        return null;
    }


    private Predicate<Arende> isQuestion() {
        return arende -> arende.getSvarPaId() == null || arende.getSvarPaId().isBlank();
    }

    private Predicate<ArendeDraft> isAnswerDraft() {
        return arendeDraft -> arendeDraft.getQuestionId() != null && !arendeDraft.getQuestionId().isBlank();
    }

    private Predicate<Arende> isAnswer() {
        return arende -> arende.getSvarPaId() != null && !arende.getSvarPaId().isBlank();
    }
}
