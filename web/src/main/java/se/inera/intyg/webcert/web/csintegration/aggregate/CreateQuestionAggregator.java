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

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.facade.question.CreateQuestionFacadeService;

@Service
public class CreateQuestionAggregator implements CreateQuestionFacadeService {

    private final CreateQuestionFacadeService createQuestionFromWC;
    private final CreateQuestionFacadeService createMessageFromCS;
    private final CertificateServiceProfile certificateServiceProfile;

    public CreateQuestionAggregator(@Qualifier("createQuestionFromWC") CreateQuestionFacadeService createQuestionFromWC,
        @Qualifier("createMessageFromCS") CreateQuestionFacadeService createMessageFromCS,
        CertificateServiceProfile certificateServiceProfile) {
        this.createQuestionFromWC = createQuestionFromWC;
        this.createMessageFromCS = createMessageFromCS;
        this.certificateServiceProfile = certificateServiceProfile;
    }

    @Override
    public Question create(String certificateId, QuestionType type, String message) {
        if (!certificateServiceProfile.active()) {
            return createQuestionFromWC.create(certificateId, type, message);
        }

        final var responseFromCS = createMessageFromCS.create(certificateId, type, message);

        return responseFromCS != null ? responseFromCS : createQuestionFromWC.create(certificateId, type, message);
    }
}
