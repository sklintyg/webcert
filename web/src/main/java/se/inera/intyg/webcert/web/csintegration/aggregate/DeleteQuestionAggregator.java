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
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.facade.question.DeleteQuestionFacadeService;

@Service("deleteQuestionAggregator")
public class DeleteQuestionAggregator implements DeleteQuestionFacadeService {

    private final DeleteQuestionFacadeService deleteQuestionFromWC;
    private final DeleteQuestionFacadeService deleteQuestionFromCS;
    private final CertificateServiceProfile certificateServiceProfile;
    private final CSIntegrationService csIntegrationService;

    public DeleteQuestionAggregator(
        DeleteQuestionFacadeService deleteQuestionFromWC, DeleteQuestionFacadeService deleteQuestionFromCS,
        CertificateServiceProfile certificateServiceProfile, CSIntegrationService csIntegrationService) {
        this.deleteQuestionFromWC = deleteQuestionFromWC;
        this.deleteQuestionFromCS = deleteQuestionFromCS;
        this.certificateServiceProfile = certificateServiceProfile;
        this.csIntegrationService = csIntegrationService;
    }

    @Override
    public void delete(String questionId) {
        if (!certificateServiceProfile.active()) {
            deleteQuestionFromWC.delete(questionId);
            return;
        }

        if (Boolean.TRUE.equals(csIntegrationService.messageExists(questionId))) {
            deleteQuestionFromCS.delete(questionId);
            return;
        }

        deleteQuestionFromWC.delete(questionId);
    }
}