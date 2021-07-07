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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.web.service.arende.ArendeService;

@ExtendWith(MockitoExtension.class)
public class GetQuestionFacadeServiceImplTest {

    @Mock
    private ArendeService arendeService;

    @InjectMocks
    private GetQuestionsFacadeServiceImpl getQuestionsFacadeService;

    private final static String CERTIFICATE_ID = "certificateId";

    @Nested
    class Question {

        @BeforeEach
        void setup() {
            final var arende = new Arende();
            arende.setId(1000L);

            doReturn(Collections.singletonList(arende))
                .when(arendeService)
                .getArendenInternal(CERTIFICATE_ID);
        }

        @Test
        void shallGetQuestionForCertificate() {
            final var actualQuestions = getQuestionsFacadeService.getQuestions(CERTIFICATE_ID);

            assertNotNull(actualQuestions[0], "Expect a question");
        }

        @Test
        void shallReturnQuestionWithId() {
            final var expectedId = "1000";

            final var actualQuestions = getQuestionsFacadeService.getQuestions(CERTIFICATE_ID);

            assertEquals(expectedId, actualQuestions[0].getId());
        }
    }


}
