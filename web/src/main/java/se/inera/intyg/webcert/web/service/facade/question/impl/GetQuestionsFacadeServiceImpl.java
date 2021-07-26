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

import static se.inera.intyg.webcert.web.service.facade.question.util.QuestionUtil.isAnswer;
import static se.inera.intyg.webcert.web.service.facade.question.util.QuestionUtil.isComplementQuestion;
import static se.inera.intyg.webcert.web.service.facade.question.util.QuestionUtil.isQuestion;
import static se.inera.intyg.webcert.web.service.facade.question.util.QuestionUtil.isReminder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.question.Complement;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeDraft;
import se.inera.intyg.webcert.web.service.arende.ArendeDraftService;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.facade.question.GetQuestionsFacadeService;
import se.inera.intyg.webcert.web.service.facade.question.util.ComplementConverter;
import se.inera.intyg.webcert.web.service.facade.question.util.QuestionConverter;

@Service
public class GetQuestionsFacadeServiceImpl implements GetQuestionsFacadeService {

    private final ArendeService arendeService;
    private final ArendeDraftService arendeDraftService;
    private final QuestionConverter questionConverter;
    private final ComplementConverter complementConverter;

    @Autowired
    public GetQuestionsFacadeServiceImpl(ArendeService arendeService,
        ArendeDraftService arendeDraftService, QuestionConverter questionConverter,
        ComplementConverter complementConverter) {
        this.arendeService = arendeService;
        this.arendeDraftService = arendeDraftService;
        this.questionConverter = questionConverter;
        this.complementConverter = complementConverter;
    }

    @Override
    public List<Question> getQuestions(String certificateId) {
        final var arendenInternal = arendeService.getArendenInternal(certificateId);
        final var arendeDraft = arendeDraftService.listAnswerDrafts(certificateId);

        final var complementsMap = getComplementsMap(arendenInternal);

        final var answersMap = getAnswersMap(arendenInternal);

        final var answersDraftMap = getAnswersDraftMap(arendeDraft);

        final var remindersMap = getRemindersMap(arendenInternal);

        final var questionList = getQuestionList(arendenInternal, complementsMap, answersMap, remindersMap, answersDraftMap);

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

    private Map<String, Complement[]> getComplementsMap(List<Arende> arendenInternal) {
        final var complementQuestions = arendenInternal.stream()
            .filter(isComplementQuestion())
            .collect(Collectors.toList());

        if (complementQuestions.isEmpty()) {
            return Collections.emptyMap();
        }

        return complementConverter.convert(complementQuestions);
    }

    private Map<String, Arende> getAnswersMap(List<Arende> arendenInternal) {
        return arendenInternal.stream()
            .filter(isAnswer())
            .collect(Collectors.toMap(Arende::getSvarPaId, Function.identity()));
    }

    private Map<String, List<Arende>> getRemindersMap(List<Arende> arendenInternal) {
        return arendenInternal.stream()
            .filter(isReminder())
            .collect(Collectors.groupingBy(Arende::getPaminnelseMeddelandeId, HashMap::new, Collectors.toCollection(ArrayList::new)));
    }

    private List<Question> getQuestionList(List<Arende> questions, Map<String, Complement[]> complementsMap, Map<String, Arende> answersMap,
        Map<String, List<Arende>> remindersMap, Map<String, ArendeDraft> answersDraftMap) {
        return questions.stream()
            .filter(isQuestion())
            .map(question ->
                convertQuestion(
                    question,
                    complementsMap.getOrDefault(question.getMeddelandeId(), new Complement[0]),
                    answersDraftMap.get(question.getMeddelandeId()),
                    answersMap.get(question.getMeddelandeId()),
                    remindersMap.getOrDefault(question.getMeddelandeId(), Collections.emptyList())
                )
            )
            .collect(Collectors.toList());
    }

    private Question convertQuestion(Arende question, Complement[] complements, ArendeDraft answerDraft, Arende answer,
        List<Arende> reminders) {
        if (answer != null) {
            return questionConverter.convert(question, complements, answer, reminders);
        }

        if (answerDraft != null) {
            return questionConverter.convert(question, complements, answerDraft, reminders);
        }

        return questionConverter.convert(question, complements, reminders);
    }

    private Question getQuestionDraft(String certificateId) {
        final var questionDraft = arendeDraftService.getQuestionDraft(certificateId);
        if (questionDraft != null) {
            return questionConverter.convert(questionDraft);
        }
        return null;
    }
}
