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

package se.inera.intyg.webcert.web.csintegration.aggregate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.facade.question.CreateQuestionFacadeService;

@ExtendWith(MockitoExtension.class)
class CreateQuestionAggregatorTest {

    private static final String CERTIFICATE_ID = "certificateId";
    private static final String MESSAGE = "message";
    @Mock
    CreateQuestionFacadeService createQuestionFromWC;
    @Mock
    CreateQuestionFacadeService createQuestionFromCS;
    @Mock
    CertificateServiceProfile certificateServiceProfile;
    CreateQuestionFacadeService createQuestionAggregator;

    @BeforeEach
    void setUp() {
        createQuestionAggregator = new CreateQuestionAggregator(
            createQuestionFromWC, createQuestionFromCS, certificateServiceProfile
        );
    }

    @Test
    void shallReturnQuestionFromWCIfCertificateServiceProfileIsInactive() {
        doReturn(false).when(certificateServiceProfile).active();

        createQuestionAggregator.create(CERTIFICATE_ID, QuestionType.CONTACT, MESSAGE);

        verify(createQuestionFromWC).create(CERTIFICATE_ID, QuestionType.CONTACT, MESSAGE);
        verifyNoInteractions(createQuestionFromCS);
    }

    @Test
    void shallReturnQuestionFromWCIfCertificateServiceProfileIsActiveButResponseFromCSIsNull() {
        final var expectedQuestion = Question.builder().build();

        doReturn(true).when(certificateServiceProfile).active();
        doReturn(null).when(createQuestionFromCS).create(CERTIFICATE_ID, QuestionType.CONTACT, MESSAGE);
        doReturn(expectedQuestion).when(createQuestionFromWC).create(CERTIFICATE_ID, QuestionType.CONTACT, MESSAGE);

        final var actualQuestion = createQuestionAggregator.create(CERTIFICATE_ID, QuestionType.CONTACT, MESSAGE);
        assertEquals(expectedQuestion, actualQuestion);
    }

    @Test
    void shallReturnQuestionFromCSIfCertificateServiceProfileIsActiveAndResponseFromCSIsNotNull() {
        final var expectedQuestion = Question.builder().build();

        doReturn(true).when(certificateServiceProfile).active();
        doReturn(expectedQuestion).when(createQuestionFromCS).create(CERTIFICATE_ID, QuestionType.CONTACT, MESSAGE);

        final var actualQuestion = createQuestionAggregator.create(CERTIFICATE_ID, QuestionType.CONTACT, MESSAGE);
        assertEquals(expectedQuestion, actualQuestion);
    }
}
