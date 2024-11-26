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

package se.inera.intyg.webcert.web.csintegration.patient;

import java.util.Optional;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.validate.SamordningsnummerValidator;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;

@Component
public class CertificateServicePatientHelper {

    private final PatientDetailsResolver patientDetailsResolver;

    public CertificateServicePatientHelper(PatientDetailsResolver patientDetailsResolver) {
        this.patientDetailsResolver = patientDetailsResolver;
    }

    public CertificateServicePatientDTO get(Personnummer patientId) {
        final var personSvar = patientDetailsResolver.getPersonFromPUService(patientId);
        return CertificateServicePatientDTO.builder()
            .id(getPersonId(patientId))
            .protectedPerson(personSvar.getPerson().sekretessmarkering())
            .deceased(personSvar.getPerson().avliden())
            .firstName(personSvar.getPerson().fornamn())
            .lastName(personSvar.getPerson().efternamn())
            .middleName(personSvar.getPerson().mellannamn())
            .street(personSvar.getPerson().postadress())
            .zipCode(personSvar.getPerson().postnummer())
            .city(personSvar.getPerson().postort())
            .testIndicated(personSvar.getPerson().testIndicator())
            .build();
    }

    public PersonIdDTO getPersonId(Personnummer patientId) {
        return new PersonIdDTO(
            isCoordinationNumber(patientId) ? PersonIdType.COORDINATION_NUMBER : PersonIdType.PERSONAL_IDENTITY_NUMBER,
            patientId.getOriginalPnr()
        );
    }

    private boolean isCoordinationNumber(Personnummer personId) {
        return SamordningsnummerValidator.isSamordningsNummer(Optional.of(personId));
    }

}