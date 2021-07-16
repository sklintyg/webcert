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

package se.inera.intyg.webcert.web.service.facade.question.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import se.inera.intyg.common.support.facade.model.question.Question;
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

        private final String QUESTION_ID = "1000";
        private final String AUTHOR_CERTIFICATE_RECEIVER = "Försäkringskassan";
        private final String AUTHOR = "author";
        private final String SUBJECT_WITHOUT_HEADER = "Avstämningsmöte";
        private final String SUBJECT_WITH_HEADER = "Avstämningsmöte - Rubrik";
        private final String HEADER = "Rubrik";
        private final LocalDateTime SENT = LocalDateTime.now();
        private final String SENT_BY_FK = "FK";
        private final boolean IS_HANDLED = true;
        private final boolean IS_FORWARDED = true;
        private final String MESSAGE = "message";
        private final LocalDateTime LAST_UPDATE = LocalDateTime.now().plusDays(1);

        private Arende arende;

        @BeforeEach
        void setup() {
            arende = new Arende();
            arende.setMeddelandeId(QUESTION_ID);
            arende.setVardaktorName(AUTHOR);
            arende.setAmne(ArendeAmne.AVSTMN);
            arende.setSkickatTidpunkt(SENT);
            arende.setSkickatAv(SENT_BY_FK);
            arende.setStatus(Status.CLOSED);
            arende.setVidarebefordrad(IS_FORWARDED);
            arende.setMeddelande(MESSAGE);
            arende.setSenasteHandelse(LAST_UPDATE);
        }

        @Test
        void shallReturnQuestionWithId() {
            final var actualQuestion = questionConverter.convert(arende);

            assertEquals(QUESTION_ID, actualQuestion.getId());
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

            assertEquals(SENT, actualQuestion.getSent());
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

            assertEquals(LAST_UPDATE, actualQuestion.getLastUpdate());
        }
    }

    @Nested
    class SendAdministrativeQuestions {

        private final long QUESTION_ID = 1000L;
        private final QuestionType TYPE = QuestionType.COORDINATION;
        private final String MESSAGE = "message";

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
        void shallReturnQuestionWithMessage() {
            final var actualQuestion = questionConverter.convert(arendeDraft);

            assertEquals(MESSAGE, actualQuestion.getMessage());
        }
    }

    @Nested
    class AnswerForAdministrativeQuestion {

        private Arende arende;

        @BeforeEach
        void setup() {
            arende = new Arende();
            arende.setMeddelandeId("questionId");
            arende.setVardaktorName("author");
            arende.setAmne(ArendeAmne.AVSTMN);
            arende.setSkickatTidpunkt(LocalDateTime.now());
            arende.setSkickatAv("FK");
            arende.setStatus(Status.PENDING_INTERNAL_ACTION);
            arende.setVidarebefordrad(false);
            arende.setMeddelande("Här är det en fråga");
            arende.setSenasteHandelse(arende.getSkickatTidpunkt());
        }

        @Test
        void shallIncludeAnswer() {
            final var answer = "Här är vårat svar";

            final var actualQuestion = questionConverter.convert(arende, answer);

            assertEquals(actualQuestion.getAnswer().getMessage(), answer);
        }
    }
}