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
package se.inera.intyg.webcert.web.web.controller.testability.facade.util;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.facade.model.PersonId;

@Component
public class SupportedPatientsUtil {

    public static final Patient ATHENA_ANDERSSON = createPatient("194011306125", "Athena", "Andersson");
    public static final Patient ALVE_ALFREDSSON = createPatient("194112128154", "Alve", "Alfridsson");
    public static final Patient ALEXA_VALFRIDSSON = createPatient("194110299221", "Alexa", "Valfridsson");
    public static final Patient BOSTADSLOSE_ANDERSSON = createPatient("194110147495", "Bostadslöse", "Andersson");
    public static final Patient ATLAS_ABRAHAMSSON = createPatient("194111299055", "Atlas", "Abrahamsson");
    public static final Patient ANONYMA_ATTILA = createPatient("194012019149", "Anonyma", "Attila");

    public List<Patient> get() {
        final var patients = new ArrayList<Patient>();
        patients.add(ATHENA_ANDERSSON);
        patients.add(ALVE_ALFREDSSON);
        patients.add(BOSTADSLOSE_ANDERSSON);
        patients.add(ATLAS_ABRAHAMSSON);
        patients.add(ANONYMA_ATTILA);
        patients.add(ALEXA_VALFRIDSSON);
        return patients;
    }

    private static Patient createPatient(String id, String firstName, String lastName) {
        return Patient.builder()
            .personId(
                PersonId.builder()
                    .id(id)
                    .type("PERSON_NUMMER")
                    .build()
            )
            .firstName(firstName)
            .lastName(lastName)
            .fullName(firstName + ' ' + lastName)
            .build();
    }
}
