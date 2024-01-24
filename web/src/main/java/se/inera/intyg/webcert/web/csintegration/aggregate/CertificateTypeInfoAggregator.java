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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.facade.GetCertificateTypesFacadeService;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateTypeInfoDTO;

@Service("GetCertificateTypeInfoAggregator")
public class CertificateTypeInfoAggregator implements GetCertificateTypesFacadeService {

    private final GetCertificateTypesFacadeService getCertificateTypesFromCertificateService;
    private final GetCertificateTypesFacadeService getCertificateTypesFacadeService;

    public CertificateTypeInfoAggregator(
        @Qualifier("GetCertificateTypeInfoFromWebcert")
        GetCertificateTypesFacadeService getCertificateTypesFacadeService,
        @Qualifier("GetCertificateTypeInfoFromCertificateService")
        GetCertificateTypesFacadeService getCertificateTypesFromCertificateService) {
        this.getCertificateTypesFromCertificateService = getCertificateTypesFromCertificateService;
        this.getCertificateTypesFacadeService = getCertificateTypesFacadeService;
    }

    public List<CertificateTypeInfoDTO> get(Personnummer patientId) {
        final var typesFromCertificateService = getCertificateTypesFromCertificateService.get(patientId);
        final var typesFromWebcert = getCertificateTypesFacadeService.get(patientId);

        return Stream
            .concat(
                typesFromWebcert.stream(),
                typesFromCertificateService.stream()
            )
            .collect(Collectors.toList());
    }
}
