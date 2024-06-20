/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.facade.question.DeleteQuestionAnswerFacadeService;

@ExtendWith(MockitoExtension.class)
class DeleteAnswerAggregatorTest {

    private static final String QUESTION_ID = "questionId";
    @Mock
    CSIntegrationService csIntegrationService;
    @Mock
    CertificateServiceProfile certificateServiceProfile;
    @Mock
    DeleteQuestionAnswerFacadeService deleteAnswerFromWC;
    @Mock
    DeleteQuestionAnswerFacadeService deleteAnswerFromCS;

    DeleteAnswerAggregator deleteAnswerAggregator;

    @BeforeEach
    void setUp() {
        deleteAnswerAggregator = new DeleteAnswerAggregator(
            deleteAnswerFromWC, deleteAnswerFromCS, certificateServiceProfile, csIntegrationService
        );
    }

    @Test
    void shallCallDeleteQuestionsFromWCIfCertificateServiceProfileIsInactive() {
        doReturn(false).when(certificateServiceProfile).active();

        deleteAnswerAggregator.delete(QUESTION_ID);

        verify(deleteAnswerFromWC).delete(QUESTION_ID);

        verifyNoInteractions(deleteAnswerFromCS);
    }

    @Test
    void shallCallDeleteQuestionsFromWCIfCertificateServiceProfileIsActiveButMessageDontExistInCertificateService() {
        doReturn(true).when(certificateServiceProfile).active();
        doReturn(false).when(csIntegrationService).messageExists(QUESTION_ID);

        deleteAnswerAggregator.delete(QUESTION_ID);

        verify(deleteAnswerFromWC).delete(QUESTION_ID);

        verifyNoInteractions(deleteAnswerFromCS);
    }

    @Test
    void shallCallDeleteQuestionsFromCSIfCertificateServiceProfileIsActiveAndMessageExistInCertificateService() {
        doReturn(true).when(certificateServiceProfile).active();
        doReturn(true).when(csIntegrationService).messageExists(QUESTION_ID);

        deleteAnswerAggregator.delete(QUESTION_ID);

        verify(deleteAnswerFromCS).delete(QUESTION_ID);

        verifyNoInteractions(deleteAnswerFromWC);
    }
}
