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

import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.csintegration.certificate.HandleQuestionFromCertificateService;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.facade.question.GetQuestionsResourceLinkService;
import se.inera.intyg.webcert.web.service.facade.question.HandleQuestionFacadeService;
import se.inera.intyg.webcert.web.web.controller.facade.dto.QuestionResponseDTO;

@Service
public class HandleQuestionAggregator {

    private final HandleQuestionFacadeService handleQuestionFromWC;
    private final HandleQuestionFromCertificateService handleQuestionFromCS;
    private final CertificateServiceProfile certificateServiceProfile;
    private final GetQuestionsResourceLinkService getQuestionsResourceLinkService;

    public HandleQuestionAggregator(
        HandleQuestionFacadeService handleQuestionFromWebcert, HandleQuestionFromCertificateService handleQuestionFromCS,
        CertificateServiceProfile certificateServiceProfile, GetQuestionsResourceLinkService getQuestionsResourceLinkService) {
        this.handleQuestionFromWC = handleQuestionFromWebcert;
        this.handleQuestionFromCS = handleQuestionFromCS;
        this.certificateServiceProfile = certificateServiceProfile;
        this.getQuestionsResourceLinkService = getQuestionsResourceLinkService;
    }

    public QuestionResponseDTO handle(String questionId, boolean isHandled) {
        if (!certificateServiceProfile.active()) {
            return handleQuestionsFromWC(questionId, isHandled);
        }

        final var responseFromCS = handleQuestionFromCS.handle(questionId, isHandled);

        return responseFromCS != null ? responseFromCS : handleQuestionsFromWC(questionId, isHandled);
    }

    private QuestionResponseDTO handleQuestionsFromWC(String questionId, boolean isHandled) {
        final var question = handleQuestionFromWC.handle(questionId, isHandled);
        final var links = getQuestionsResourceLinkService.get(question);
        return QuestionResponseDTO.create(question, links);
    }
}
