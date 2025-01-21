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
import org.springframework.stereotype.Service;
import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.facade.GetCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.GetRelatedCertificateFacadeService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;

@Service
public class GetRelatedCertificateFacadeServiceImpl implements GetRelatedCertificateFacadeService {

    private final GetCertificateFacadeService getCertificateFacadeService;
    private final UtkastService utkastService;

    private final WebCertUserService webCertUserService;

    public GetRelatedCertificateFacadeServiceImpl(GetCertificateFacadeService getCertificateFacadeService, UtkastService utkastService,
        WebCertUserService webCertUserService) {
        this.getCertificateFacadeService = getCertificateFacadeService;
        this.utkastService = utkastService;
        this.webCertUserService = webCertUserService;
    }

    @Override
    public String get(String certificateId) {
        final var certificate = getCertificateFacadeService.getCertificate(certificateId, false, true);
        if (DbModuleEntryPoint.MODULE_ID.equalsIgnoreCase(certificate.getMetadata().getType())) {
            final var existingCertificates = utkastService.checkIfPersonHasExistingIntyg(
                Personnummer.createPersonnummer(certificate.getMetadata().getPatient().getPersonId().getId()).orElseThrow(),
                webCertUserService.getUser(),
                certificateId
            );
            final var doiDraft = existingCertificates
                .getOrDefault(UTKAST_INDICATOR, Collections.emptyMap())
                .getOrDefault(DoiModuleEntryPoint.MODULE_ID, null);
            if (doiDraft != null && doiDraft.isEnableShowDoiButton()) {
                return doiDraft.getLatestIntygsId();
            }
            final var doiCertificate = existingCertificates
                .getOrDefault(INTYG_INDICATOR, Collections.emptyMap())
                .getOrDefault(DoiModuleEntryPoint.MODULE_ID, null);
            if (doiCertificate != null && doiCertificate.isEnableShowDoiButton()) {
                return doiCertificate.getLatestIntygsId();
            }
        }
        return null;
    }
}
