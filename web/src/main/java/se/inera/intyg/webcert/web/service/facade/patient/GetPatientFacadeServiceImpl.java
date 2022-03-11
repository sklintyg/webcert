/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.facade.patient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.facade.model.PersonId;
import se.inera.intyg.infra.integration.pu.model.PersonSvar;
import se.inera.intyg.infra.integration.pu.services.PUService;
import se.inera.intyg.schemas.contract.InvalidPersonNummerException;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.web.controller.facade.dto.PatientResponseDTO;

@Service
public class GetPatientFacadeServiceImpl implements GetPatientFacadeService {
    private static final Logger LOG = LoggerFactory.getLogger(GetPatientFacadeServiceImpl.class);

    private final PUService puService;

    private final MonitoringLogService monitoringService;

    @Autowired
    public GetPatientFacadeServiceImpl(PUService puService, MonitoringLogService monitoringService) {
        this.puService = puService;
        this.monitoringService = monitoringService;
    }

    @Override
    public PatientResponseDTO getPatient(String patientId) {
        try {
            Personnummer formattedPatientId = formatPatientId(patientId);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Getting patient info for: {}", formattedPatientId.getPersonnummerHash());
            }

            PersonSvar personSvar = puService.getPerson(formattedPatientId);

            monitoringService.logPULookup(formattedPatientId, personSvar.getStatus().name());

            return convertPatientResponse(personSvar, patientId);

        } catch (InvalidPersonNummerException e) {
            LOG.error(e.getMessage());
            return null;
        }
    }

    private PatientResponseDTO convertPatientResponse(PersonSvar personSvar, String patientId) {
        final var isPatientDefined = personSvar.getPerson() != null;
        final var patient = isPatientDefined ? Patient.builder()
                .personId(
                        PersonId.builder()
                                .id(patientId)
                                .type("")
                                .build()
                )
                .firstName(personSvar.getPerson().getFornamn())
                .lastName(personSvar.getPerson().getEfternamn())
                .middleName(personSvar.getPerson().getMellannamn())
                .fullName(getFullName(personSvar))
                .deceased(personSvar.getPerson().isAvliden())
                .protectedPerson(personSvar.getPerson().isSekretessmarkering())
                .testIndicated(personSvar.getPerson().isTestIndicator())
                .build() : Patient.builder().build();

        return PatientResponseDTO.create(patient, personSvar.getStatus());
    }

    private String getFullName(PersonSvar personSvar) {
        final var patient = personSvar.getPerson();
        if (patient.getMellannamn() == null || patient.getMellannamn().length() == 0) {
            return patient.getFornamn() + " " + patient.getEfternamn();
        }
        return patient.getFornamn() + " " + patient.getMellannamn() + " " + patient.getEfternamn();
    }

    private Personnummer formatPatientId(String personId) throws InvalidPersonNummerException {
        return Personnummer.createPersonnummer(personId)
                .orElseThrow(() -> new InvalidPersonNummerException("Could not create Personnummer object from personId: " + personId));
    }
}
