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

import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.webcert.web.csintegration.message.GetQuestionsFromCertificateService;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.facade.question.GetQuestionsFacadeService;
import se.inera.intyg.webcert.web.service.facade.question.GetQuestionsResourceLinkService;
import se.inera.intyg.webcert.web.web.controller.facade.dto.QuestionsResponseDTO;

@Service
public class GetQuestionsAggregator {

    private final CertificateServiceProfile certificateServiceProfile;
    private final GetQuestionsFacadeService getQuestionsFromWebcert;
    private final GetQuestionsResourceLinkService getQuestionsResourceLinkService;
    private final GetQuestionsFromCertificateService getQuestionsFromCertificateService;

    public GetQuestionsAggregator(
        CertificateServiceProfile certificateServiceProfile,
        @Qualifier(value = "GetQuestionsFacadeServiceImpl") GetQuestionsFacadeService getQuestionsFromWebcert,
        GetQuestionsResourceLinkService getQuestionsResourceLinkService,
        @Qualifier("getQuestionsFromCertificateService") GetQuestionsFromCertificateService getQuestionsFromCertificateService) {
        this.certificateServiceProfile = certificateServiceProfile;
        this.getQuestionsFromWebcert = getQuestionsFromWebcert;
        this.getQuestionsResourceLinkService = getQuestionsResourceLinkService;
        this.getQuestionsFromCertificateService = getQuestionsFromCertificateService;
    }

    public QuestionsResponseDTO getComplements(String certificateId) {
        return QuestionsResponseDTO.builder()
            .questions(
                get(certificateId).getQuestions().stream()
                    .filter(question -> question.getType().equals(QuestionType.COMPLEMENT))
                    .collect(Collectors.toList())
            )
            .build();
    }

    public QuestionsResponseDTO get(String certificateId) {
        if (!certificateServiceProfile.active()) {
            return getQuestionsFromWebcert(certificateId);
        }

        final var responseFromCS = getQuestionsFromCertificateService.get(certificateId);

        return responseFromCS != null ? responseFromCS : getQuestionsFromWebcert(certificateId);
    }

    private QuestionsResponseDTO getQuestionsFromWebcert(String certificateId) {
        final var questions = getQuestionsFromWebcert.getQuestions(certificateId);
        final var links = getQuestionsResourceLinkService.get(questions);
        return QuestionsResponseDTO.create(questions, links);
    }
}
