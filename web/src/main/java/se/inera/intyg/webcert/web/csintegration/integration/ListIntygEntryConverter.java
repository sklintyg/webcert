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

package se.inera.intyg.webcert.web.csintegration.integration;

import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;

public class ListIntygEntryConverter {

    public ListIntygEntry convert(Certificate certificate) {
        final var listIntygEntry = new ListIntygEntry();
        final var metadata = certificate.getMetadata();

        listIntygEntry.setIntygId(metadata.getId());
        listIntygEntry.setIntygType(metadata.getType());
        listIntygEntry.setIntygTypeName(metadata.getTypeName());
        listIntygEntry.setIntygTypeVersion(metadata.getTypeVersion());
        listIntygEntry.setVersion(metadata.getVersion());

        listIntygEntry.setTestIntyg(metadata.isTestCertificate());
        listIntygEntry.setStatus(metadata.getStatus().toString());
        listIntygEntry.setStatusName(metadata.getStatus().name());

        listIntygEntry.setPatientId(createPatientId(metadata.getPatient().getPersonId().getId()));
        listIntygEntry.setAvliden(metadata.getPatient().isDeceased());
        listIntygEntry.setSekretessmarkering(metadata.getPatient().isProtectedPerson());

        listIntygEntry.setUpdatedSignedBy(metadata.getIssuedBy().getFullName());
        listIntygEntry.setLastUpdatedSigned(metadata.getReadyForSign());  //TODO is this correct?
        listIntygEntry.setVardenhetId(metadata.getCareUnit().getUnitId());
        listIntygEntry.setVardgivarId(metadata.getCareProvider().getUnitId());
        //TODO add separate converter for resource links
        listIntygEntry.setVidarebefordrad(metadata.isForwarded());
        return listIntygEntry;
    }

    private Personnummer createPatientId(String patientId) {
        return Personnummer.createPersonnummer(patientId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    String.format("PatientId has wrong format: '%s'", patientId)
                )
            );
    }
}
