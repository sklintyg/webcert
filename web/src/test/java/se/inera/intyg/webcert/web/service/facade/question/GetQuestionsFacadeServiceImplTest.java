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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.web.service.arende.ArendeService;

@ExtendWith(MockitoExtension.class)
public class GetQuestionsFacadeServiceImplTest {

    @Mock
    private ArendeService arendeService;

    @InjectMocks
    private GetQuestionsFacadeServiceImpl getQuestionsFacadeService;

    private final static String CERTIFICATE_ID = "certificateId";

    private static final String QUESTION_ID = "1000";
    private static final String AUTHOR = "author";
    private static final String SUBJECT = "subject";
    private static final LocalDateTime SENT = LocalDateTime.now();
    private static final boolean IS_HANDLED = true;
    private static final boolean IS_FORWARDED = true;
    private static final String MESSAGE = "message";
    private static final LocalDateTime LAST_UPDATE = LocalDateTime.now().plusDays(1);

    @Nested
    class Question {

        @BeforeEach
        void setup() {
            final var arende = new Arende();
            arende.setId(Long.parseLong(QUESTION_ID));
            arende.setSkickatAv(AUTHOR);
            arende.setRubrik(SUBJECT);
            arende.setSkickatTidpunkt(SENT);
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
        void shallReturnQuestionWithAuthor() {
            final var actualQuestions = getQuestionsFacadeService.getQuestions(CERTIFICATE_ID);

            assertEquals(AUTHOR, actualQuestions.get(0).getAuthor());
        }

        @Test
        void shallReturnQuestionWithSubject() {
            final var actualQuestions = getQuestionsFacadeService.getQuestions(CERTIFICATE_ID);

            assertEquals(SUBJECT, actualQuestions.get(0).getSubject());
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
