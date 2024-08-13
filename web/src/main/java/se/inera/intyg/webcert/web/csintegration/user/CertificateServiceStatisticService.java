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

package se.inera.intyg.webcert.web.csintegration.user;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.facade.user.UserStatisticsDTO;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@Service
@RequiredArgsConstructor
public class CertificateServiceStatisticService {

    private final CertificateServiceProfile certificateServiceProfile;
    private final CSIntegrationService csIntegrationService;
    private final CSIntegrationRequestFactory csIntegrationRequestFactory;

    public void add(UserStatisticsDTO statisticsFromWC, List<String> unitIds, WebCertUser user) {
        if (!certificateServiceProfile.active()) {
            return;
        }

        final var statisticsFromCS = csIntegrationService.getStatistics(
            csIntegrationRequestFactory.getStatisticsRequest(unitIds, user.getIdsOfSelectedVardenhet())
        );

        if (user.getValdVardenhet() != null) {
            statisticsFromWC.addNbrOfDraftsOnSelectedUnit(statisticsFromCS.getNbrOfDraftsOnSelectedUnit());
            statisticsFromWC.addNbrOfUnhandledQuestionsOnSelectedUnit(statisticsFromCS.getNbrOfUnhandledQuestionsOnSelectedUnit());
            statisticsFromWC.addTotalDraftsAndUnhandledQuestionsOnOtherUnits(
                statisticsFromCS.getTotalDraftsAndUnhandledQuestionsOnOtherUnits()
            );
        }

        statisticsFromWC.mergeUnitStatistics(statisticsFromCS.getUnitStatistics());
    }
}
