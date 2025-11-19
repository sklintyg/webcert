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

import static se.inera.intyg.webcert.web.service.utkast.UtkastServiceImpl.INTYG_INDICATOR;
import static se.inera.intyg.webcert.web.service.utkast.UtkastServiceImpl.UTKAST_INDICATOR;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.facade.modal.typeinfo.CertificateTypeInfoModalProviderResolver;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.PreviousIntyg;

@Service
@RequiredArgsConstructor
public class CertificateTypeInfoModalService {

    private final UtkastService utkastService;
    private final WebCertUserService webCertUserService;

    public Optional<CertificateTypeInfoModal> get(String certificateType, Personnummer patientId) {
        final var user = webCertUserService.getUser();
        final var previousCertificates = utkastService
            .checkIfPersonHasExistingIntyg(patientId, user, null, false);

        final var previousCertificateMap = previousCertificates.getOrDefault(INTYG_INDICATOR, Collections.emptyMap());
        final var previousDraftMap = previousCertificates.getOrDefault(UTKAST_INDICATOR, Collections.emptyMap());

        final var previousCertificate = getPreviousIntyg(certificateType, previousCertificateMap, previousDraftMap);

        if (previousCertificate.isEmpty() || previousCertificate.get().getLatestIntygsId() == null) {
            return Optional.empty();
        }

        final var modalProvider = CertificateTypeInfoModalProviderResolver.getModalProvider(certificateType);

        if (modalProvider == null) {
            return Optional.empty();
        }

        return modalProvider.create(
            info(previousCertificate.get(), certificateType)
        );
    }

    private static Optional<PreviousIntyg> getPreviousIntyg(String certificateType,
        Map<String, PreviousIntyg> previousCertificateMap,
        Map<String, PreviousIntyg> previousDraftMap) {

        if (previousCertificateMap.containsKey(certificateType)) {
            return Optional.of(previousCertificateMap.get(certificateType));
        }

        if (previousDraftMap.containsKey(certificateType)) {
            return Optional.of(previousDraftMap.get(certificateType));
        }

        return Optional.empty();
    }

    private PreviousCertificateInfo info(PreviousIntyg previousCertificate, String certificateType) {
        final var utkast = utkastService.getDraft(previousCertificate.getLatestIntygsId(), certificateType, false);
        return PreviousCertificateInfo.builder()
            .careUnitName(utkast.getEnhetsNamn())
            .careUnitHsaId(utkast.getEnhetsId())
            .careProviderName(utkast.getVardgivarNamn())
            .isDraft(utkast.getStatus() != UtkastStatus.SIGNED)
            .isSameCareProvider(previousCertificate.isSameVardgivare())
            .isSameUnit(previousCertificate.isSameEnhet())
            .build();
    }
}
