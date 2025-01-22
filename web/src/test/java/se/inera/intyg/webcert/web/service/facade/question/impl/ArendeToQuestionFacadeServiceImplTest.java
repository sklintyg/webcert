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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.common.support.facade.model.question.Answer;
import se.inera.intyg.common.support.facade.model.question.Complement;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.common.support.facade.model.question.Reminder;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.arende.model.ArendeDraft;
import se.inera.intyg.webcert.persistence.arende.model.MedicinsktArende;
import se.inera.intyg.webcert.web.service.arende.ArendeDraftService;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.facade.GetCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.question.util.ComplementConverter;
import se.inera.intyg.webcert.web.service.facade.question.util.QuestionConverter;

@ExtendWith(MockitoExtension.class)
public class ArendeToQuestionFacadeServiceImplTest {

    @Mock
    private ArendeService arendeService;

    @Mock
    private ArendeDraftService arendeDraftService;

    @Mock
    private QuestionConverter questionConverter;

    @Mock
    private ComplementConverter complementConverter;

    @Mock
    private GetCertificateFacadeService getCertificateFacadeService;

    @InjectMocks
    private ArendeToQuestionFacadeServiceImpl arendeToQuestionFacadeService;

    private static final String CERTIFICATE_ID = "certificateId";
    private Certificate certificate;

    @Test
    void shallReturnEmptyQuestionsIfNoQuestionDraft() {
        doReturn(null)
            .when(arendeDraftService)
            .getQuestionDraft(CERTIFICATE_ID);

        final var actualQuestions = arendeToQuestionFacadeService.getQuestions(CERTIFICATE_ID);

        assertTrue(actualQuestions.isEmpty(), "Don't expect any questions");
    }

    @Test
    void shallReturnOneQuestionIfQuestionDraft() {
        setupMockToReturnQuestionDraft();

        final var actualQuestions = arendeToQuestionFacadeService.getQuestions(CERTIFICATE_ID);

        assertEquals(1, actualQuestions.size(), "Expect one question to be returned");
    }

    @Test
    void shallReturnEmptyQuestionsIfQuestions() {
        doReturn(Collections.emptyList())
            .when(arendeService)
            .getArendenInternal(CERTIFICATE_ID);

        final var actualQuestions = arendeToQuestionFacadeService.getQuestions(CERTIFICATE_ID);

        assertTrue(actualQuestions.isEmpty(), "Don't expect any questions");
    }

    @Test
    void shallReturnQuestionsIfQuestions() {
        setupMockToReturnQuestions();

        final var actualQuestions = arendeToQuestionFacadeService.getQuestions(CERTIFICATE_ID);

        assertEquals(3, actualQuestions.size(), "Expect three question to be returned");
    }

    @Test
    void shallReturnQuestionsIfQuestionWithReminder() {
        final var remindersArgCaptor = setupMockToReturnQuestionsWithReminder();

        final var actualQuestions = arendeToQuestionFacadeService.getQuestions(CERTIFICATE_ID);

        assertEquals(1, actualQuestions.size(), "Expect one question");
        assertNotNull(actualQuestions.get(0).getReminders()[0], "Expect a reminder for the question");
        assertNotNull(remindersArgCaptor.getValue().get(0), "Expect a reminder to be converted");
    }

    @Test
    void shallReturnQuestionsIfQuestionsAndQuestionDraft() {
        setupMockToReturnQuestions();

        setupMockToReturnQuestionDraft();

        final var actualQuestions = arendeToQuestionFacadeService.getQuestions(CERTIFICATE_ID);

        assertEquals(4, actualQuestions.size(), "Expect four question to be returned");
    }

    @Test
    void shallReturnQuestionWithAnswer() {
        setupMockToReturnQuestionWithAnswer();

        final var actualQuestions = arendeToQuestionFacadeService.getQuestions(CERTIFICATE_ID);

        assertEquals(1, actualQuestions.size(), "Expect one question");
        assertNotNull(actualQuestions.get(0).getAnswer(), "Expect an answer for the question");
    }

    @Test
    void shallReturnQuestionWithAnswerAndReminder() {
        final var remindersArgCaptor = setupMockToReturnQuestionWithAnswerAndReminder();

        final var actualQuestions = arendeToQuestionFacadeService.getQuestions(CERTIFICATE_ID);

        assertEquals(1, actualQuestions.size(), "Expect one question");
        assertNotNull(actualQuestions.get(0).getReminders()[0], "Expect a reminder for the question");
        assertNotNull(remindersArgCaptor.getValue().get(0), "Expect a reminder to be converted");
    }

    @Test
    void shallReturnQuestionWithAnswerDraft() {
        setupMockToReturnQuestionWithAnswerDraft();

        final var actualQuestions = arendeToQuestionFacadeService.getQuestions(CERTIFICATE_ID);

        assertEquals(1, actualQuestions.size(), "Expect one question");
        assertNotNull(actualQuestions.get(0).getAnswer(), "Expect an answer for the question");
    }

    @Test
    void shallReturnQuestionWithReminder() {
        final var remindersArgCaptor = setupMockToReturnQuestionWithReminder();

        final var actualQuestions = arendeToQuestionFacadeService.getQuestions(CERTIFICATE_ID);

        assertEquals(1, actualQuestions.size(), "Expect one question");
        assertNotNull(actualQuestions.get(0).getReminders()[0], "Expect a reminder for the question");
        assertNotNull(remindersArgCaptor.getValue().get(0), "Expect a reminder to be converted");
    }

    @Test
    void shallReturnQuestionWithComplement() {
        final var complementArgCaptor = setupMockToReturnQuestionsWithComplement();

        final var actualQuestions = arendeToQuestionFacadeService.getQuestions(CERTIFICATE_ID);

        assertEquals(1, actualQuestions.size(), "Expect one question");
        assertNotNull(actualQuestions.get(0).getComplements()[0], "Expect a complement for the question");
        assertNotNull(complementArgCaptor.getValue().get(0), "Expect a complement to be converted");
    }

    @Test
    void shallReturnOnlyQuestionWithComplement() {
        final var complementArgCaptor = setupMockToReturnQuestionsWithComplement();

        final var actualQuestions = arendeToQuestionFacadeService.getComplementQuestions(CERTIFICATE_ID);

        assertEquals(1, actualQuestions.size(), "Expect one question");
        assertNotNull(actualQuestions.get(0).getComplements()[0], "Expect a complement for the question");
        assertNotNull(complementArgCaptor.getValue().get(0), "Expect a complement to be converted");
    }

    @Test
    void shallReturnQuestionWithComplementAndAnsweredByCertificate() {
        final var answeredByCertificate = CertificateRelation.builder()
            .type(CertificateRelationType.COMPLEMENTED)
            .created(LocalDateTime.now().plus(1, ChronoUnit.DAYS))
            .build();

        final var answeredByCertificateArgCaptor =
            setupMockToReturnQuestionsWithComplementAndAnsweredByCertificate(new CertificateRelation[]{answeredByCertificate});

        final var actualQuestions = arendeToQuestionFacadeService.getQuestions(CERTIFICATE_ID);

        assertEquals(1, actualQuestions.size(), "Expect one question");
        assertEquals(answeredByCertificate, answeredByCertificateArgCaptor.getValue());
    }

    @Test
    void shallReturnQuestionWithComplementButNoAnsweredByCertificateIfRevoked() {
        final var answeredByCertificate = CertificateRelation.builder()
            .type(CertificateRelationType.COMPLEMENTED)
            .created(LocalDateTime.now().plus(1, ChronoUnit.DAYS))
            .status(CertificateStatus.REVOKED)
            .build();

        final var answeredByCertificateArgCaptor =
            setupMockToReturnQuestionsWithComplementAndAnsweredByCertificate(new CertificateRelation[]{answeredByCertificate});

        final var actualQuestions = arendeToQuestionFacadeService.getQuestions(CERTIFICATE_ID);

        assertEquals(1, actualQuestions.size(), "Expect one question");
        assertNull(answeredByCertificateArgCaptor.getValue(), "Expect answeredByCertificate to be null because it is revoked");
    }

    @Test
    void shallReturnQuestionWithAnsweredByCertificateAfterQuestionSent() {
        final var answeredByCertificateBeforeQuestion = CertificateRelation.builder()
            .type(CertificateRelationType.COMPLEMENTED)
            .created(LocalDateTime.now().minus(1, ChronoUnit.DAYS))
            .build();

        final var answeredByCertificate = CertificateRelation.builder()
            .type(CertificateRelationType.COMPLEMENTED)
            .created(LocalDateTime.now().plus(1, ChronoUnit.DAYS))
            .build();

        final var answeredByCertificateArgCaptor =
            setupMockToReturnQuestionsWithComplementAndAnsweredByCertificate(
                new CertificateRelation[]{answeredByCertificateBeforeQuestion, answeredByCertificate});

        final var actualQuestions = arendeToQuestionFacadeService.getQuestions(CERTIFICATE_ID);

        assertEquals(1, actualQuestions.size(), "Expect one question");
        assertEquals(answeredByCertificate, answeredByCertificateArgCaptor.getValue());
    }

    @Test
    void shallReturnQuestionWithFirstAnsweredByCertificateAfterQuestionSent() {
        final var answeredByCertificateFirst = CertificateRelation.builder()
            .type(CertificateRelationType.COMPLEMENTED)
            .created(LocalDateTime.now().plus(1, ChronoUnit.DAYS))
            .build();

        final var answeredByCertificateSecond = CertificateRelation.builder()
            .type(CertificateRelationType.COMPLEMENTED)
            .created(LocalDateTime.now().plus(2, ChronoUnit.DAYS))
            .build();

        final var answeredByCertificateArgCaptor =
            setupMockToReturnQuestionsWithComplementAndAnsweredByCertificate(
                new CertificateRelation[]{answeredByCertificateFirst, answeredByCertificateSecond});

        final var actualQuestions = arendeToQuestionFacadeService.getQuestions(CERTIFICATE_ID);

        assertEquals(1, actualQuestions.size(), "Expect one question");
        assertEquals(answeredByCertificateFirst, answeredByCertificateArgCaptor.getValue());
    }

    @Test
    void shallNotReturnQuestionWithAnsweredByCertificateIfNoRelationFound() {
        final var copiedRelation = CertificateRelation.builder()
            .type(CertificateRelationType.COPIED)
            .created(LocalDateTime.now().minus(1, ChronoUnit.DAYS))
            .build();

        final var replacedRelation = CertificateRelation.builder()
            .type(CertificateRelationType.REPLACED)
            .created(LocalDateTime.now().plus(1, ChronoUnit.DAYS))
            .build();

        final var answeredByCertificateArgCaptor =
            setupMockToReturnQuestionsWithComplementAndAnsweredByCertificate(new CertificateRelation[]{copiedRelation, replacedRelation});

        final var actualQuestions = arendeToQuestionFacadeService.getQuestions(CERTIFICATE_ID);

        assertEquals(1, actualQuestions.size(), "Expect one question");
        assertNull(answeredByCertificateArgCaptor.getValue());
    }

    private void setupMockToReturnQuestionDraft() {
        doReturn(new ArendeDraft())
            .when(arendeDraftService)
            .getQuestionDraft(CERTIFICATE_ID);

        doReturn(Question.builder().build())
            .when(questionConverter)
            .convert(any(ArendeDraft.class));
    }

    private void setupMockToReturnQuestions() {
        doReturn(List.of(new Arende(), new Arende(), new Arende()))
            .when(arendeService)
            .getArendenInternal(CERTIFICATE_ID);

        doReturn(Question.builder().build())
            .when(questionConverter)
            .convert(any(Arende.class), any(Complement[].class), any(), any(List.class));
    }

    private ArgumentCaptor<List> setupMockToReturnQuestionsWithComplement() {
        final var question = new Arende();
        question.setMeddelandeId("questionId");
        question.setAmne(ArendeAmne.KOMPLT);
        question.setKomplettering(List.of(new MedicinsktArende()));

        doReturn(List.of(question))
            .when(arendeService)
            .getArendenInternal(CERTIFICATE_ID);

        final var complement = Complement.builder().build();
        final var complements = new Complement[]{complement};
        final var complementArgCaptor = ArgumentCaptor.forClass(List.class);
        doReturn(Map.of(question.getMeddelandeId(), complements))
            .when(complementConverter)
            .convert(complementArgCaptor.capture());

        certificate = new Certificate();
        certificate.setMetadata(
            CertificateMetadata.builder()
                .relations(
                    CertificateRelations.builder().build()
                )
                .build()
        );
        doReturn(certificate)
            .when(getCertificateFacadeService)
            .getCertificate(CERTIFICATE_ID, false, true);

        doReturn(Question.builder()
            .complements(complements)
            .type(QuestionType.COMPLEMENT)
            .build())
            .when(questionConverter)
            .convert(eq(question), eq(complements), any(), anyList());

        return complementArgCaptor;
    }

    private ArgumentCaptor<CertificateRelation> setupMockToReturnQuestionsWithComplementAndAnsweredByCertificate(
        CertificateRelation[] certificateRelations) {
        final var question = new Arende();
        question.setMeddelandeId("questionId");
        question.setAmne(ArendeAmne.KOMPLT);
        question.setKomplettering(List.of(new MedicinsktArende()));
        question.setSkickatTidpunkt(LocalDateTime.now());

        doReturn(List.of(question))
            .when(arendeService)
            .getArendenInternal(CERTIFICATE_ID);

        final var complement = Complement.builder().build();
        final var complements = new Complement[]{complement};
        doReturn(Map.of(question.getMeddelandeId(), complements))
            .when(complementConverter)
            .convert(anyList());

        certificate = new Certificate();
        certificate.setMetadata(
            CertificateMetadata.builder()
                .relations(
                    CertificateRelations.builder()
                        .children(certificateRelations)
                        .build()
                )
                .build()
        );
        doReturn(certificate)
            .when(getCertificateFacadeService)
            .getCertificate(CERTIFICATE_ID, false, true);

        final var answeredByCertificateArgCaptor = ArgumentCaptor.forClass(CertificateRelation.class);
        doReturn(Question.builder()
            .complements(complements)
            .build())
            .when(questionConverter)
            .convert(eq(question), eq(complements), answeredByCertificateArgCaptor.capture(), anyList());

        return answeredByCertificateArgCaptor;
    }

    private ArgumentCaptor<List> setupMockToReturnQuestionsWithReminder() {
        final var question = new Arende();
        question.setMeddelandeId("questionId");

        final var reminder = new Arende();
        reminder.setMeddelandeId("reminderId");
        reminder.setPaminnelseMeddelandeId("questionId");

        doReturn(List.of(question, reminder))
            .when(arendeService)
            .getArendenInternal(CERTIFICATE_ID);

        final var remindersArgCaptor = ArgumentCaptor.forClass(List.class);
        doReturn(Question.builder()
            .reminders(new Reminder[]{Reminder.builder().build()})
            .build())
            .when(questionConverter)
            .convert(eq(question), any(Complement[].class), any(), remindersArgCaptor.capture());
        return remindersArgCaptor;
    }

    private void setupMockToReturnQuestionWithAnswer() {
        final var question = new Arende();
        question.setMeddelandeId("questionId");

        final var answer = new Arende();
        answer.setMeddelandeId("answerId");
        answer.setSvarPaId("questionId");

        doReturn(List.of(question, answer))
            .when(arendeService)
            .getArendenInternal(CERTIFICATE_ID);

        doReturn(Question.builder().answer(Answer.builder().build()).build())
            .when(questionConverter)
            .convert(eq(question), any(Complement[].class), any(), eq(answer), any(List.class));
    }

    private ArgumentCaptor<List> setupMockToReturnQuestionWithAnswerAndReminder() {
        final var question = new Arende();
        question.setMeddelandeId("questionId");

        final var answer = new Arende();
        answer.setMeddelandeId("answerId");
        answer.setSvarPaId("questionId");

        final var reminder = new Arende();
        reminder.setMeddelandeId("reminderId");
        reminder.setPaminnelseMeddelandeId("questionId");

        doReturn(List.of(question, answer, reminder))
            .when(arendeService)
            .getArendenInternal(CERTIFICATE_ID);

        final var remindersArgCaptor = ArgumentCaptor.forClass(List.class);
        doReturn(Question.builder()
            .answer(Answer.builder().build())
            .reminders(new Reminder[]{Reminder.builder().build()})
            .build())
            .when(questionConverter)
            .convert(eq(question), any(Complement[].class), any(), eq(answer), remindersArgCaptor.capture());
        return remindersArgCaptor;
    }

    private void setupMockToReturnQuestionWithAnswerDraft() {
        final var question = new Arende();
        question.setMeddelandeId("questionId");

        final var answer = new ArendeDraft();
        answer.setQuestionId("questionId");

        doReturn(List.of(question))
            .when(arendeService)
            .getArendenInternal(CERTIFICATE_ID);

        doReturn(List.of(answer))
            .when(arendeDraftService)
            .listAnswerDrafts(CERTIFICATE_ID);

        doReturn(Question.builder().answer(Answer.builder().build()).build())
            .when(questionConverter)
            .convert(eq(question), any(Complement[].class), any(), eq(answer), any(List.class));
    }

    private ArgumentCaptor<List> setupMockToReturnQuestionWithReminder() {
        final var question = new Arende();
        question.setMeddelandeId("questionId");

        final var reminder = new Arende();
        reminder.setMeddelandeId("reminderId");
        reminder.setPaminnelseMeddelandeId("questionId");

        doReturn(List.of(question, reminder))
            .when(arendeService)
            .getArendenInternal(CERTIFICATE_ID);

        final var remindersArgCaptor = ArgumentCaptor.forClass(List.class);
        doReturn(
            Question.builder()
                .reminders(new Reminder[]{Reminder.builder().build()})
                .build())
            .when(questionConverter)
            .convert(eq(question), any(Complement[].class), any(), remindersArgCaptor.capture());
        return remindersArgCaptor;
    }
}
