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
package se.inera.intyg.webcert.web.service.facade.patient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.facade.model.PersonId;
import se.inera.intyg.infra.pu.integration.api.model.PersonSvar;
import se.inera.intyg.infra.pu.integration.api.services.PUService;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;

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
    public Patient getPatient(String patientId) throws InvalidPatientIdException, PatientSearchErrorException, PatientNoNameException {
        Personnummer formattedPatientId = formatPatientId(patientId);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Getting patient info for: {}", formattedPatientId.getPersonnummerHash());
        }

        PersonSvar personSvar = puService.getPerson(formattedPatientId);
        monitoringService.logPULookup(formattedPatientId, personSvar.getStatus().name());

        if (personSvar.getStatus() == PersonSvar.Status.ERROR) {
            throw new PatientSearchErrorException();
        }

        if (personSvar.getPerson() != null && (personSvar.getPerson().fornamn() == null
            || personSvar.getPerson().efternamn() == null)) {
            throw new PatientNoNameException();
        }

        return convertPatient(personSvar, patientId);
    }

    private Patient convertPatient(PersonSvar personSvar, String patientId) {
        if (personSvar.getPerson() == null) {
            return null;
        }

        return Patient.builder()
            .personId(
                PersonId.builder()
                    .id(patientId)
                    .type("")
                    .build()
            )
            .firstName(personSvar.getPerson().fornamn())
            .lastName(personSvar.getPerson().efternamn())
            .middleName(personSvar.getPerson().mellannamn())
            .fullName(getFullName(personSvar))
            .deceased(personSvar.getPerson().avliden())
            .protectedPerson(personSvar.getPerson().sekretessmarkering())
            .testIndicated(personSvar.getPerson().testIndicator())
            .build();
    }

    private String getFullName(PersonSvar personSvar) {
        final var patient = personSvar.getPerson();
        if (patient.mellannamn() == null || patient.mellannamn().isEmpty()) {
            return patient.fornamn() + " " + patient.efternamn();
        }
        return patient.fornamn() + " " + patient.mellannamn() + " " + patient.efternamn();
    }

    private Personnummer formatPatientId(String personId) throws InvalidPatientIdException {
        return Personnummer.createPersonnummer(personId).orElseThrow(InvalidPatientIdException::new);
    }
}
