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
package se.inera.intyg.webcert.web.service.facade.question.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.common.support.facade.model.question.Complement;
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.arende.model.ArendeDraft;
import se.inera.intyg.webcert.persistence.model.Status;

class QuestionConverterImplTest {

    private QuestionConverter questionConverter;

    @BeforeEach
    void setUp() {
        questionConverter = new QuestionConverterImpl();
    }

    @Nested
    class ReceivedAdministativeQuestions {

        private static final String QUESTION_ID = "1000";
        private static final String CERTIFICATE_ID = "CERTIFICATE_ID";
        private static final String AUTHOR_CERTIFICATE_RECEIVER = "Försäkringskassan";
        private static final String AUTHOR = "author";
        private static final String SUBJECT_WITHOUT_HEADER = "Avstämningsmöte";
        private static final String SUBJECT_WITH_HEADER = "Avstämningsmöte - Rubrik";
        private static final String HEADER = "Rubrik";
        private static final String SENT_BY_FK = "FK";
        private static final boolean IS_HANDLED = true;
        private static final boolean IS_FORWARDED = true;
        private static final String MESSAGE = "message";

        private final LocalDateTime lastUpdate = LocalDateTime.now().plusDays(1);
        private final LocalDate lastDateToReply = LocalDate.now();
        private final LocalDateTime sent = LocalDateTime.now();

        private Arende arende;

        @BeforeEach
        void setup() {
            arende = new Arende();
            arende.setMeddelandeId(QUESTION_ID);
            arende.setVardaktorName(AUTHOR);
            arende.setAmne(ArendeAmne.AVSTMN);
            arende.setTimestamp(sent);
            arende.setSkickatAv(SENT_BY_FK);
            arende.setStatus(Status.CLOSED);
            arende.setVidarebefordrad(IS_FORWARDED);
            arende.setMeddelande(MESSAGE);
            arende.setSenasteHandelse(lastUpdate);
            arende.setSistaDatumForSvar(lastDateToReply);
            arende.setIntygsId(CERTIFICATE_ID);
        }

        @Test
        void shallReturnQuestionWithId() {
            final var actualQuestion = questionConverter.convert(arende);

            assertEquals(QUESTION_ID, actualQuestion.getId());
        }

        @Test
        void shallReturnQuestionWithCertificateId() {
            final var actualQuestion = questionConverter.convert(arende);

            assertEquals(CERTIFICATE_ID, actualQuestion.getCertificateId());
        }

        @Test
        void shallReturnQuestionWithTypeCoordination() {
            final var actualQuestion = questionConverter.convert(arende);

            assertEquals(QuestionType.COORDINATION, actualQuestion.getType());
        }

        @Test
        void shallReturnQuestionWithTypeContact() {
            arende.setAmne(ArendeAmne.KONTKT);

            final var actualQuestion = questionConverter.convert(arende);

            assertEquals(QuestionType.CONTACT, actualQuestion.getType());
        }

        @Test
        void shallReturnQuestionWithTypeOther() {
            arende.setAmne(ArendeAmne.OVRIGT);

            final var actualQuestion = questionConverter.convert(arende);

            assertEquals(QuestionType.OTHER, actualQuestion.getType());
        }

        @Test
        void shallReturnReceivedQuestionWithAuthor() {
            final var actualQuestion = questionConverter.convert(arende);

            assertEquals(AUTHOR_CERTIFICATE_RECEIVER, actualQuestion.getAuthor());
        }

        @Test
        void shallReturnSentQuestionWithAuthor() {
            arende.setSkickatAv("WC");

            final var actualQuestion = questionConverter.convert(arende);

            assertEquals(AUTHOR, actualQuestion.getAuthor());
        }

        @Test
        void shallReturnQuestionWithSubjectWithoutHeader() {
            final var actualQuestion = questionConverter.convert(arende);

            assertEquals(SUBJECT_WITHOUT_HEADER, actualQuestion.getSubject());
        }

        @Test
        void shallReturnQuestionWithSubjectWithHeader() {
            arende.setRubrik(HEADER);

            final var actualQuestion = questionConverter.convert(arende);

            assertEquals(SUBJECT_WITH_HEADER, actualQuestion.getSubject());
        }

        @Test
        void shallReturnQuestionWithSent() {
            final var actualQuestion = questionConverter.convert(arende);

            assertEquals(sent, actualQuestion.getSent());
        }

        @Test
        void shallReturnQuestionWithHandled() {
            final var actualQuestion = questionConverter.convert(arende);

            assertEquals(IS_HANDLED, actualQuestion.isHandled());
        }

        @Test
        void shallReturnQuestionWithForwarded() {
            final var actualQuestion = questionConverter.convert(arende);

            assertEquals(IS_FORWARDED, actualQuestion.isForwarded());
        }

        @Test
        void shallReturnQuestionWithMessage() {
            final var actualQuestion = questionConverter.convert(arende);

            assertEquals(MESSAGE, actualQuestion.getMessage());
        }

        @Test
        void shallReturnQuestionWithLastUpdate() {
            final var actualQuestion = questionConverter.convert(arende);

            assertEquals(lastUpdate, actualQuestion.getLastUpdate());
        }

        @Test
        void shallReturnQuestionWithLastDateToReply() {
            final var actualQuestion = questionConverter.convert(arende);

            assertEquals(lastDateToReply, actualQuestion.getLastDateToReply());
        }
    }

    @Nested
    class ReceivedComplementQuestions {

        private static final String QUESTION_ID = "1000";
        private static final String AUTHOR = "author";
        private static final String SENT_BY_FK = "FK";
        private static final boolean IS_FORWARDED = true;
        private static final String MESSAGE = "message";
        private final LocalDateTime lastUpdate = LocalDateTime.now().plusDays(1);
        private final LocalDate lastDateToReply = LocalDate.now();
        private final LocalDateTime sent = LocalDateTime.now();

        private Arende arende;
        private Complement[] expectedComplements;
        private CertificateRelation expectedAnswerByCertificate;

        @BeforeEach
        void setup() {
            arende = new Arende();
            arende.setMeddelandeId(QUESTION_ID);
            arende.setVardaktorName(AUTHOR);
            arende.setAmne(ArendeAmne.KOMPLT);
            arende.setSkickatTidpunkt(sent);
            arende.setSkickatAv(SENT_BY_FK);
            arende.setStatus(Status.CLOSED);
            arende.setVidarebefordrad(IS_FORWARDED);
            arende.setMeddelande(MESSAGE);
            arende.setSenasteHandelse(lastUpdate);
            arende.setSistaDatumForSvar(lastDateToReply);

            expectedComplements = new Complement[]{Complement.builder().build()};
            expectedAnswerByCertificate = CertificateRelation.builder().build();
        }

        @Test
        void shallReturnQuestionWithComplement() {
            final var actualQuestion = questionConverter.convert(arende, expectedComplements, null);

            assertEquals(expectedComplements, actualQuestion.getComplements());
        }

        @Test
        void shallReturnQuestionWithComplementThatHasAnswer() {
            final var actualQuestion = questionConverter.convert(arende, expectedComplements, null, new Arende(), Collections.emptyList());

            assertEquals(expectedComplements, actualQuestion.getComplements());
        }

        @Test
        void shallReturnQuestionWithComplementThatHasAnswerDraft() {
            final var actualQuestion = questionConverter
                .convert(arende, expectedComplements, null, new ArendeDraft(), Collections.emptyList());

            assertEquals(expectedComplements, actualQuestion.getComplements());
        }

        @Test
        void shallReturnQuestionWithComplementThatHasAnswerByCertificate() {
            final var actualQuestion = questionConverter
                .convert(arende, expectedComplements, expectedAnswerByCertificate, new ArendeDraft(), Collections.emptyList());

            assertEquals(expectedAnswerByCertificate, actualQuestion.getAnsweredByCertificate());
        }

        @Test
        void shallReturnQuestionWithLastDateToReply() {
            final var actualQuestion = questionConverter
                .convert(arende, expectedComplements, expectedAnswerByCertificate, new ArendeDraft(), Collections.emptyList());

            assertEquals(lastDateToReply, actualQuestion.getLastDateToReply());

        }
    }

    @Nested
    class AnswerOnReceivedAdministativeQuestions {

        private static final String QUESTION_ID = "1000";
        private static final String AUTHOR = "author";
        private static final String SENT_BY_FK = "FK";
        private static final boolean IS_FORWARDED = true;
        private static final String MESSAGE = "message";

        private final LocalDateTime lastUpdate = LocalDateTime.now().plusDays(1);
        private final LocalDateTime sent = LocalDateTime.now();
        private final LocalDateTime answerSent = LocalDateTime.now();

        private static final String ANSWER_AUTHOR = "answer author";
        private static final String ANSWER_ID = "answerId";
        private static final String ANSWER_MESSAGE = "answer message";

        private Arende arende;
        private Arende arendeSvar;
        private ArendeDraft arendeSvarDraft;

        @BeforeEach
        void setup() {
            arende = new Arende();
            arende.setMeddelandeId(QUESTION_ID);
            arende.setVardaktorName(AUTHOR);
            arende.setAmne(ArendeAmne.AVSTMN);
            arende.setTimestamp(sent);
            arende.setSkickatAv(SENT_BY_FK);
            arende.setStatus(Status.CLOSED);
            arende.setVidarebefordrad(IS_FORWARDED);
            arende.setMeddelande(MESSAGE);
            arende.setSenasteHandelse(lastUpdate);

            arendeSvar = new Arende();
            arendeSvar.setMeddelandeId(ANSWER_ID);
            arendeSvar.setVardaktorName(ANSWER_AUTHOR);
            arendeSvar.setTimestamp(answerSent);
            arendeSvar.setMeddelande(ANSWER_MESSAGE);

            arendeSvarDraft = new ArendeDraft();
            arendeSvarDraft.setQuestionId(QUESTION_ID);
            arendeSvarDraft.setText(ANSWER_MESSAGE);
        }

        @Test
        void shallNotThrowErrorIfContactInfoIsNull() {
            final var answer = mock(Arende.class);
            when(answer.getKontaktInfo()).thenReturn(null);
            when(answer.getAmne()).thenReturn(ArendeAmne.AVSTMN);

            assertDoesNotThrow(() -> questionConverter.convert(answer, new Complement[0], null, arendeSvar, Collections.emptyList()));
        }

        @Test
        void shallReturnAnswerWithContactInfo() {
            final var answer = mock(Arende.class);
            final var contactInfo = List.of("Test 1", "Test 2");
            when(answer.getKontaktInfo()).thenReturn(contactInfo);
            when(answer.getAmne()).thenReturn(ArendeAmne.AVSTMN);

            final var actualQuestion = questionConverter.convert(answer, new Complement[0], null, arendeSvar, Collections.emptyList());

            assertEquals(contactInfo.size(), actualQuestion.getContactInfo().length);
            assertEquals(contactInfo.get(0), actualQuestion.getContactInfo()[0]);
            assertEquals(contactInfo.get(1), actualQuestion.getContactInfo()[1]);
        }

        @Test
        void shallReturnAnswerWithId() {
            final var actualQuestion = questionConverter.convert(arende, new Complement[0], null, arendeSvar, Collections.emptyList());

            assertEquals(ANSWER_ID, actualQuestion.getAnswer().getId());
        }

        @Test
        void shallReturnAnswerWithAuthor() {
            final var actualQuestion = questionConverter.convert(arende, new Complement[0], null, arendeSvar, Collections.emptyList());

            assertEquals(ANSWER_AUTHOR, actualQuestion.getAnswer().getAuthor());
        }

        @Test
        void shallReturnAnswerWithMessage() {
            final var actualQuestion = questionConverter.convert(arende, new Complement[0], null, arendeSvar, Collections.emptyList());

            assertEquals(ANSWER_MESSAGE, actualQuestion.getAnswer().getMessage());
        }

        @Test
        void shallReturnAnswerWithSent() {
            final var actualQuestion = questionConverter.convert(arende, new Complement[0], null, arendeSvar, Collections.emptyList());

            assertEquals(answerSent, actualQuestion.getAnswer().getSent());
        }

        @Test
        void shallReturnQuestionWithoutAnswerIfAnswerIsNull() {
            final var actualQuestion = questionConverter.convert(arende, new Complement[0], null, (Arende) null, Collections.emptyList());

            assertNull(actualQuestion.getAnswer(), "Answer should be null");
        }

        @Test
        void shallReturnAnswerDraftWithMessage() {
            final var actualQuestion = questionConverter.convert(arende, new Complement[0], null, arendeSvarDraft, Collections.emptyList());

            assertEquals(ANSWER_MESSAGE, actualQuestion.getAnswer().getMessage());
        }

        @Test
        void shallReturnQuestionWithoutAnswerIfAnswerDraftIsNull() {
            final var actualQuestion = questionConverter
                .convert(arende, new Complement[0], null, (ArendeDraft) null, Collections.emptyList());

            assertNull(actualQuestion.getAnswer(), "Answer should be null");
        }
    }

    @Nested
    class ReminderOnReceivedAdministativeQuestions {

        private static final String QUESTION_ID = "1000";
        private static final String AUTHOR = "author";
        private static final String SENT_BY_FK = "FK";
        private static final boolean IS_FORWARDED = true;
        private static final String MESSAGE = "message";

        private static final String ANSWER_AUTHOR = "answer author";
        private static final String ANSWER_ID = "answerId";
        private static final String ANSWER_MESSAGE = "answer message";

        private static final String REMINDER_AUTHOR = "Försäkringskassan";
        private static final String REMINDER_ID = "reminderId";
        private static final String REMINDER_MESSAGE = "reminderMessage";

        private final LocalDateTime reminderSent = LocalDateTime.now();
        private final LocalDateTime sent = LocalDateTime.now();
        private final LocalDateTime lastUpdate = LocalDateTime.now().plusDays(1);
        private final LocalDateTime answerSent = LocalDateTime.now();
        private final LocalDate lastDateToReply = LocalDate.now();

        private Arende arende;
        private Arende arendeSvar;
        private ArendeDraft arendeSvarDraft;
        private Arende arendePaminnelse;

        @BeforeEach
        void setup() {
            arende = new Arende();
            arende.setMeddelandeId(QUESTION_ID);
            arende.setVardaktorName(AUTHOR);
            arende.setAmne(ArendeAmne.AVSTMN);
            arende.setTimestamp(sent);
            arende.setSkickatAv(SENT_BY_FK);
            arende.setStatus(Status.CLOSED);
            arende.setVidarebefordrad(IS_FORWARDED);
            arende.setMeddelande(MESSAGE);
            arende.setSenasteHandelse(lastUpdate);

            arendeSvar = new Arende();
            arendeSvar.setMeddelandeId(ANSWER_ID);
            arendeSvar.setVardaktorName(ANSWER_AUTHOR);
            arendeSvar.setTimestamp(answerSent);
            arendeSvar.setMeddelande(ANSWER_MESSAGE);

            arendeSvarDraft = new ArendeDraft();
            arendeSvarDraft.setQuestionId(QUESTION_ID);
            arendeSvarDraft.setText(ANSWER_MESSAGE);

            arendePaminnelse = new Arende();
            arendePaminnelse.setMeddelandeId(REMINDER_ID);
            arendePaminnelse.setMeddelande(REMINDER_MESSAGE);
            arendePaminnelse.setTimestamp(reminderSent);
            arendePaminnelse.setSkickatAv(SENT_BY_FK);
            arendePaminnelse.setSistaDatumForSvar(lastDateToReply);
        }

        @Test
        void shallReturnReminderWithId() {
            final var actualQuestion = questionConverter
                .convert(arende, new Complement[0], null, arendeSvar, Collections.singletonList(arendePaminnelse));

            assertEquals(REMINDER_ID, actualQuestion.getReminders()[0].getId());
        }

        @Test
        void shallReturnReminderWithAuthor() {
            final var actualQuestion = questionConverter
                .convert(arende, new Complement[0], null, arendeSvar, Collections.singletonList(arendePaminnelse));

            assertEquals(REMINDER_AUTHOR, actualQuestion.getReminders()[0].getAuthor());
        }

        @Test
        void shallReturnReminderWithMessage() {
            final var actualQuestion = questionConverter
                .convert(arende, new Complement[0], null, arendeSvar, Collections.singletonList(arendePaminnelse));

            assertEquals(REMINDER_MESSAGE, actualQuestion.getReminders()[0].getMessage());
        }

        @Test
        void shallReturnReminderWithSent() {
            final var actualQuestion = questionConverter
                .convert(arende, new Complement[0], null, arendeSvar, Collections.singletonList(arendePaminnelse));

            assertEquals(reminderSent, actualQuestion.getReminders()[0].getSent());
        }

        @Test
        void shallReturnQuestionWithoutRemindersIfAnswerIsNull() {
            final var actualQuestion = questionConverter.convert(arende, new Complement[0], null, (Arende) null, Collections.emptyList());

            assertEquals(0, actualQuestion.getReminders().length, "Reminders should be empty");
        }

        @Test
        void shallReturnAnswerDraftWithReminder() {
            final var actualQuestion = questionConverter
                .convert(arende, new Complement[0], null, arendeSvarDraft, Collections.singletonList(arendePaminnelse));

            assertEquals(REMINDER_MESSAGE, actualQuestion.getReminders()[0].getMessage());
        }

        @Test
        void shallReturnQuestionWithoutRemindersIfAnswerDraftIsNull() {
            final var actualQuestion = questionConverter
                .convert(arende, new Complement[0], null, (ArendeDraft) null, Collections.emptyList());

            assertEquals(0, actualQuestion.getReminders().length, "Reminders should be empty");
        }

        @Test
        void shallReturnQuestionWithLastDateToReplyFromReminder() {
            final var actualQuestion = questionConverter
                .convert(arende, new Complement[0], null, arendeSvar, Collections.singletonList(arendePaminnelse));

            assertEquals(lastDateToReply, actualQuestion.getLastDateToReply());
        }

        @Test
        void shallReturnQuestionWithLatestLastDateToReplyFromReminderIfMultipleReminders() {
            final Arende reminder1 = new Arende();
            reminder1.setSistaDatumForSvar(LocalDate.now());
            final Arende reminder2 = new Arende();
            reminder2.setSistaDatumForSvar(LocalDate.now().plusDays(10));
            final Arende reminder3 = new Arende();
            reminder3.setSistaDatumForSvar(LocalDate.now().plusDays(5));

            final var actualQuestion = questionConverter
                .convert(arende, new Complement[0], null, arendeSvar, List.of(reminder1, reminder2, reminder3));

            assertEquals(reminder3.getSistaDatumForSvar(), actualQuestion.getLastDateToReply());
        }
    }

    @Nested
    class SendAdministrativeQuestions {

        private static final long QUESTION_ID = 1000L;
        private static final String MESSAGE = "message";

        private ArendeDraft arendeDraft;

        @BeforeEach
        void setup() {
            arendeDraft = new ArendeDraft();
            arendeDraft.setId(QUESTION_ID);
            arendeDraft.setAmne(ArendeAmne.AVSTMN.toString());
            arendeDraft.setText(MESSAGE);
        }

        @Test
        void shallReturnQuestionWithId() {
            final var actualQuestion = questionConverter.convert(arendeDraft);

            assertEquals(Long.toString(QUESTION_ID), actualQuestion.getId());
        }

        @Test
        void shallReturnQuestionWithTypeCoordination() {
            arendeDraft.setAmne(ArendeAmne.AVSTMN.toString());

            final var actualQuestion = questionConverter.convert(arendeDraft);

            assertEquals(QuestionType.COORDINATION, actualQuestion.getType());
        }

        @Test
        void shallReturnQuestionWithTypeContact() {
            arendeDraft.setAmne(ArendeAmne.KONTKT.toString());

            final var actualQuestion = questionConverter.convert(arendeDraft);

            assertEquals(QuestionType.CONTACT, actualQuestion.getType());
        }

        @Test
        void shallReturnQuestionWithTypeOther() {
            arendeDraft.setAmne(ArendeAmne.OVRIGT.toString());

            final var actualQuestion = questionConverter.convert(arendeDraft);

            assertEquals(QuestionType.OTHER, actualQuestion.getType());
        }

        @Test
        void shallReturnQuestionWithTypeMissing() {
            arendeDraft.setAmne("");

            final var actualQuestion = questionConverter.convert(arendeDraft);

            assertEquals(QuestionType.MISSING, actualQuestion.getType());
        }

        @Test
        void shallReturnQuestionWithTypeNull() {
            arendeDraft.setAmne(null);

            final var actualQuestion = questionConverter.convert(arendeDraft);

            assertEquals(QuestionType.MISSING, actualQuestion.getType());
        }

        @Test
        void shallReturnQuestionWithMessage() {
            final var actualQuestion = questionConverter.convert(arendeDraft);

            assertEquals(MESSAGE, actualQuestion.getMessage());
        }
    }
}
