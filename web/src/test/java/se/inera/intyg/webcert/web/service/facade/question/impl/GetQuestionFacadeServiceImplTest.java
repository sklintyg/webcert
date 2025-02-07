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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.common.support.facade.model.question.Answer;
import se.inera.intyg.common.support.facade.model.question.Complement;
import se.inera.intyg.common.support.facade.model.question.Question;
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
class GetQuestionFacadeServiceImplTest {

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
    private GetQuestionFacadeServiceImpl getQuestionFacadeService;

    private String certificateId = "certificateId";
    private String questionId = "questionId";
    private Arende arende;
    private Arende arendeSvar;
    private Arende arendePaminnelse;
    private List<Arende> relatedArenden;
    private List<MedicinsktArende> kompletteringar = new ArrayList<>();

    @BeforeEach
    void setUp() {
        arende = new Arende();
        arende.setMeddelandeId(questionId);
        arende.setIntygsId(certificateId);
        arende.setKomplettering(kompletteringar);
        arende.setSkickatTidpunkt(LocalDateTime.now());

        arendeSvar = new Arende();
        arendeSvar.setMeddelandeId("arendeSvarId");
        arendeSvar.setSvarPaId(arende.getMeddelandeId());

        arendePaminnelse = new Arende();
        arendePaminnelse.setMeddelandeId("arendePaminnelseId");
        arendePaminnelse.setPaminnelseMeddelandeId(arende.getMeddelandeId());

        relatedArenden = new ArrayList<>();

        doReturn(arende)
            .when(arendeService)
            .getArende(questionId);

        doReturn(relatedArenden)
            .when(arendeService)
            .getRelatedArenden(questionId);
    }

    @Test
    void shallReturnQuestion() {
        doReturn(
            Question.builder()
                .answer(Answer.builder().build())
                .build())
            .when(questionConverter)
            .convert(eq(arende), any(Complement[].class), any(), any(List.class));

        final var actualQuestion = getQuestionFacadeService.get(questionId);
        assertNotNull(actualQuestion, "Shall return a question");
    }

    @Test
    void shallReturnQuestionWithReminder() {
        relatedArenden.add(arendePaminnelse);
        final var remindersArgCaptor = ArgumentCaptor.forClass(List.class);
        doReturn(
            Question.builder()
                .reminders(new Reminder[]{Reminder.builder().build()})
                .build())
            .when(questionConverter)
            .convert(eq(arende), any(Complement[].class), any(), remindersArgCaptor.capture());

        final var actualQuestion = getQuestionFacadeService.get(questionId);
        assertNotNull(actualQuestion.getReminders()[0], "Shall return a question with reminder");
        assertEquals(arendePaminnelse, remindersArgCaptor.getValue().get(0));
    }

    @Test
    void shallReturnQuestionWithAnswer() {
        relatedArenden.add(arendeSvar);
        doReturn(
            Question.builder()
                .answer(Answer.builder().build())
                .build())
            .when(questionConverter)
            .convert(eq(arende), any(Complement[].class), any(), eq(arendeSvar), any(List.class));

        final var actualQuestion = getQuestionFacadeService.get(questionId);
        assertNotNull(actualQuestion.getAnswer(), "Shall return a question with answer");
    }

    @Test
    void shallReturnQuestionWithAnswerWithReminder() {
        relatedArenden.add(arendeSvar);
        relatedArenden.add(arendePaminnelse);
        final var remindersArgCaptor = ArgumentCaptor.forClass(List.class);
        doReturn(
            Question.builder()
                .answer(Answer.builder().build())
                .reminders(new Reminder[]{Reminder.builder().build()})
                .build())
            .when(questionConverter)
            .convert(eq(arende), any(Complement[].class), any(), eq(arendeSvar), remindersArgCaptor.capture());

        final var actualQuestion = getQuestionFacadeService.get(questionId);
        assertNotNull(actualQuestion.getReminders()[0], "Shall return a question with reminder");
        assertEquals(arendePaminnelse, remindersArgCaptor.getValue().get(0));
    }

    @Test
    void shallReturnQuestionWithAnswerDraft() {
        final var answerDraft = new ArendeDraft();
        doReturn(answerDraft)
            .when(arendeDraftService)
            .getAnswerDraft(certificateId, questionId);

        doReturn(
            Question.builder()
                .answer(Answer.builder().build())
                .build())
            .when(questionConverter)
            .convert(eq(arende), any(Complement[].class), any(), eq(answerDraft), any(List.class));

        final var actualQuestion = getQuestionFacadeService.get(questionId);
        assertNotNull(actualQuestion.getAnswer(), "Shall return a question with answer");
    }

    @Test
    void shallReturnQuestionWithAnswerDraftWithReminder() {
        final var answerDraft = new ArendeDraft();
        doReturn(answerDraft)
            .when(arendeDraftService)
            .getAnswerDraft(certificateId, questionId);
        relatedArenden.add(arendePaminnelse);
        final var remindersArgCaptor = ArgumentCaptor.forClass(List.class);
        doReturn(
            Question.builder()
                .answer(Answer.builder().build())
                .reminders(new Reminder[]{Reminder.builder().build()})
                .build())
            .when(questionConverter)
            .convert(eq(arende), any(Complement[].class), any(), eq(answerDraft), remindersArgCaptor.capture());

        final var actualQuestion = getQuestionFacadeService.get(questionId);
        assertNotNull(actualQuestion.getReminders()[0], "Shall return a question with reminder");
        assertEquals(arendePaminnelse, remindersArgCaptor.getValue().get(0));
    }

    @Test
    void shallReturnQuestionWithComplements() {
        final var complements = new Complement[]{Complement.builder().build()};
        kompletteringar.add(new MedicinsktArende());
        arende.setAmne(ArendeAmne.KOMPLT);

        doReturn(complements)
            .when(complementConverter)
            .convert(arende);

        final var certificate = new Certificate();
        certificate.setMetadata(
            CertificateMetadata.builder().build()
        );

        doReturn(certificate)
            .when(getCertificateFacadeService)
            .getCertificate(certificateId, false, true);

        doReturn(
            Question.builder()
                .answer(Answer.builder().build())
                .complements(complements)
                .build())
            .when(questionConverter)
            .convert(eq(arende), eq(complements), any(), any(List.class));

        final var actualQuestion = getQuestionFacadeService.get(questionId);
        assertNotNull(actualQuestion, "Shall return a question");
        assertEquals(complements, actualQuestion.getComplements());
    }

    @Nested
    class AnsweredByCertificate {

        private Certificate certificate;
        private ArgumentCaptor<CertificateRelation> answeredByCertificateCaptor;

        @BeforeEach
        void setUp() {
            final var complements = new Complement[]{Complement.builder().build()};
            kompletteringar.add(new MedicinsktArende());
            arende.setAmne(ArendeAmne.KOMPLT);

            doReturn(complements)
                .when(complementConverter)
                .convert(arende);

            certificate = new Certificate();

            doReturn(certificate)
                .when(getCertificateFacadeService)
                .getCertificate(certificateId, false, true);

            answeredByCertificateCaptor = ArgumentCaptor.forClass(CertificateRelation.class);
            doReturn(
                Question.builder()
                    .answeredByCertificate(CertificateRelation.builder().build())
                    .complements(complements)
                    .build())
                .when(questionConverter)
                .convert(eq(arende), eq(complements), answeredByCertificateCaptor.capture(), any(List.class));
        }

        @Test
        void shallReturnQuestionWithAnsweredByCertificate() {
            final var answeredByCertificate = CertificateRelation.builder()
                .type(CertificateRelationType.COMPLEMENTED)
                .created(LocalDateTime.now().plus(1, ChronoUnit.DAYS))
                .build();

            setCertificateRelations(new CertificateRelation[]{answeredByCertificate});

            final var actualQuestion = getQuestionFacadeService.get(questionId);
            assertNotNull(actualQuestion, "Shall return a question");
            assertEquals(answeredByCertificate, answeredByCertificateCaptor.getValue());
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

            setCertificateRelations(new CertificateRelation[]{answeredByCertificateBeforeQuestion, answeredByCertificate});

            final var actualQuestion = getQuestionFacadeService.get(questionId);
            assertNotNull(actualQuestion, "Shall return a question");
            assertEquals(answeredByCertificate, answeredByCertificateCaptor.getValue());
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

            setCertificateRelations(new CertificateRelation[]{answeredByCertificateFirst, answeredByCertificateSecond});

            final var actualQuestion = getQuestionFacadeService.get(questionId);
            assertNotNull(actualQuestion, "Shall return a question");
            assertEquals(answeredByCertificateFirst, answeredByCertificateCaptor.getValue());
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

            setCertificateRelations(new CertificateRelation[]{copiedRelation, replacedRelation});

            final var actualQuestion = getQuestionFacadeService.get(questionId);
            assertNotNull(actualQuestion, "Shall return a question");
            assertNull(answeredByCertificateCaptor.getValue());
        }

        private void setCertificateRelations(CertificateRelation[] childrenRelations) {
            certificate.setMetadata(
                CertificateMetadata.builder()
                    .relations(
                        CertificateRelations.builder()
                            .children(childrenRelations)
                            .build()
                    )
                    .build()
            );
        }
    }
}
