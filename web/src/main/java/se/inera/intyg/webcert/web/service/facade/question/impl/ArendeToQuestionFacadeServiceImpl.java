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
package se.inera.intyg.webcert.web.service.facade.question.impl;

import static java.util.Comparator.comparing;
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
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.common.support.facade.model.question.Complement;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeDraft;
import se.inera.intyg.webcert.web.service.arende.ArendeDraftService;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.facade.GetCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.question.GetQuestionsFacadeService;
import se.inera.intyg.webcert.web.service.facade.question.util.ComplementConverter;
import se.inera.intyg.webcert.web.service.facade.question.util.QuestionConverter;

@Service(value = "ArendeToQuestionFacadeService")
public class ArendeToQuestionFacadeServiceImpl implements GetQuestionsFacadeService {

    private final ArendeService arendeService;
    private final ArendeDraftService arendeDraftService;
    private final QuestionConverter questionConverter;
    private final ComplementConverter complementConverter;
    private final GetCertificateFacadeService getCertificateFacadeService;

    @Autowired
    public ArendeToQuestionFacadeServiceImpl(ArendeService arendeService,
        ArendeDraftService arendeDraftService, QuestionConverter questionConverter,
        ComplementConverter complementConverter, GetCertificateFacadeService getCertificateFacadeService) {
        this.arendeService = arendeService;
        this.arendeDraftService = arendeDraftService;
        this.questionConverter = questionConverter;
        this.complementConverter = complementConverter;
        this.getCertificateFacadeService = getCertificateFacadeService;
    }

    @Override
    public List<Question> getComplementQuestions(String certificateId) {
        return getQuestions(certificateId).stream()
            .filter(question -> question.getType() == QuestionType.COMPLEMENT)
            .collect(Collectors.toList());
    }

    @Override
    public List<Question> getQuestions(String certificateId) {
        final var arendenInternal = arendeService.getArendenInternal(certificateId);
        final var arendeDraft = arendeDraftService.listAnswerDrafts(certificateId);

        final var complementsMap = getComplementsMap(arendenInternal);

        final var answersByCertificate = getAnswersByCertificate(certificateId, complementsMap);

        final var answersMap = getAnswersMap(arendenInternal);

        final var answersDraftMap = getAnswersDraftMap(arendeDraft);

        final var remindersMap = getRemindersMap(arendenInternal);

        final var questionList = getQuestionList(arendenInternal, complementsMap, answersByCertificate, answersMap, remindersMap,
            answersDraftMap);

        final var questionDraft = getQuestionDraft(certificateId);
        if (questionDraft != null) {
            questionList.add(questionDraft);
        }

        return questionList;
    }

    private List<CertificateRelation> getAnswersByCertificate(String certificateId, Map<String, Complement[]> complementsMap) {
        if (complementsMap.isEmpty()) {
            return Collections.emptyList();
        }

        final var certificateRelations = getCertificateFacadeService
            .getCertificate(certificateId, false, true)
            .getMetadata()
            .getRelations();
        if (certificateRelations == null) {
            return Collections.emptyList();
        }

        final var childrenRelations = certificateRelations.getChildren();
        if (childrenRelations == null) {
            return Collections.emptyList();
        }

        return Stream.of(childrenRelations)
            .filter(childRelation -> childRelation.getType() == CertificateRelationType.COMPLEMENTED
                && childRelation.getStatus() != CertificateStatus.REVOKED)
            .collect(Collectors.toList());
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

    private List<Question> getQuestionList(List<Arende> questions, Map<String, Complement[]> complementsMap,
        List<CertificateRelation> answersByCertificate, Map<String, Arende> answersMap,
        Map<String, List<Arende>> remindersMap, Map<String, ArendeDraft> answersDraftMap) {
        return questions.stream()
            .filter(isQuestion())
            .map(question ->
                convertQuestion(
                    question,
                    complementsMap.getOrDefault(question.getMeddelandeId(), new Complement[0]),
                    getAnsweredByCertificate(question, answersByCertificate),
                    answersDraftMap.get(question.getMeddelandeId()),
                    answersMap.get(question.getMeddelandeId()),
                    remindersMap.getOrDefault(question.getMeddelandeId(), Collections.emptyList())
                )
            )
            .collect(Collectors.toList());
    }

    private CertificateRelation getAnsweredByCertificate(Arende question, List<CertificateRelation> answersByCertificate) {
        return answersByCertificate.stream()
            .filter(certificateRelation -> certificateRelation.getCreated().isAfter(question.getSkickatTidpunkt()))
            .min(comparing(CertificateRelation::getCreated))
            .orElse(null);
    }

    private Question convertQuestion(Arende question, Complement[] complements, CertificateRelation answeredByCertificate,
        ArendeDraft answerDraft, Arende answer, List<Arende> reminders) {

        if (answer != null) {
            return questionConverter.convert(question, complements, answeredByCertificate, answer, reminders);
        }

        if (answerDraft != null) {
            return questionConverter.convert(question, complements, answeredByCertificate, answerDraft, reminders);
        }

        return questionConverter.convert(question, complements, answeredByCertificate, reminders);
    }

    private Question getQuestionDraft(String certificateId) {
        final var questionDraft = arendeDraftService.getQuestionDraft(certificateId);
        if (questionDraft != null) {
            return questionConverter.convert(questionDraft);
        }
        return null;
    }
}
