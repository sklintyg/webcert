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
import static se.inera.intyg.webcert.web.service.facade.question.util.QuestionUtil.isReminder;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.common.support.facade.model.question.Complement;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.arende.model.ArendeDraft;
import se.inera.intyg.webcert.web.service.arende.ArendeDraftService;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.facade.GetCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.question.GetQuestionFacadeService;
import se.inera.intyg.webcert.web.service.facade.question.util.ComplementConverter;
import se.inera.intyg.webcert.web.service.facade.question.util.QuestionConverter;

@Service
public class GetQuestionFacadeServiceImpl implements GetQuestionFacadeService {

    private final ArendeService arendeService;
    private final ArendeDraftService arendeDraftService;
    private final QuestionConverter questionConverter;
    private final ComplementConverter complementConverter;
    private final GetCertificateFacadeService getCertificateFacadeService;

    @Autowired
    public GetQuestionFacadeServiceImpl(ArendeService arendeService,
        ArendeDraftService arendeDraftService, QuestionConverter questionConverter,
        ComplementConverter complementConverter,
        GetCertificateFacadeService getCertificateFacadeService) {
        this.arendeService = arendeService;
        this.arendeDraftService = arendeDraftService;
        this.questionConverter = questionConverter;
        this.complementConverter = complementConverter;
        this.getCertificateFacadeService = getCertificateFacadeService;
    }

    @Override
    public Question get(String questionId) {
        final var question = getQuestion(questionId);
        final var relatedArenden = arendeService.getRelatedArenden(questionId);

        final var complements = getComplements(question);

        final var answeredByCertificate = getAnsweredByCertificate(question, complements);

        final var reminders = getReminders(relatedArenden);

        final var answer = getAnswer(relatedArenden);
        if (hasAnswer(answer)) {
            return questionConverter.convert(question, complements, answeredByCertificate, answer, reminders);
        }

        final var answerDraft = getAnswerDraft(questionId, question);
        if (hasAnswerDraft(answerDraft)) {
            return questionConverter.convert(question, complements, answeredByCertificate, answerDraft, reminders);
        }

        return questionConverter.convert(question, complements, answeredByCertificate, reminders);
    }

    private CertificateRelation getAnsweredByCertificate(Arende question, Complement[] complements) {
        if (complements.length == 0) {
            return null;
        }

        final var certificate = getCertificateFacadeService.getCertificate(question.getIntygsId(), false, true);
        final var relations = certificate.getMetadata().getRelations();
        if (relations == null) {
            return null;
        }

        final var children = relations.getChildren();
        if (children == null || children.length == 0) {
            return null;
        }

        return Stream.of(children)
            .filter(certificateRelation ->
                certificateRelation.getType() == CertificateRelationType.COMPLEMENTED
                    && certificateRelation.getCreated().isAfter(question.getSkickatTidpunkt()))
            .min(comparing(CertificateRelation::getCreated))
            .orElse(null);
    }

    private Arende getQuestion(String questionId) {
        return arendeService.getArende(questionId);
    }

    private Complement[] getComplements(Arende question) {
        if (question.getAmne() != ArendeAmne.KOMPLT || question.getKomplettering().isEmpty()) {
            return new Complement[0];
        }

        return complementConverter.convert(question);
    }

    private List<Arende> getReminders(List<Arende> relatedArenden) {
        return relatedArenden.stream()
            .filter(isReminder())
            .collect(Collectors.toList());
    }

    private Arende getAnswer(List<Arende> relatedArenden) {
        return relatedArenden.stream()
            .filter(isAnswer())
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
