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

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.facade.GetCertificateTypesFacadeService;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateTypeInfoDTO;

@Service("certificateTypeInfoAggregator")
public class CertificateTypeInfoAggregator implements GetCertificateTypesFacadeService {

    private final GetCertificateTypesFacadeService getCertificateTypeInfoFromWebcert;
    private final GetCertificateTypesFacadeService getCertificateTypeInfoFromCertificateService;
    private final CertificateServiceProfile certificateServiceProfile;

    public CertificateTypeInfoAggregator(
        @Qualifier("getCertificateTypeInfoFromWebcert")
        GetCertificateTypesFacadeService getCertificateTypeInfoFromWebcert,
        @Qualifier("getCertificateTypeInfoFromCertificateService")
        GetCertificateTypesFacadeService getCertificateTypeInfoFromCertificateService,
        CertificateServiceProfile certificateServiceProfile) {
        this.getCertificateTypeInfoFromWebcert = getCertificateTypeInfoFromWebcert;
        this.getCertificateTypeInfoFromCertificateService = getCertificateTypeInfoFromCertificateService;
        this.certificateServiceProfile = certificateServiceProfile;
    }

    @Override
    public List<CertificateTypeInfoDTO> get(Personnummer patientId) {
        final var typesFromWebcert = getCertificateTypeInfoFromWebcert.get(patientId);
        if (!certificateServiceProfile.active()) {
            return typesFromWebcert;
        }

        final var typesFromCertificateService = getCertificateTypeInfoFromCertificateService.get(patientId);
        final var csIds = typesFromCertificateService.stream()
            .map(CertificateTypeInfoDTO::getIssuerTypeId)
            .map(id -> id.replace(" ", "").toLowerCase())
            .collect(Collectors.toSet());

        final var filteredTypesFromWebcert = typesFromWebcert.stream()
            .filter(dto -> !csIds.contains(dto.getIssuerTypeId().replace(" ", "").toLowerCase()))
            .toList();

        return Stream
            .concat(
                typesFromCertificateService.stream(),
                filteredTypesFromWebcert.stream()
            )
            .sorted(Comparator.comparing(CertificateTypeInfoDTO::getLabel))
            .collect(Collectors.toList());
    }
}
