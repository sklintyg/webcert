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

import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarResponse;

@Service
@RequiredArgsConstructor
public class ListCertificateQuestionsFromCS {

    private final CertificateServiceProfile certificateServiceProfile;
    private final CSIntegrationService csIntegrationService;
    private final CSIntegrationRequestFactory csIntegrationRequestFactory;

    public QueryFragaSvarResponse list(String patientId) {
        if (!certificateServiceProfile.active()) {
            return QueryFragaSvarResponse.builder()
                .results(Collections.emptyList())
                .totalCount(0)
                .build();
        }
        final var listFromCS = csIntegrationService.listQuestionsForUnit(
            csIntegrationRequestFactory.getUnitQuestionsRequestDTO(patientId));

        return QueryFragaSvarResponse.builder()
            .results(listFromCS)
            .totalCount(listFromCS.size())
            .build();
    }
}
