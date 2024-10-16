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

package se.inera.intyg.webcert.web.csintegration.certificate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.util.PDLLogService;
import se.inera.intyg.webcert.web.service.facade.question.HandleQuestionFacadeService;
import se.inera.intyg.webcert.web.service.fragasvar.dto.FrageStallare;

@Service("handleQuestionFromCS")
@RequiredArgsConstructor
public class HandleQuestionFromCertificateService implements HandleQuestionFacadeService {

    private final CSIntegrationService csIntegrationService;
    private final CSIntegrationRequestFactory csIntegrationRequestFactory;
    private final PDLLogService pdlLogService;
    private final PublishCertificateStatusUpdateService publishCertificateStatusUpdateService;

    @Override
    public Question handle(String questionId, boolean isHandled) {
        if (Boolean.FALSE.equals(csIntegrationService.messageExists(questionId))) {
            return null;
        }

        final var question = csIntegrationService.handleMessage(
            csIntegrationRequestFactory.handleMessageRequestDTO(isHandled),
            questionId
        );

        final var certificate = csIntegrationService.getCertificate(
            csIntegrationRequestFactory.getCertificateFromMessageRequestDTO(),
            questionId
        );

        pdlLogService.logCreateMessage(
            certificate.getMetadata().getPatient().getPersonId().getId(),
            certificate.getMetadata().getId()
        );

        publishCertificateStatusUpdateService.publish(certificate, eventType(question.getAuthor()));

        return question;
    }

    private HandelsekodEnum eventType(String author) {
        return FrageStallare.FORSAKRINGSKASSAN.isNameEqual(author) ? HandelsekodEnum.HANFRFM : HandelsekodEnum.HANFRFV;
    }
}