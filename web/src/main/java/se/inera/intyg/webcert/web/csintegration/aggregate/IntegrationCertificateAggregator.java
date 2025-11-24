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
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.integration.IntegrationService;
import se.inera.intyg.webcert.web.web.controller.integration.dto.PrepareRedirectToIntyg;

@Service("integrationCertificateAggregator")
public class IntegrationCertificateAggregator implements IntegrationService {

    private final IntegrationService integrationServiceForWC;
    private final IntegrationService integrationServiceForCS;

    public IntegrationCertificateAggregator(
        @Qualifier("integrationServiceForWC") IntegrationService integrationServiceForWC,
        @Qualifier("integrationServiceForCS") IntegrationService integrationServiceForCS) {
        this.integrationServiceForWC = integrationServiceForWC;
        this.integrationServiceForCS = integrationServiceForCS;
    }

    @Override
    public PrepareRedirectToIntyg prepareRedirectToIntyg(String intygId, WebCertUser user) {
        return prepareRedirectToIntyg(intygId, user, null);
    }

    @Override
    public PrepareRedirectToIntyg prepareRedirectToIntyg(String intygId, WebCertUser user,
        Personnummer prepareBeforeAlternateSsn) {
        final var responseFromCS = integrationServiceForCS.prepareRedirectToIntyg(intygId, user,
            prepareBeforeAlternateSsn);

        return responseFromCS != null ? responseFromCS
            : integrationServiceForWC.prepareRedirectToIntyg(intygId, user, prepareBeforeAlternateSsn);
    }
}
