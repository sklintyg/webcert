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

import java.util.Optional;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.ag7804.support.Ag7804EntryPoint;
import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.facade.util.CandidateDataHelper;
import se.inera.intyg.webcert.web.service.utkast.dto.UtkastCandidateMetaData;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@Component
public class CreateCertificateFromCandidateFunctionImpl implements CreateCertificateFromCandidateFunction {

    private static final String CREATE_FROM_CANDIDATE_NAME = "Hjälp med ifyllnad?";
    private static final String CREATE_FROM_CANDIDATE_WITH_MESSAGE_NAME = "Information om dödsbevis";

    private final CandidateDataHelper candidateDataHelper;

    public CreateCertificateFromCandidateFunctionImpl(CandidateDataHelper candidateDataHelper) {
        this.candidateDataHelper = candidateDataHelper;
    }

    @Override
    public Optional<ResourceLinkDTO> get(Certificate certificate) {
        if (missingSupportToCreateFromCandidate(certificate)) {
            return Optional.empty();
        }

        final var candidateMetadata = getCandidateMetadata(certificate);
        if (candidateMetadata.isEmpty()) {
            return Optional.empty();
        }

        if (!candidateMetadata.get().getSameVardenhet() && DbModuleEntryPoint.MODULE_ID.equalsIgnoreCase(
            candidateMetadata.get().getIntygType())) {
            return Optional.of(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.CREATE_CERTIFICATE_FROM_CANDIDATE_WITH_MESSAGE,
                    CREATE_FROM_CANDIDATE_WITH_MESSAGE_NAME,
                    "",
                    "Det finns ett signerat dödsbevis för detta personnummer på annan vårdenhet än den du är inloggad på. "
                        + "Vill du se informationen om vilken vårdenhet det handlar om?",
                    true
                )
            );
        } else {
            return Optional.of(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.CREATE_CERTIFICATE_FROM_CANDIDATE,
                    CREATE_FROM_CANDIDATE_NAME,
                    "",
                    getBody(candidateMetadata.get()),
                    true
                )
            );
        }
    }

    private boolean missingSupportToCreateFromCandidate(Certificate certificate) {
        return !(isFirstVersion(certificate) && noParentRelations(certificate) && isCertificateTypeSupported(certificate));
    }

    private Optional<UtkastCandidateMetaData> getCandidateMetadata(Certificate certificate) {
        return candidateDataHelper.getCandidateMetadata(
            certificate.getMetadata().getType(),
            certificate.getMetadata().getTypeVersion(),
            Personnummer.createPersonnummer(certificate.getMetadata().getPatient().getPersonId().getId()).orElseThrow()
        );
    }

    private boolean isFirstVersion(Certificate certificate) {
        return certificate.getMetadata().getVersion() == 0;
    }

    private boolean noParentRelations(Certificate certificate) {
        return certificate.getMetadata().getRelations() == null || certificate.getMetadata().getRelations().getParent() == null;
    }

    private boolean isCertificateTypeSupported(Certificate certificate) {
        return certificate.getMetadata().getType().equalsIgnoreCase(Ag7804EntryPoint.MODULE_ID)
            || certificate.getMetadata().getType().equalsIgnoreCase(DoiModuleEntryPoint.MODULE_ID);
    }

    private String getBody(UtkastCandidateMetaData candidateMetaData) {
        if (LisjpEntryPoint.MODULE_ID.equalsIgnoreCase(candidateMetaData.getIntygType())) {
            return "<p>Det finns ett Läkarintyg för sjukpenning för denna patient som är utfärdat "
                + "<span class='iu-fw-bold'>"
                + candidateMetaData.getIntygCreated().toLocalDate()
                + "</span> på en enhet som du har åtkomst till. "
                + "Vill du kopiera de svar som givits i det intyget till detta intygsutkast?</p>";
        }

        if (DbModuleEntryPoint.MODULE_ID.equalsIgnoreCase(candidateMetaData.getIntygType())) {
            return "<p>Det finns ett signerat dödsbevis (från den "
                + "<span class='iu-fw-bold'>"
                + candidateMetaData.getIntygCreated().toLocalDate()
                + "</span>) för detta personnummer på samma enhet som du är inloggad. "
                + "Vill du kopiera de svar som givits i det intyget till detta intygsutkast?</p>";
        }

        throw new IllegalArgumentException(
            String.format("Candidate certificate of type '%s' is not supported", candidateMetaData.getIntygType())
        );
    }
}
