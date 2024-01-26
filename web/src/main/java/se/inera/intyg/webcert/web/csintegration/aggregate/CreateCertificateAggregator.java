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

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.service.facade.CreateCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.impl.CreateCertificateException;

@Service
@Profile("certificate-service-active")
public class CreateCertificateAggregator implements CreateCertificateFacadeService {

    private final CreateCertificateFacadeService createCertificateFromWC;
    private final CreateCertificateFacadeService createCertificateFromCS;
    private final CSIntegrationService csIntegrationService;
    private final Environment environment;
    private final IntygTextsService intygTextsService;

    public CreateCertificateAggregator(
        @Qualifier("CreateCertificateFromWC") CreateCertificateFacadeService createCertificateFromWC,
        @Qualifier("CreateCertificateFromCS") CreateCertificateFacadeService createCertificateFromCS,
        CSIntegrationService csIntegrationService, Environment environment, IntygTextsService intygTextsService) {
        this.createCertificateFromWC = createCertificateFromWC;
        this.createCertificateFromCS = createCertificateFromCS;
        this.csIntegrationService = csIntegrationService;
        this.environment = environment;
        this.intygTextsService = intygTextsService;
    }

    @Override
    public String create(String certificateType, String patientId) throws CreateCertificateException {
        if (!environment.matchesProfiles("certificate-service-active")) {
            return createCertificateFromWC.create(certificateType, patientId);
        }

        //replace intygTexts with something else to get version, the new types will not exists in intygTexts
        final var version = intygTextsService.getLatestVersion(certificateType);
        final var csHasCertificateModel = csIntegrationService.createCertificateExists(certificateType, version);

        return csHasCertificateModel
            ? createCertificateFromCS.create(certificateType, patientId)
            : createCertificateFromWC.create(certificateType, patientId);
    }
}
