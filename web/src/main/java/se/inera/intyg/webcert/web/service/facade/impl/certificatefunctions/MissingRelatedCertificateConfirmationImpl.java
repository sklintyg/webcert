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
package se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions;

import static se.inera.intyg.webcert.web.service.utkast.UtkastServiceImpl.INTYG_INDICATOR;

import java.util.Collections;
import java.util.Optional;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@Component
public class MissingRelatedCertificateConfirmationImpl implements MissingRelatedCertificateConfirmation {

    private final UtkastService utkastService;

    private final WebCertUserService webCertUserService;

    public MissingRelatedCertificateConfirmationImpl(UtkastService utkastService, WebCertUserService webCertUserService) {
        this.utkastService = utkastService;
        this.webCertUserService = webCertUserService;
    }

    @Override
    public Optional<ResourceLinkDTO> get(String certificateType, Personnummer patientId) {
        final var relatedCertificateType = relatedCertificateType(certificateType);
        if (relatedCertificateType == null) {
            return Optional.empty();
        }

        if (isRelatedCertificateTypeExistingOnSameCareProvider(relatedCertificateType, patientId)) {
            return Optional.empty();
        }

        return Optional.of(ResourceLinkDTO.create(
            ResourceLinkTypeDTO.MISSING_RELATED_CERTIFICATE_CONFIRMATION,
            "Dödsbevis saknas",
            "",
            "Är du säker att du vill skapa ett dödsorsaksintyg? Det finns inget dödsbevis i nuläget inom vårdgivaren.\n"
                + "\n"
                + "Dödsorsaksintyget bör alltid skapas efter dödsbeviset.",
            true)
        );
    }

    private boolean isRelatedCertificateTypeExistingOnSameCareProvider(String relatedCertificateType, Personnummer patientId) {
        final var existingCertificates = utkastService.checkIfPersonHasExistingIntyg(patientId, webCertUserService.getUser(), null);
        final var existingSignedCertificates = existingCertificates.getOrDefault(INTYG_INDICATOR, Collections.emptyMap());
        return existingSignedCertificates.containsKey(relatedCertificateType)
            && existingSignedCertificates.get(relatedCertificateType).isSameVardgivare();
    }

    private String relatedCertificateType(String certificateType) {
        return DoiModuleEntryPoint.MODULE_ID.equalsIgnoreCase(certificateType) ? DbModuleEntryPoint.MODULE_ID : null;
    }
}
