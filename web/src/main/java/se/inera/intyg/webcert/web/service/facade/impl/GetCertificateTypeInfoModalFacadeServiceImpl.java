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
package se.inera.intyg.webcert.web.service.facade.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.logmessages.ResourceType;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.facade.GetCertificateTypeInfoModalFacadeService;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateTypeInfoModalDTO;

@Service
@RequiredArgsConstructor
public class GetCertificateTypeInfoModalFacadeServiceImpl implements GetCertificateTypeInfoModalFacadeService {

    private final CertificateTypeInfoModalService certificateTypeInfoModalService;
    private final LogService logService;
    private final WebCertUserService webCertUserService;

  @Override
    public CertificateTypeInfoModalDTO get(String certificateType, Personnummer patientId) {
        final var modal = certificateTypeInfoModalService.get(certificateType, patientId);
        logService.logReadLevelTwo(webCertUserService.getUser(), patientId.getPersonnummer(),
            ResourceType.RESOURCE_TYPE_VARDINFORMATION);

        return modal.map(certificateTypeInfoModal -> CertificateTypeInfoModalDTO.builder()
            .title(certificateTypeInfoModal.getTitle())
            .description(certificateTypeInfoModal.getDescription())
            .build()
        ).orElse(null);
    }
}

