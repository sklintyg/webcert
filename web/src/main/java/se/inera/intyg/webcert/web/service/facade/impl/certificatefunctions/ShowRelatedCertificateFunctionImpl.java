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
import static se.inera.intyg.webcert.web.service.utkast.UtkastServiceImpl.UTKAST_INDICATOR;

import java.util.Collections;
import java.util.Optional;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.PreviousIntyg;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@Component
public class ShowRelatedCertificateFunctionImpl implements ShowRelatedCertificateFunction {

    private final UtkastService utkastService;

    public ShowRelatedCertificateFunctionImpl(UtkastService utkastService) {
        this.utkastService = utkastService;
    }

    @Override
    public Optional<ResourceLinkDTO> get(Certificate certificate, WebCertUser webCertUser) {
        if (isShowDoiFromDb(certificate, webCertUser)) {
            return Optional.of(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.SHOW_RELATED_CERTIFICATE,
                    "Visa dödsorsaksintyg",
                    "Visa det dödsorsaksintyg som har skapats från dödsbeviset.",
                    true
                )
            );
        }
        return Optional.empty();
    }

    private boolean isShowDoiFromDb(Certificate certificate, WebCertUser webCertUser) {
        if (!DbModuleEntryPoint.MODULE_ID.equalsIgnoreCase(certificate.getMetadata().getType())) {
            return false;
        }

        final var existingCertificates = utkastService.checkIfPersonHasExistingIntyg(
            Personnummer.createPersonnummer(certificate.getMetadata().getPatient().getPersonId().getId()).orElseThrow(),
            webCertUser,
            certificate.getMetadata().getId()
        );

        final var doiDraft = existingCertificates
            .getOrDefault(UTKAST_INDICATOR, Collections.emptyMap())
            .getOrDefault(DoiModuleEntryPoint.MODULE_ID, null);
        final var doiCertificate = existingCertificates
            .getOrDefault(INTYG_INDICATOR, Collections.emptyMap())
            .getOrDefault(DoiModuleEntryPoint.MODULE_ID, null);

        return isShowDoiEnabled(doiDraft) || isShowDoiEnabled(doiCertificate);
    }

    private static boolean isShowDoiEnabled(PreviousIntyg existing) {
        return existing != null && existing.isEnableShowDoiButton();
    }
}
