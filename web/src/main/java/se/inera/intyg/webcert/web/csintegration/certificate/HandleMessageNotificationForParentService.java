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

package se.inera.intyg.webcert.web.csintegration.certificate;

import static se.inera.intyg.common.support.facade.model.CertificateRelationType.COMPLEMENTED;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;

@Service
@RequiredArgsConstructor
public class HandleMessageNotificationForParentService {

    private final CSIntegrationService csIntegrationService;
    private final CSIntegrationRequestFactory csIntegrationRequestFactory;
    private final PublishCertificateStatusUpdateService publishCertificateStatusUpdateService;

    public void notify(CertificateRelations relations) {
        if (parentCertificateRelationIsNotComplement(relations)) {
            return;
        }

        final var certificateId = relations.getParent().getCertificateId();

        final var questions = csIntegrationService.getQuestions(
            certificateId
        );

        final var parentCertificate = csIntegrationService.getCertificate(
            certificateId,
            csIntegrationRequestFactory.getCertificateRequest()
        );

        questions.forEach(question -> {
            if (isQuestionComplement(question)) {
                publishCertificateStatusUpdateService.publish(parentCertificate, HandelsekodEnum.NYFRFM);
            }
        });
    }

    private static boolean parentCertificateRelationIsNotComplement(CertificateRelations relations) {
        return relations == null || relations.getParent() == null || !relations.getParent().getType().equals(COMPLEMENTED);
    }

    private boolean isQuestionComplement(Question question) {
        return QuestionType.COMPLEMENT.equals(question.getType());
    }
}