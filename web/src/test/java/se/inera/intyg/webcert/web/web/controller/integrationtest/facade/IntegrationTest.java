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
package se.inera.intyg.webcert.web.web.controller.integrationtest.facade;

import java.util.Collections;
import java.util.List;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.facade.model.PersonId;
import se.inera.intyg.webcert.web.auth.fake.FakeCredentials;

public class IntegrationTest {

    public static String DR_AJLA = "TSTNMT2321000156-DRAA";
    public static final String VARDADMIN_ALVA = "TSTNMT2321000156-VAAA";
    public static String DR_BEATA = "TSTNMT2321000156-DRBE";

    public static String ALFA_REGIONEN = "TSTNMT2321000156-ALFA";
    public static final String ALFA_REGIONEN_NAME = "Alfa Regionen";
    public static String ALFA_VARDCENTRAL = "TSTNMT2321000156-ALVC";
    public static final String ALFA_VARDCENTRAL_NAME = "Alfa Vårdcentral";
    public static String ALFA_LAKARMOTTAGNING = "TSTNMT2321000156-ALLM";

    public static String BETA_REGIONEN = "TSTNMT2321000156-BETA";
    public static String BETA_REGIONEN_NAME = "Beta Regionen";
    public static String BETA_VARDCENTRAL = "TSTNMT2321000156-BEVC";
    public static String BETA_VARDCENTRAL_NAME = "Beta Vårdcentral";
    public static String BETA_LAKARMOTTAGNING = "TSTNMT2321000156-BELM";

    public static Patient ATHENA_ANDERSSON = createPatient("194011306125", "Athena", "Andersson");
    public static Patient ALEXA_VALFRIDSSON = createPatient("194110299221", "Alexa", "Valfridsson");

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
            .build();
    }

    protected static final List<String> LAKARE = Collections.singletonList("Läkare");
    protected static final List<String> VARDADMIN = Collections.singletonList("Vårdadministratör");


    protected static FakeCredentials DR_AJLA_ALFA_VARDCENTRAL = new FakeCredentials.FakeCredentialsBuilder(DR_AJLA,
        ALFA_VARDCENTRAL).legitimeradeYrkesgrupper(LAKARE).build();

    protected static final FakeCredentials VARDADMIN_ALVA_ALFA_VARDCENTRAL = new FakeCredentials.FakeCredentialsBuilder(VARDADMIN_ALVA,
        ALFA_VARDCENTRAL).legitimeradeYrkesgrupper(VARDADMIN).build();

    protected static FakeCredentials DR_BEATA_BETA_VARDCENTRAL = new FakeCredentials.FakeCredentialsBuilder(DR_BEATA,
        BETA_VARDCENTRAL).legitimeradeYrkesgrupper(LAKARE).build();

}
