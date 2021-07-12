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

package se.inera.intyg.webcert.web.service.facade.question;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.time.LocalDateTime;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.arende.model.ArendeDraft;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.web.service.arende.ArendeDraftService;
import se.inera.intyg.webcert.web.service.arende.ArendeService;

@ExtendWith(MockitoExtension.class)
public class GetQuestionsFacadeServiceImplTest {

    @Mock
    private ArendeService arendeService;

    @Mock
    private ArendeDraftService arendeDraftService;

    @InjectMocks
    private GetQuestionsFacadeServiceImpl getQuestionsFacadeService;

    private final static String CERTIFICATE_ID = "certificateId";

    @Test
    void shallReturnEmptyQuestionsIfNoQuestionDraft() {
        doReturn(null)
            .when(arendeDraftService)
            .getQuestionDraft(CERTIFICATE_ID);

        final var actualQuestions = getQuestionsFacadeService.getQuestions(CERTIFICATE_ID);

        assertTrue(actualQuestions.isEmpty(), "Don't expect any questions");
    }

    @Nested
    class SendAdministrativeQuestions {

        private final long QUESTION_ID = 1000L;
        private final QuestionType TYPE = QuestionType.COORDINATION;
        private final ArendeAmne ARENDE_AMNE = ArendeAmne.AVSTMN;
        private final String MESSAGE = "message";


        private ArendeDraft arendeDraft;

        @BeforeEach
        void setup() {
            arendeDraft = new ArendeDraft();
            arendeDraft.setId(QUESTION_ID);
            arendeDraft.setAmne(ARENDE_AMNE.toString());
            arendeDraft.setText(MESSAGE);

            doReturn(arendeDraft)
                .when(arendeDraftService)
                .getQuestionDraft(CERTIFICATE_ID);
        }

        @Test
        void shallReturnQuestionDraft() {
            final var actualQuestions = getQuestionsFacadeService.getQuestions(CERTIFICATE_ID);

            assertFalse(actualQuestions.isEmpty(), "Expect a question");
        }

        @Test
        void shallReturnQuestionWithId() {
            final var actualQuestions = getQuestionsFacadeService.getQuestions(CERTIFICATE_ID);

            assertEquals(Long.toString(QUESTION_ID), actualQuestions.get(0).getId());
        }

        @Test
        void shallReturnQuestionWithType() {
            final var actualQuestions = getQuestionsFacadeService.getQuestions(CERTIFICATE_ID);

            assertEquals(TYPE, actualQuestions.get(0).getType());
        }

        @Test
        void shallReturnQuestionWithMessage() {
            final var actualQuestions = getQuestionsFacadeService.getQuestions(CERTIFICATE_ID);

            assertEquals(MESSAGE, actualQuestions.get(0).getMessage());
        }
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

            doReturn(Collections.singletonList(arende))
                .when(arendeService)
                .getArendenInternal(CERTIFICATE_ID);
        }

        @Test
        void shallGetQuestionForCertificate() {
            final var actualQuestions = getQuestionsFacadeService.getQuestions(CERTIFICATE_ID);

            assertFalse(actualQuestions.isEmpty(), "Expect a question");
        }

        @Test
        void shallReturnQuestionWithId() {
            final var actualQuestions = getQuestionsFacadeService.getQuestions(CERTIFICATE_ID);

            assertEquals(QUESTION_ID, actualQuestions.get(0).getId());
        }

        @Test
        void shallReturnReceivedQuestionWithAuthor() {
            final var actualQuestions = getQuestionsFacadeService.getQuestions(CERTIFICATE_ID);

            assertEquals(AUTHOR_CERTIFICATE_RECEIVER, actualQuestions.get(0).getAuthor());
        }

        @Test
        void shallReturnSentQuestionWithAuthor() {
            arende.setSkickatAv("WC");

            final var actualQuestions = getQuestionsFacadeService.getQuestions(CERTIFICATE_ID);

            assertEquals(AUTHOR, actualQuestions.get(0).getAuthor());
        }

        @Test
        void shallReturnQuestionWithSubjectWithoutHeader() {
            final var actualQuestions = getQuestionsFacadeService.getQuestions(CERTIFICATE_ID);

            assertEquals(SUBJECT_WITHOUT_HEADER, actualQuestions.get(0).getSubject());
        }

        @Test
        void shallReturnQuestionWithSubjectWithHeader() {
            arende.setRubrik(HEADER);

            final var actualQuestions = getQuestionsFacadeService.getQuestions(CERTIFICATE_ID);

            assertEquals(SUBJECT_WITH_HEADER, actualQuestions.get(0).getSubject());
        }

        @Test
        void shallReturnQuestionWithSent() {
            final var actualQuestions = getQuestionsFacadeService.getQuestions(CERTIFICATE_ID);

            assertEquals(SENT, actualQuestions.get(0).getSent());
        }

        @Test
        void shallReturnQuestionWithHandled() {
            final var actualQuestions = getQuestionsFacadeService.getQuestions(CERTIFICATE_ID);

            assertEquals(IS_HANDLED, actualQuestions.get(0).isHandled());
        }

        @Test
        void shallReturnQuestionWithForwarded() {
            final var actualQuestions = getQuestionsFacadeService.getQuestions(CERTIFICATE_ID);

            assertEquals(IS_FORWARDED, actualQuestions.get(0).isForwarded());
        }

        @Test
        void shallReturnQuestionWithMessage() {
            final var actualQuestions = getQuestionsFacadeService.getQuestions(CERTIFICATE_ID);

            assertEquals(MESSAGE, actualQuestions.get(0).getMessage());
        }

        @Test
        void shallReturnQuestionWithLastUpdate() {
            final var actualQuestions = getQuestionsFacadeService.getQuestions(CERTIFICATE_ID);

            assertEquals(LAST_UPDATE, actualQuestions.get(0).getLastUpdate());
        }
    }
}
