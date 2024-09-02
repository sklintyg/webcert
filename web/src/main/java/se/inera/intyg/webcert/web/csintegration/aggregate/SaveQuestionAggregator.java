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

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.facade.question.SaveQuestionFacadeService;

@Service
@RequiredArgsConstructor
public class SaveQuestionAggregator implements SaveQuestionFacadeService {

    private final SaveQuestionFacadeService saveQuestionFromWC;
    private final SaveQuestionFacadeService saveMessageFromCS;
    private final CertificateServiceProfile certificateServiceProfile;

    @Override
    public Question save(Question question) {
        if (!certificateServiceProfile.active()) {
            return saveQuestionFromWC.save(question);
        }

        final var responseFromCS = saveMessageFromCS.save(question);

        return responseFromCS != null ? responseFromCS : saveQuestionFromWC.save(question);
    }
}
