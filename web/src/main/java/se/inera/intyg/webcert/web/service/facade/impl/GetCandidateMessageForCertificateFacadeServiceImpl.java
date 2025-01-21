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

import java.util.Optional;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.facade.GetCandidateMesssageForCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.GetCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.util.CandidateDataHelper;
import se.inera.intyg.webcert.web.service.utkast.dto.UtkastCandidateMetaData;
import se.inera.intyg.webcert.web.web.controller.facade.dto.GetCandidateMessageForCertificateDTO;

@Service
public class GetCandidateMessageForCertificateFacadeServiceImpl implements GetCandidateMesssageForCertificateFacadeService {

    private static final String MISSING_MESSAGE = "Saknar meddelande";
    private static final String MISSING_TITLE = "Saknar titel";
    private static final String UNIT_TITLE_TEXT = "Information om vårdenhet";
    private final GetCertificateFacadeService getCertificateFacadeService;
    private final CandidateDataHelper candidateDataHelper;

    public GetCandidateMessageForCertificateFacadeServiceImpl(GetCertificateFacadeService getCertificateFacadeService,
        CandidateDataHelper candidateDataHelper) {
        this.getCertificateFacadeService = getCertificateFacadeService;
        this.candidateDataHelper = candidateDataHelper;
    }

    @Override
    public GetCandidateMessageForCertificateDTO get(String certificateId) {
        final var certificate = getCertificateFacadeService.getCertificate(certificateId, false, true);
        final var candidateMetadata = getCandidateMetadata(certificate);

        if (candidateMetadata.isEmpty()) {
            return GetCandidateMessageForCertificateDTO.create(MISSING_MESSAGE, MISSING_TITLE);
        }

        if (DbModuleEntryPoint.MODULE_ID.equalsIgnoreCase(candidateMetadata.get().getIntygType())) {
            return GetCandidateMessageForCertificateDTO.create(getDbMessage(candidateMetadata.get().getEnhetName()), UNIT_TITLE_TEXT);
        }
        return GetCandidateMessageForCertificateDTO.create(MISSING_MESSAGE, MISSING_TITLE);
    }

    private String getDbMessage(String enhetName) {
        return "<p>Det finns ett signerat dödsbevis för detta personnummer på "
            + "<span class='iu-fw-bold'>"
            + enhetName
            + "</span>. Det är tyvärr inte möjligt att kopiera de svar som givits i det intyget till detta intygsutkast. ";
    }

    private Optional<UtkastCandidateMetaData> getCandidateMetadata(Certificate certificate) {
        return candidateDataHelper.getCandidateMetadata(
            certificate.getMetadata().getType(),
            certificate.getMetadata().getTypeVersion(),
            Personnummer.createPersonnummer(certificate.getMetadata().getPatient().getPersonId().getId()).orElseThrow()
        );
    }
}
