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

package se.inera.intyg.webcert.web.web.controller.internalapi;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.service.facade.GetCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.internalapi.service.GetAvailableFunctionsForCertificateService;
import se.inera.intyg.webcert.web.service.facade.internalapi.service.GetTextsForCertificateService;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.GetCertificateResponse;

@Service("getCertificateInternalServiceFromWC")
public class GetCertificateInternalServiceFromWC implements GetCertificateInteralApi {

    private final GetCertificateFacadeService getCertificateFacadeService;
    private final GetTextsForCertificateService getTextsForCertificateService;
    private final GetAvailableFunctionsForCertificateService getAvailableFunctionsForCertificateService;

    private static final boolean SHOULD_NOT_PDL_LOG = false;
    private static final boolean SHOULD_NOT_VALIDATE_ACCESS = false;

    public GetCertificateInternalServiceFromWC(
        @Qualifier("getCertificateFromWC") GetCertificateFacadeService getCertificateFacadeService,
        GetTextsForCertificateService getTextsForCertificateService,
        GetAvailableFunctionsForCertificateService getAvailableFunctionsForCertificateService) {
        this.getCertificateFacadeService = getCertificateFacadeService;
        this.getTextsForCertificateService = getTextsForCertificateService;
        this.getAvailableFunctionsForCertificateService = getAvailableFunctionsForCertificateService;
    }

    @Override
    public GetCertificateResponse get(String certificateId, String personId) {
        final var certificate = getCertificateFacadeService.getCertificate(certificateId, SHOULD_NOT_PDL_LOG, SHOULD_NOT_VALIDATE_ACCESS);
        final var availableFunction = getAvailableFunctionsForCertificateService.get(certificate);
        final var texts = getTextsForCertificateService.get(
            certificate.getMetadata().getType(),
            certificate.getMetadata().getTypeVersion()
        );

        return GetCertificateResponse.create(
            certificate,
            availableFunction,
            texts
        );
    }
}
