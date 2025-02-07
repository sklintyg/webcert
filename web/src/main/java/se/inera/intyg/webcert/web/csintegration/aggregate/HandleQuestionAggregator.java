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

import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.facade.question.HandleQuestionFacadeService;

@Service
public class HandleQuestionAggregator implements HandleQuestionFacadeService {

    private final HandleQuestionFacadeService handleQuestionFromWC;
    private final HandleQuestionFacadeService handleQuestionFromCS;
    private final CertificateServiceProfile certificateServiceProfile;

    public HandleQuestionAggregator(
        HandleQuestionFacadeService handleQuestionFromWC,
        HandleQuestionFacadeService handleQuestionFromCS,
        CertificateServiceProfile certificateServiceProfile) {
        this.handleQuestionFromWC = handleQuestionFromWC;
        this.handleQuestionFromCS = handleQuestionFromCS;
        this.certificateServiceProfile = certificateServiceProfile;
    }

    @Override
    public Question handle(String questionId, boolean isHandled) {
        if (!certificateServiceProfile.active()) {
            return handleQuestionFromWC.handle(questionId, isHandled);
        }

        final var responseFromCS = handleQuestionFromCS.handle(questionId, isHandled);

        return responseFromCS != null ? responseFromCS : handleQuestionFromWC.handle(questionId, isHandled);
    }
}
