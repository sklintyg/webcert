/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.web.controller.integrationtest.facade;

import java.util.Collections;
import java.util.List;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.facade.model.PersonId;
import se.inera.intyg.webcert.web.auth.fake.FakeCredentials;

public class IntegrationTest {

    public static String DR_AJLA = "TSTNMT2321000156-DRAJ";
    public static String DR_BETA = "TSTNMT2321000156-DRBE";

    public static String ALFA_REGIONEN = "TSTNMT2321000156-ALFA";
    public static String ALFA_VARDCENTRAL = "TSTNMT2321000156-ALVC";
    public static String ALFA_LAKARMOTTAGNING = "TSTNMT2321000156-ALLM";

    public static String BETA_REGIONEN = "TSTNMT2321000156-BETA";
    public static String BETA_VARDCENTRAL = "TSTNMT2321000156-BEVC";
    public static String BETA_LAKARMOTTAGNING = "TSTNMT2321000156-BELM";

    public static Patient ATHENA_ANDERSSON = createPatient("191212121212", "Athena", "Andersson");

    private static Patient createPatient(String id, String firstName, String lastName) {
        final var personId = new PersonId();
        personId.setId(id);
        personId.setType("PERSONNUMMER");
        final var patient = new Patient();
        patient.setPersonId(personId);
        patient.setFirstName(firstName);
        patient.setLastName(lastName);
        return patient;
    }

    private static final List<String> LAKARE = Collections.singletonList("Läkare");

    protected static FakeCredentials DR_AJLA_ALFA_VARDCENTRAL = new FakeCredentials.FakeCredentialsBuilder(DR_AJLA,
        ALFA_VARDCENTRAL).legitimeradeYrkesgrupper(LAKARE).build();

    protected static FakeCredentials DR_BEATA_BETA_VARDCENTRAL = new FakeCredentials.FakeCredentialsBuilder(DR_BETA,
        BETA_VARDCENTRAL).legitimeradeYrkesgrupper(LAKARE).build();

}
